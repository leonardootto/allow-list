@file:DependsOn("it.unimi.dsi:fastutil:8.5.15")

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import java.io.File
import kotlin.random.Random

val SIZE = 1_000_000
val WARMUP_OPS = 1_000_000
val MEASURED_OPS = 5_000_000
val BATCH_SIZE = 1_000

fun heapUsed(): Long = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }

println("=== Allow-List Benchmark ===")
println("Dataset: $SIZE PVs | Warmup: $WARMUP_OPS ops | Measured: $MEASURED_OPS ops\n")

// --- Memory: LongOpenHashSet ---
System.gc(); Thread.sleep(300)
val baseline = heapUsed()
val longSet = LongOpenHashSet(SIZE)
for (i in 1..SIZE.toLong()) longSet.add(i)
System.gc(); Thread.sleep(300)
val longSetMb = maxOf(0.0, (heapUsed() - baseline).toDouble() / 1_048_576)

// --- Memory: HashSet<Long> (measured on top of longSet, both live) ---
System.gc(); Thread.sleep(300)
val baselineHash = heapUsed()
val hashSet = HashSet<Long>(SIZE * 2)
for (i in 1..SIZE.toLong()) hashSet.add(i)
System.gc(); Thread.sleep(300)
val hashSetMb = maxOf(0.0, (heapUsed() - baselineHash).toDouble() / 1_048_576)

println("Memory — LongOpenHashSet : ${"%.2f".format(longSetMb)} MB")
println("Memory — HashSet<Long>   : ${"%.2f".format(hashSetMb)} MB")
println("Ratio                    : ${"%.1f".format(if (longSetMb > 0) hashSetMb / longSetMb else 0.0)}x\n")

// --- Query sequence: 70% hits (1..SIZE), 30% misses (SIZE+1..2*SIZE) ---
val rng = Random(42)
val totalOps = WARMUP_OPS + MEASURED_OPS
val queries = LongArray(totalOps) {
    if (rng.nextDouble() < 0.7) rng.nextLong(1, SIZE.toLong() + 1)
    else rng.nextLong(SIZE.toLong() + 1, SIZE.toLong() * 2 + 1)
}

// Sink accumulator — consuming contains() prevents the JIT from eliminating
// the call as dead code, which would make the measured latency meaningless.
var sink = 0L

// --- Warmup ---
print("Warming up... ")
for (i in 0 until WARMUP_OPS) if (longSet.contains(queries[i])) sink++
println("done")

// --- Measured run ---
print("Measuring... ")
val batches = MEASURED_OPS / BATCH_SIZE
val batchNs = LongArray(batches)
var idx = WARMUP_OPS
for (b in 0 until batches) {
    val t0 = System.nanoTime()
    for (k in 0 until BATCH_SIZE) if (longSet.contains(queries[idx++])) sink++
    batchNs[b] = (System.nanoTime() - t0) / BATCH_SIZE
}
println("done\n")

batchNs.sort()

fun pct(p: Double) = batchNs[(batchNs.size * p / 100.0).toInt().coerceIn(0, batchNs.size - 1)] / 1000.0

val p50 = pct(50.0); val p75 = pct(75.0); val p90 = pct(90.0)
val p95 = pct(95.0); val p99 = pct(99.0); val maxUs = batchNs.last() / 1000.0
val throughput = 1_000_000_000.0 / batchNs.average()

println("p50=${"%.3f".format(p50)}μs  p75=${"%.3f".format(p75)}μs  p90=${"%.3f".format(p90)}μs")
println("p95=${"%.3f".format(p95)}μs  p99=${"%.3f".format(p99)}μs  max=${"%.3f".format(maxUs)}μs")
println("throughput=${"%.0f".format(throughput)} ops/s")
println("hits observed (sink)=$sink\n")

val json = """{
  "p50_us": ${"%.3f".format(p50)},
  "p75_us": ${"%.3f".format(p75)},
  "p90_us": ${"%.3f".format(p90)},
  "p95_us": ${"%.3f".format(p95)},
  "p99_us": ${"%.3f".format(p99)},
  "max_us": ${"%.3f".format(maxUs)},
  "throughput_ops_sec": ${"%.0f".format(throughput)},
  "memory_mb": ${"%.2f".format(longSetMb)},
  "memory_hashset_mb": ${"%.2f".format(hashSetMb)}
}"""

File("results").mkdirs()
File("results/benchmark-results.json").writeText(json)
println("Results → results/benchmark-results.json")
