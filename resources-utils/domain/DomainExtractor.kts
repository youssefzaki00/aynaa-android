#!/usr/bin/env kotlin



import java.io.File

fun main() {
    val inputFile = File("hosts.txt")
    val outputFile = File("../../app/src/main/assets/domains.txt")

    if (!inputFile.exists()) {
        println("Error: hosts.txt not found at ${inputFile.absolutePath}")
        return
    }

    val domains = inputFile.useLines { lines ->
        lines.map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { line ->
                val parts = line.split(Regex("\\s+"))
                if (parts.size >= 2) parts[1] else null
            }
            .distinct()
            .toList()
    }

    outputFile.writeText(domains.joinToString("\n"))
    println("Successfully extracted ${domains.size} domains to domains.txt")

}

main()
