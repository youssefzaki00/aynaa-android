package com.mafazaa.ainaa.data

import com.mafazaa.ainaa.domain.models.ScreenAnalysis
import com.mafazaa.ainaa.domain.models.ScreenNode
import com.mafazaa.ainaa.domain.models.ScriptCode
import com.mafazaa.ainaa.domain.models.ScriptResult
import com.mafazaa.ainaa.domain.repo.AntiDisableRepo
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform

class LuaRepo : AntiDisableRepo {
    private var scripts: List<ScriptCode> = emptyList()

    override fun setCodes(codes: List<ScriptCode>) {
        this.scripts = codes
    }

    override fun evaluate(screenAnalysis: ScreenAnalysis): ScriptResult {
        // 1. Convert Kotlin Data Class to Lua Table once per evaluation cycle
        val luaScreen = screenAnalysis.toLuaTable()

        for (script in scripts) {
            try {
                // 2. Initialize a fresh environment for each script to avoid side effects
                val globals = JsePlatform.standardGlobals()
                
                // 3. Inject the screen data
                globals.set("screen", luaScreen)

                // 4. Load and execute the script
                val chunk = globals.load(script.code)
                val result = chunk.call()

                // 5. If a script matches (returns true), we stop and return Success
                if (result.isboolean() && result.toboolean()) {
                    return ScriptResult.Success(scriptName = script.name, matched = true)
                }
            } catch (e: Exception) {
                // If a specific script crashes, we report it immediately
                return ScriptResult.Error("Script '${script.name}' failed: ${e.message}")
            }
        }

        // 6. No scripts matched
        return ScriptResult.Success(scriptName = "none", matched = false)
    }

    // --- Mapper Extensions ---

    private fun ScreenAnalysis.toLuaTable(): LuaTable {
        val table = LuaTable()
        pkg?.let { table.set("pkg", LuaValue.valueOf(it)) }
        table.set("appName", LuaValue.valueOf(appName))
        table.set("nodesCount", LuaValue.valueOf(nodesCount))
        table.set("hasAppName", LuaValue.valueOf(hasAppName))
        table.set("isSettingsScreen", LuaValue.valueOf(isSettingsScreen))
        table.set("root", root.toLuaTable())
        return table
    }

    private fun ScreenNode.toLuaTable(): LuaTable {
        val table = LuaTable()
        cls?.let { table.set("cls", LuaValue.valueOf(it)) }
        text?.let { table.set("text", LuaValue.valueOf(it)) }
        id?.let { table.set("id", LuaValue.valueOf(it)) }
        desc?.let { table.set("desc", LuaValue.valueOf(it)) }
        
        val luaChildren = LuaTable()
        children.forEachIndexed { index, child ->
            luaChildren.set(index + 1, child.toLuaTable())
        }
        table.set("children", luaChildren)
        return table
    }
    companion object{

   val defaultScripts =     listOf(ScriptCode(
        name = "app info screen xiaomi",
        code = """
        local function containsText(node, targetText)
            if not node then return false end
            
            if (node.text and string.find(node.text, targetText)) or 
               (node.desc and string.find(node.desc, targetText)) then
                return true
            end
            
            if node.children then
                for _, child in ipairs(node.children) do
                    if containsText(child, targetText) then
                        return true
                    end
                end
            end
            
            return false
        end

        local function evaluate()
            if not screen.hasAppName or not screen.isSettingsScreen then
                return false
            end
            
            local hints = {"App info", "App details"}
            local hasHint = false
            
            for _, hint in ipairs(hints) do
                if containsText(screen.root, hint) then
                    hasHint = true
                    break -- Stop searching if we found one
                end
            end
            
            return hasHint
        end

        local status, result = pcall(evaluate)
        return status and result
    """.trimIndent()
        ))
    }
}