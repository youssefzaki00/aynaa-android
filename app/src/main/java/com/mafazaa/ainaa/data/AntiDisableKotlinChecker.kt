package com.mafazaa.ainaa.data

import com.mafazaa.ainaa.domain.models.ScreenAnalysis

data class AntiDisableKotlinChecker(
    val name: String,
    val check: (screenAnalysis: ScreenAnalysis) -> Boolean
){
    companion object{

        val checkers = listOf(
            AntiDisableKotlinChecker("uninstall screen xiaomi") { screen ->
                screen.hasAppName &&
                        screen.pkg == "com.google.android.packageinstaller" &&
                        screen.root.children.size == 6
            },

            AntiDisableKotlinChecker("Overlay screen xiaomi") { screen ->
                screen.hasAppName &&
                        screen.isSettingsScreen &&
                        screen.nodesCount == 15
            },

            AntiDisableKotlinChecker("app info screen xiaomi") { screen ->
                if (!screen.hasAppName || !screen.isSettingsScreen) return@AntiDisableKotlinChecker false

                val hints = listOf("App info", "App details")
                hints.any { hint ->
                    screen.root.hasTextOrDescContaining( hint)
                }
            },

            AntiDisableKotlinChecker("battery pop up xiaomi") { screen ->
                val children = screen.root.children
                if (children.size != 5) return@AntiDisableKotlinChecker false

                children[0].cls == "android.widget.TextView" &&
                        children[1].cls == "android.widget.TextView" &&
                        children[2].cls == "android.widget.CheckBox" &&
                        children[3].cls == "android.widget.Button" &&
                        children[4].cls == "android.widget.Button"
            },

            AntiDisableKotlinChecker("background apps dialog xiaomi") { screen ->
                if (!screen.hasAppName) return@AntiDisableKotlinChecker false
                if (screen.nodesCount > 50) return@AntiDisableKotlinChecker false
                screen.root.hasTextOrDescContaining( "fgs_manager_app_item_label")
            },

            AntiDisableKotlinChecker("settings app info screen samsung") { screen ->
                if (screen.pkg == "com.samsung.accessibility") return@AntiDisableKotlinChecker true
                if (screen.nodesCount != 16) return@AntiDisableKotlinChecker false
                screen.hasAppName && screen.isSettingsScreen
            },

            AntiDisableKotlinChecker("device admin xiaomi") { screen ->
                screen.hasAppName &&
                        screen.isSettingsScreen &&
                        screen.nodesCount == 13
            },

            AntiDisableKotlinChecker("background apps dialog xiaomi 2") { screen ->
                screen.hasAppName &&
                        screen.isSettingsScreen &&
                        screen.nodesCount == 18
            },

            AntiDisableKotlinChecker("safe zone screen realme") { screen ->
                    val targetId = "com.android.systemui:id/sec_global_actions_icon"
                    val baseNode = screen.root.children.getOrNull(0)?.children?.getOrNull(0)
                    baseNode?.children?.getOrNull(0)?.children?.getOrNull(0)?.id == targetId &&
                            baseNode.children.getOrNull(1)?.children?.getOrNull(0)?.id == targetId &&
                            baseNode.children.getOrNull(2)?.children?.getOrNull(0)?.id == targetId &&
                            baseNode.children.getOrNull(3)?.children?.getOrNull(0)?.id == targetId

            },

            AntiDisableKotlinChecker("device admin realme") { screen ->

                    screen.hasAppName && screen.isSettingsScreen && !isAppsScreen(screen)

            },

            AntiDisableKotlinChecker("device admin samsung") { screen ->
                if (!screen.hasAppName || !screen.isSettingsScreen) return@AntiDisableKotlinChecker false

                return@AntiDisableKotlinChecker !isAppsScreen(screen)
            },

            AntiDisableKotlinChecker("uninstall screen realme") { screen ->
                screen.nodesCount == 9 && screen.hasAppName && !isLastAppsScreen(screen)
            },

            AntiDisableKotlinChecker("uninstall popup samsung") { screen ->
                val root = screen.root
                if (!screen.hasAppName) return@AntiDisableKotlinChecker false

                val children = root.children
                if (children.size < 3) return@AntiDisableKotlinChecker false

                val title = children[0]
                val message = children[1]
                val panel = children[2]

                if (title.cls != "android.widget.TextView") return@AntiDisableKotlinChecker false
                if (message.cls != "android.widget.TextView") return@AntiDisableKotlinChecker false
                if (panel.cls != "android.widget.ScrollView") return@AntiDisableKotlinChecker false

                val panelChildren = panel.children
                val buttonCount = panelChildren.count { it.cls == "android.widget.Button" }
                buttonCount >= 2
            }
        )
    }
}

private fun isAppsScreen(screen: ScreenAnalysis): Boolean {
    return try {

    val rootChildren = screen.root.children
    if (rootChildren.isEmpty()) return false

    val firstChild = rootChildren[0]
    if (firstChild.children.size < 2) return false

    return firstChild.children[1].id == "com.android.settings:id/recycler_view" &&
            firstChild.children[0].id == "com.android.settings:id/collapsing_toolbar"
    }catch  (_:Exception){
        false
    }
}

private fun isLastAppsScreen(screen: ScreenAnalysis): Boolean {
    return try {
        if (screen.pkg != "com.google.android.apps.nexuslauncher") return false
        if (screen.nodesCount != 9) return false

        val rootChildren = screen.root.children
        if (rootChildren.size != 3) return false

        val overviewPanel = rootChildren[0]
        val screenshotButton = rootChildren[1]
        val selectButton = rootChildren[2]

        if (overviewPanel.cls != "android.widget.ListView") return false
        if (overviewPanel.id != "com.google.android.apps.nexuslauncher:id/overview_panel") return false
        if (overviewPanel.children.isEmpty()) return false

        val firstOverviewChild = overviewPanel.children[0]
        if (firstOverviewChild.cls != "android.widget.Button") return false
        if (firstOverviewChild.id != "com.google.android.apps.nexuslauncher:id/clear_all") return false
        if (firstOverviewChild.text != "Clear all") return false

        screenshotButton.cls == "android.widget.Button" &&
                screenshotButton.id == "com.google.android.apps.nexuslauncher:id/action_screenshot" &&
                selectButton.cls == "android.widget.Button" &&
                selectButton.id == "com.google.android.apps.nexuslauncher:id/action_select"
    } catch (_: Exception) {
        false
    }
}

