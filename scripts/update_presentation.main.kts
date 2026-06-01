import java.io.File

val benchFile = File("results/benchmark-results.json")
val k6File    = File("results/summary.json")
val presFile  = File("presentation/index.html")

for (f in listOf(benchFile, k6File, presFile)) {
    if (!f.exists()) { System.err.println("Error: ${f.path} not found"); kotlin.system.exitProcess(1) }
}

val benchJson = benchFile.readText().trim()
val k6Json    = k6File.readText().trim()

val block = "// RESULTS_START\nconst RESULTS = {\"benchmark\": $benchJson, \"k6\": $k6Json};\n// RESULTS_END"

val html    = presFile.readText()
val updated = html.replace(Regex("// RESULTS_START.*?// RESULTS_END", RegexOption.DOT_MATCHES_ALL), block)

if (updated == html) {
    System.err.println("Error: RESULTS_START/END markers not found in presentation")
    kotlin.system.exitProcess(1)
}

presFile.writeText(updated)

fun field(json: String, key: String) =
    Regex(""""$key"\s*:\s*([^,\n}]+)""").find(json)?.groupValues?.get(1)?.trim() ?: "?"

println("Presentation updated")
println("  benchmark p99 : ${field(benchJson, "p99_us")} μs")
println("  k6 p99        : ${field(k6Json, "p99_ms")} ms")
