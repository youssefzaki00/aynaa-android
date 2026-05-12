package com.mafazaa.ainaa.data

import com.mafazaa.ainaa.domain.models.ScreenAnalysis

data class BuiltInChecker(
    val name: String,
    val check: (screenAnalysis: ScreenAnalysis) -> Boolean
){
    companion object{

        val checkers = listOf(
            BuiltInChecker("uninstall screen xiaomi") { screen ->
                screen.hasAppName &&
                        screen.pkg == "com.google.android.packageinstaller" &&
                        screen.root.children.size == 6
            },

            BuiltInChecker("Overlay screen xiaomi") { screen ->
                screen.hasAppName &&
                        screen.isSettingsScreen &&
                        screen.nodesCount == 15
            },

            BuiltInChecker("app info screen xiaomi") { screen ->
                if (!screen.hasAppName || !screen.isSettingsScreen) return@BuiltInChecker false

                val hints = listOf("App info", "App details")
                hints.any { hint ->
                    screen.root.hasTextOrDescContaining( hint)
                }
            },

            BuiltInChecker("battery pop up xiaomi") { screen ->
                val children = screen.root.children
                if (children.size != 5) return@BuiltInChecker false

                children[0].cls == "android.widget.TextView" &&
                        children[1].cls == "android.widget.TextView" &&
                        children[2].cls == "android.widget.CheckBox" &&
                        children[3].cls == "android.widget.Button" &&
                        children[4].cls == "android.widget.Button"
            },

            BuiltInChecker("background apps dialog xiaomi") { screen ->
                if (!screen.hasAppName) return@BuiltInChecker false
                if (screen.nodesCount > 50) return@BuiltInChecker false
                screen.root.hasTextOrDescContaining( "fgs_manager_app_item_label")
            },

            BuiltInChecker("settings app info screen samsung") { screen ->
                if (screen.pkg == "com.samsung.accessibility") return@BuiltInChecker true
                if (screen.nodesCount != 16) return@BuiltInChecker false
                screen.hasAppName && screen.isSettingsScreen
            },

            BuiltInChecker("device admin xiaomi") { screen ->
                screen.hasAppName &&
                        screen.isSettingsScreen &&
                        screen.nodesCount == 13
            },

            BuiltInChecker("background apps dialog xiaomi 2") { screen ->
                screen.hasAppName &&
                        screen.isSettingsScreen &&
                        screen.nodesCount == 18
            },

            BuiltInChecker("safe zone screen realme") { screen ->
                val root = screen.root
                if (screen.nodesCount != 13) return@BuiltInChecker false
                if (screen.pkg != "com.android.systemui") return@BuiltInChecker false
                root.children.size == 2
            },

            BuiltInChecker("device admin realme") { screen ->
                try {
                    screen.hasAppName && screen.isSettingsScreen && screen.nodesCount != 18
                } catch (e: Exception) {
                    false
                }
            },

            BuiltInChecker("device admin samsung") { screen ->
                screen.hasAppName && screen.isSettingsScreen && (screen.nodesCount != 8 && screen.nodesCount != 18)
            },

            BuiltInChecker("uninstall screen realme") { screen ->
                screen.nodesCount == 9 && screen.hasAppName
            },

            BuiltInChecker("uninstall popup samsung") { screen ->
                val root = screen.root
                if (!screen.hasAppName) return@BuiltInChecker false

                val children = root.children
                if (children.size < 3) return@BuiltInChecker false

                val title = children[0]
                val message = children[1]
                val panel = children[2]

                if (title.cls != "android.widget.TextView") return@BuiltInChecker false
                if (message.cls != "android.widget.TextView") return@BuiltInChecker false
                if (panel.cls != "android.widget.ScrollView") return@BuiltInChecker false

                val panelChildren = panel.children
                val buttonCount = panelChildren.count { it.cls == "android.widget.Button" }
                buttonCount >= 2
            }
        )
    }
}
