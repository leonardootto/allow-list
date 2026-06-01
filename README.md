# Allow-List Service

A static, in-memory HTTP service that checks whether a given PV (point-of-sale identifier) belongs to a named allow-list. Designed for high-throughput, low-latency lookups with zero runtime dependencies.

## How it works

Allow-lists are JSON files embedded in the JAR. On startup, every list is loaded and indexed into a [`LongOpenHashSet`](https://fastutil.di.unimi.it/) (fastutil) — a primitive hash set that avoids boxing `long` values. The application blocks until all lists are ready before accepting traffic, so every request sees fully-loaded state.

```
GET /allow-list/{listName}?pv={Long}
```

| Scenario | Response |
|---|---|
| PV found in list | `200 {"allowed": true}` |
| PV not in list | `200 {"allowed": false}` |
| List does not exist | `404` |
| Invalid `pv` parameter | `400` |

A `GET /actuator/health` endpoint serves as the Kubernetes readiness probe.

## Performance

Measured with 20 lists × 500,000 PVs (10M total), 70% hit / 30% miss pattern, 100 virtual users.

**Local CPU benchmark** (Kotlin Script, no HTTP, no Spring):

| p50 | p95 | p99 | Throughput |
|---|---|---|---|
| 0.016 μs | 0.021 μs | 0.085 μs | ~56M ops/s |

**Load test** (k6 + Docker bridge, end-to-end latency):

| p50 | p95 | p99 | Requests | Errors |
|---|---|---|---|---|
| 2.32 ms | 5.48 ms | 8.59 ms | 5,348,849 | 0 |

SLA target: p99 < 20 ms. Result: **PASSED**.

**Memory** (1M PVs): `LongOpenHashSet` uses ~16 MB vs ~63 MB for `HashSet<Long>` — a 3.9× reduction from avoiding boxing.

## Running

Requires Java 21, Docker, and Python 3.

```bash
./run.sh
```

This runs the full pipeline in sequence:

1. Local CPU benchmark → `results/benchmark-results.json`
2. Generates test data (20 × 500k PVs)
3. Builds the JAR (`./gradlew bootJar`)
4. Runs Docker Compose (Spring Boot app + k6 load test) → `results/summary.json`
5. Injects results into `presentation/index.html`

Open `presentation/index.html` in a browser to see the results presentation.

To run only the tests:

```bash
./gradlew test
```

## Adding an allow-list

Place a JSON file in `src/main/resources/lists/`:

```json
{"pvs": [1, 2, 3, 42]}
```

The filename (without `.json`) becomes the list name. Rebuild and redeploy to activate.

## Architecture decisions

- [`docs/adr/0001`](docs/adr/0001-fastutil-longopenHashSet.md) — Why `LongOpenHashSet` instead of `HashSet<Long>`
- [`docs/adr/0002`](docs/adr/0002-json-embarcado-no-classpath.md) — Why lists are embedded in the JAR instead of a database or Redis
- [`docs/adr/0003`](docs/adr/0003-carregamento-eager-bloqueia-startup.md) — Why startup blocks until all lists are loaded

## Project structure

```
src/main/kotlin/         Application, Controller, Service
src/main/resources/lists/    Allow-list JSON files (embedded in JAR)
benchmark/               Kotlin Script CPU-only benchmark
k6/                      k6 load test script
scripts/                 Data generation and presentation update
presentation/            Reveal.js presentation (auto-updated by run.sh)
docs/adr/                Architecture Decision Records
```

## Stack

- Kotlin 2.1 + Spring Boot 3.4
- [fastutil](https://fastutil.di.unimi.it/) 8.5 (`LongOpenHashSet`)
- k6 for load testing
- Docker Compose for the test environment
