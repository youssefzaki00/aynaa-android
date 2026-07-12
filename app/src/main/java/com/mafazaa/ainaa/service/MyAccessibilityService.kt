package com.mafazaa.ainaa.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.data.AntiDisableKotlinChecker
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.domain.DomainNamesManager
import com.mafazaa.ainaa.domain.models.BlockReason
import com.mafazaa.ainaa.domain.models.ScreenAnalysis
import com.mafazaa.ainaa.domain.models.ScreenNode
import com.mafazaa.ainaa.domain.models.ScriptResult
import com.mafazaa.ainaa.domain.repo.ContentRepo
import com.mafazaa.ainaa.domain.repo.AntiDisableRepo
import com.mafazaa.ainaa.helpers.DeviceUtils
import com.mafazaa.ainaa.helpers.LockOverlayManager
import com.mafazaa.ainaa.helpers.ScreenAnalyser
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.MyLog.logUiTree
import com.mafazaa.ainaa.utils.createNotification
import com.mafazaa.ainaa.utils.isKeyguardSecure
import com.mafazaa.ainaa.utils.shareFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import java.util.Arrays
import kotlin.system.measureTimeMillis
import kotlin.time.measureTimedValue

@SuppressLint("AccessibilityPolicy")
class MyAccessibilityService : AccessibilityService() {
    val lockOverlayManager: LockOverlayManager by inject(LockOverlayManager::class.java)
    internal val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    internal val sharedPrefs: SharedPrefs by inject(SharedPrefs::class.java)
    private val antiDisableRepo: AntiDisableRepo by inject(AntiDisableRepo::class.java)
    private val contentRepo: ContentRepo by inject(ContentRepo::class.java)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand called ${intent?.action}")
        when (intent?.action) {
            ACTION_START_FOREGROUND -> {
                startForeground(
                    NOTIFICATION_ID, createNotification()
                )
                MyLog.i(TAG, "Accessibility Service started and moved to foreground.")
                isPaused.value = false

            }

            ACTION_START -> {
                isPaused.value = false
                MyLog.i(TAG, "Accessibility Service started and moved to foreground.")
            }

            ACTION_STOP -> {
                MyLog.i(TAG, "Accessibility Service stopped.")
                isPaused.value = true
                isStopped.value = true
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()

            }

            ACTION_SHARE_CURRENT_SCREEN -> {
                serviceScope.launch {
                    val screenAnalysis = ScreenAnalyser.analyzeScreen(
                        rootInActiveWindow, getString(R.string.app_name)
                    )
                    shareFile(logUiTree("screenShot", screenAnalysis))
                }
            }

            else -> {
                MyLog.w(TAG, "Unknown action received: ${intent?.action}")
            }
        }
        return START_STICKY
    }

    companion object {
        fun Context.startAccessibilityService(action: String = ACTION_START_FOREGROUND) {
            val intent = Intent(this, MyAccessibilityService::class.java).apply {
                this@apply.action = action
            }
            startService(intent)
        }

        val isPaused = MutableStateFlow(true)
        val isStopped = MutableStateFlow(false)
        const val ACTION_STOP = "STOP_ACCESSIBILITY"
        const val ACTION_START = "START_ACCESSIBILITY"

        const val ACTION_START_FOREGROUND = "START_ACCESSIBILITY_FOREGROUND"
        const val ACTION_SHARE_CURRENT_SCREEN = "SHARE_CURRENT_SCREEN"
        internal const val NOTIFICATION_ID = 101 // Unique ID for the notification
        internal const val NOTIFICATION_CHANNEL_ID = "AINAA_PROTECTION_CHANNEL"

        const val TAG = "MyAccessibilityService"

        private val SETTINGS_PACKAGE = DeviceUtils.settingsPackageName
        private val ACCESSIBILITY_SETTINGS =
            "${SETTINGS_PACKAGE}.accessibility.AccessibilitySettings"

        private fun isLikelyKeyboardPackage(packageName: String): Boolean {
            val keyboardPackageHints = listOf(
                "inputmethod",
                "keyboard"
            )
            return keyboardPackageHints.any { packageName.contains(it, ignoreCase = true) }
        }
    }

    override fun onCreate() {
        super.onCreate()

        MyLog.i(TAG, "Accessibility Service created.")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info
        MyLog.i(TAG, "Accessibility Service connected.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Return early if event is null or service is not running
        event ?: return
        val currentPackageName = event.packageName ?: return
        if (isPaused.value || isStopped.value) return
        if (currentPackageName == packageName) return



        // Only handle window content changed events
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return
        }


        rootInActiveWindow?.let { rootNode ->
            serviceScope.launch {
                // Analyze the current screen and measure the time taken
                val (analysisResult, analysisDuration) = measureTimedValue {
                    ScreenAnalyser.analyzeScreen(rootNode, getString(R.string.app_name))
                }
                Log.d(
                    TAG,
                    "Screen analyzed in ${analysisDuration.inWholeMilliseconds}ms, nodes=${analysisResult.nodesCount}"
                )
                val currentPackage = analysisResult.pkg
                val t = measureTimeMillis {
                    AntiDisableKotlinChecker.checkers.firstOrNull { it.check(analysisResult) }
                        ?.let {
                            block(BlockReason.TryingToDisable(it.name, analysisResult))
                            return@launch
                        }
                }
                Log.d(TAG, "Built-in checkers evaluated in ${t}ms")
                // Evaluate scripts and measure the time taken
                val (scriptResult, scriptEvalDuration) = measureTimedValue {
                    antiDisableRepo.evaluate(
                        analysisResult
                    )
                }
                when (scriptResult) {
                    is ScriptResult.Error -> {
                        MyLog.e(TAG, "Script evaluation error: ${scriptResult.error}")
                    }

                    is ScriptResult.Success -> {
                        if (scriptResult.matched) {
                            MyLog.i(
                                TAG,
                                "Blocking due to script match: ${scriptResult.scriptName} on ${analysisResult.pkg}"
                            )
                            block(
                                BlockReason.TryingToDisable(
                                    scriptResult.scriptName, analysisResult
                                )
                            )
                            return@launch
                        }
                    }
                }
                Log.d(TAG, "Script evaluated in ${scriptEvalDuration.inWholeMilliseconds}ms")
                // Skip excluded apps
                val excludedApps = contentRepo.excludedAppsStatus.value
                if (currentPackageName.toString() in excludedApps || isLikelyKeyboardPackage(currentPackageName.toString())) {
                    return@launch
                }

                if (checkBlockedApp(currentPackage)) {
                    MyLog.i(TAG, "Blocked app in use: $currentPackage")
                    block(BlockReason.UsingBlockedApp(currentPackage ?: "unknown"))
                    return@launch
                }
                checkBlockedWords(analysisResult)?.let { blockedWord ->
                    MyLog.i(TAG, "Blocked word detected: $blockedWord")
                    block(blockedWord)
                    return@launch
                }
                checkBlockedDomains(analysisResult)?.let { blockedDomain ->
                    MyLog.i(TAG, "Blocked domain detected: $blockedDomain")
                    block(blockedDomain)
                    return@launch
                }

            }
        }
    }

    fun MyAccessibilityService.block(reason: BlockReason) {
        this.serviceScope.launch(Dispatchers.Main) {
            lockOverlayManager.showOverlay(reason)
        }
        this.performGlobalAction(GLOBAL_ACTION_BACK)
    }


    fun MyAccessibilityService.checkBlockedApp(currentApp: String?): Boolean {
//        if (!this.isKeyguardSecure()) todo
//            return false
        if (currentApp == null) return false

        
        return currentApp in sharedPrefs.blockedApps ||
               currentApp in contentRepo.remoteBlockedAppsStatus.value
    }

    suspend fun MyAccessibilityService.checkBlockedDomains(screenAnalysis: ScreenAnalysis): BlockReason.BlockedSiteDetected? {
        return withContext(Dispatchers.Default) {
            val t = measureTimeMillis {

                val stack = mutableListOf<ScreenNode>()
                stack.add(screenAnalysis.root)
                var nodesChecked = 0

                while (stack.isNotEmpty()) {
                    val node = stack.removeAt(stack.size - 1)

                    val nodeText = node.text ?: ""
                    if (nodeText.isNotBlank()) {
                        // Extract possible domains from the node text
                        val detectedDomain = findBlockedDomainInText(nodeText)
                        if (detectedDomain != null) {
                            MyLog.i(
                                TAG,
                                "Blocked domain '$detectedDomain' found in node text: '$nodeText'"
                            )
                            return@withContext BlockReason.BlockedSiteDetected(
                                domain = detectedDomain, sentence = nodeText
                            )
                        }
                    }

                    // Add children to stack for DFS traversal
                    stack.addAll(node.children)
                    nodesChecked++


                }
            }
            Log.d(TAG, "Domain blocking completed in ${t}ms")
            return@withContext null
        }
    }

    /**
     * Splits and cleans the text to find if any token matches a blocked domain in the Trie.
     */
    private fun findBlockedDomainInText(text: String): String? {
        // Split text by spaces to evaluate individual words/URLs
        val tokens = text.split(Regex("\\s+"))

        for (token in tokens) {
            if (token.isBlank()) continue

            // Clean up common URL structures to extract just the host domain
            var cleanToken = token.trim().lowercase()

            if (cleanToken.startsWith("http://")) cleanToken = cleanToken.substring(7)
            if (cleanToken.startsWith("https://")) cleanToken = cleanToken.substring(8)


            // Remove trailing paths, queries, or ports (e.g., "example.com/path?query=1" -> "example.com")
            val slashIndex = cleanToken.indexOf('/')
            if (slashIndex != -1) {
                cleanToken = cleanToken.substring(0, slashIndex)
            }
            val colonIndex = cleanToken.indexOf(':')
            if (colonIndex != -1) {
                cleanToken = cleanToken.substring(0, colonIndex)
            }

            // Basic sanity check: valid domains usually contain at least one dot
            if (cleanToken.contains(".") && DomainNamesManager.isBlocked(cleanToken)) {
                return cleanToken // Return the blocked domain found
            }
        }
        return null
    }

    suspend fun MyAccessibilityService.checkBlockedWords(screenAnalysis: ScreenAnalysis): BlockReason.BlockedWordDetected? {
        val maxNodes = 500
        val blockedWordsList = contentRepo.blockedWordsStatus.value
        if (blockedWordsList.isEmpty()) {
            return null
        }
        return withContext(Dispatchers.Default) {
            for (word in blockedWordsList) {
                val stack = mutableListOf<ScreenNode>()
                stack.add(screenAnalysis.root)
                var nodesChecked = 0
                while (stack.isNotEmpty()) {
                    val node = stack.removeAt(stack.size - 1)
                    val nodeText = node.text ?: ""
                    if (nodeText.split(" ").any { it.equals(word, true) }) {
                        MyLog.i(
                            TAG, "Blocked word '$word' found in node text: '$nodeText'"
                        )
                        return@withContext BlockReason.BlockedWordDetected(
                            word, nodeText,screenAnalysis.pkg.toString()
                        )
                    }
                    stack.addAll(node.children)
                    nodesChecked++
                    if (nodesChecked > maxNodes) {
                        MyLog.d(
                            TAG, "Max nodes checked ($maxNodes), stopping search for blocked words."
                        )
                        break
                    }
                }
            }
            return@withContext null
        }
    }

    override fun onInterrupt() {
        serviceScope.cancel()
        MyLog.w(TAG, "Service interrupted")

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        MyLog.d(TAG, "Restart broadcast sent")
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        MyLog.i(TAG, "Accessibility Service destroyed.")
        serviceScope.cancel()

    }
}
