package com.mafazaa.ainaa.data

import com.google.gson.Gson
import com.mafazaa.ainaa.domain.models.ScreenAnalysis
import com.mafazaa.ainaa.domain.models.ScriptResult
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class JsEngineTest {
    val engine = LuaRepo().apply {
        setCodes(
            LuaRepo.defaultScripts
        )
    }
    val gson = Gson()
    val root = File("..\\resources-utils")
    val reportDir = File(root, "report").apply { mkdirs() }
    val reportShouldBlock = File(reportDir,"should-block-report.txt")
    val reportShouldNotBlock = File(reportDir,"should-not-block-report.txt")
    val reportBrowsers = File(reportDir,"browsers.txt")

    @Test
    fun `test codes should blocking`() {
        val root = File("..\\resources-utils\\block")
        val manufacturers = root.list() ?: return
        val report = FileOutputStream(reportShouldBlock, false)
        for (m in manufacturers) {
            report.append("\n=== $m ===\n")
            loop@ for (file in File(root, m).listFiles() ?: continue) {
                val rootJson = com.google.gson.JsonParser.parseString(file.readText()).asJsonObject
                val screenJson = rootJson.getAsJsonObject("screenAnalysis")
                val analysis = gson.fromJson(screenJson, ScreenAnalysis::class.java)
                AntiDisableKotlinChecker.checkers.forEach { checker ->
                    if (checker.check(analysis)) {
                        report.append("already blocked ${file.name} of $m by ${checker.name}\n")
                        continue@loop
                    }
                }
                engine.evaluate(analysis).also { result ->
                    if (result is ScriptResult.Error) {
                        report.append("error blocking ${file.name} of $m : ${result.error}")
                    }
                    val matched = (result as ScriptResult.Success).matched
                    report.append("blocked ${file.name} of $m by ${result.scriptName} $matched")
                }
            }

        }
    }
    @Test
    fun `get url from browsers`(){
        val dir =File(root,"browser")
        val files = dir.listFiles() ?: return
        val report = FileOutputStream(reportBrowsers, false)
        for (file in files) {
            val rootJson = com.google.gson.JsonParser.parseString(file.readText()).asJsonObject
            val screenJson = rootJson.getAsJsonObject("screenAnalysis")
            val analysis = gson.fromJson(screenJson, ScreenAnalysis::class.java)
            val url =getUrlFromBrowser(analysis)
            report.append(file.name + " : " + url + "\n")
        }
    }

    @Test
    fun `test codes should not blocking`() {
        val root = File("..\\resources-utils\\pass")
        if (!root.exists()) return

        // gather all files under the pass folder (no manufacturers list)
        val files = root.walkTopDown().filter { it.isFile }.toList()
        val builder = StringBuilder()
        builder.appendLine("=== pass files ===")

        for (file in files) {
            builder.appendLine("File: ${file.path}")
            try {
                val rootJson = com.google.gson.JsonParser.parseString(file.readText()).asJsonObject
                val screenJson = rootJson.getAsJsonObject("screenAnalysis")
                val analysis = gson.fromJson(screenJson, ScreenAnalysis::class.java)
                val result = engine.evaluate(analysis)
                AntiDisableKotlinChecker.checkers.forEach { checker ->
                    if (checker.check(analysis)) {
                        builder.appendLine("already blocked by ${checker.name}")
                    }
                }
                when (result) {
                    is ScriptResult.Success -> {
                        if (result.matched) {
                            builder.appendLine("unexpectedly blocked by ${result.scriptName} ")
                        }
                    }
                    is ScriptResult.Error -> builder.appendLine("error evaluating: ${result.error}")
                }

            } catch (e: Exception) {
                builder.appendLine("exception processing ${file.name}: ${e.message}")
            }
            builder.appendLine()
        }

        // write report at once using StringBuilder
        reportShouldNotBlock.writeText(builder.toString())
    }


}

private fun FileOutputStream.append(text: String) {
    this.write((text + "\n").toByteArray())
}
