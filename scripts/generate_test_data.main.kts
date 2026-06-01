import java.io.File

val outputDir  = File("src/main/resources/lists")
val listCount  = 20
val pvsPerList = 500_000

outputDir.mkdirs()

for (i in 0 until listCount) {
    val name  = if (i == 0) "merchants" else "merchants-${i + 1}"
    val start = i * pvsPerList + 1
    val file  = File(outputDir, "$name.json")
    file.bufferedWriter().use { w ->
        w.write("""{"pvs":[""")
        for (pv in start until start + pvsPerList) {
            if (pv > start) w.write(",")
            w.write(pv.toString())
        }
        w.write("]}")
    }
    println("Generated ${file.path} ($pvsPerList PVs, range $start–${start + pvsPerList - 1})")
}

println("\nDone: $listCount lists × ${"%,d".format(pvsPerList)} PVs = ${"%,d".format(listCount * pvsPerList)} total PVs")
