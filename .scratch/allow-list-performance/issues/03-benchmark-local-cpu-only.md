Status: ready-for-agent

# Benchmark local CPU-only (`.main.kts`)

## What to build

Script Kotlin `benchmark/allowlist-benchmark.main.kts` que mede a performance do algoritmo de lookup em isolamento — sem Spring, sem HTTP, sem rede. Usa `@file:DependsOn` para resolver fastutil via Maven, portanto requer apenas o CLI `kotlin` instalado.

O script gera 1.000.000 PVs aleatórios em memória, executa 1.000.000 ops de warmup (descartadas), depois mede 5.000.000 ops em lotes de 1.000 para reduzir overhead do `System.nanoTime()`. Padrão de acesso: 70% hits / 30% misses — igual ao teste k6 para garantir comparabilidade direta.

Além da latência, o script mede o consumo de memória do `LongOpenHashSet` e do `HashSet<Long>` padrão com o mesmo volume de dados, usando `Runtime.getRuntime()` com GC forçado antes de cada medição.

Resultado gravado em `results/benchmark-results.json`.

## Acceptance criteria

- [ ] `kotlin benchmark/allowlist-benchmark.main.kts` executa sem erros
- [ ] Script aceita path de saída como argumento: `kotlin benchmark/allowlist-benchmark.main.kts results/benchmark-results.json`
- [ ] `results/benchmark-results.json` contém: `p50_us`, `p75_us`, `p90_us`, `p95_us`, `p99_us`, `max_us`, `throughput_ops_sec`, `memory_mb`, `memory_hashset_mb`, `total_pvs`, `measured_ops`, `hit_ratio`
- [ ] Stdout exibe resumo legível com percentis, throughput e comparação de memória
- [ ] p99 medido está na ordem de grandeza de microsegundos (< 1ms), demonstrando eficiência do algoritmo puro

## Blocked by

- #01 — build system Gradle precisa existir para confirmar que fastutil resolve corretamente
