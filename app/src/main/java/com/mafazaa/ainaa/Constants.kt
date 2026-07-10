package com.mafazaa.ainaa

import com.mafazaa.ainaa.domain.models.ScriptCode

object Constants {
    const val SECRET_COMBO="tttbbt"
    const val SUPPORT_URL = "https://ainaa.mafazaa.com/support_us"
    const val JOIN_URL = "https://www.mafazaa.com/join"
    const val SUPPORT_CONTACT_URL = "https://ainaa.mafazaa.com/support"
    const val SAFE_SEARCH_URL = "https://google.com/safesearch"
    const val evaluationDelay = 300L
    const val BASE_URL="https://api.aynaa.org/api/v1/"
    val socialMediaPackages = listOf(
        "com.facebook.katana",        // Facebook
        "com.facebook.lite",          // Facebook Lite
        "com.instagram.android",      // Instagram
        "com.facebook.orca",          // Messenger
        "com.whatsapp",               // WhatsApp
        "com.whatsapp.w4b",           // WhatsApp Business
        "com.zhiliaoapp.musically",   // TikTok (older/global)
        "com.ss.android.ugc.trill",   // TikTok (regional)
        "com.snapchat.android",       // Snapchat
        "org.telegram.messenger",     // Telegram
        "org.thunderdog.challegram",  // Telegram X (old)
        "com.reddit.frontpage",       // Reddit
        "com.instagram.barcelona",    // Threads
        "com.twitter.android",        // X / Twitter
        "com.discord",                // Discord
        "com.linkedin.android",       // LinkedIn
        "com.pinterest"               // Pinterest
    )

    val browserPackages = listOf(
        "com.android.chrome",                 // Chrome
        "com.chrome.beta",                    // Chrome Beta
        "com.chrome.dev",                     // Chrome Dev
        "com.chrome.canary",                  // Chrome Canary
        "com.google.android.googlequicksearchbox", // Google app browser
        "com.sec.android.app.sbrowser",       // Samsung Internet
        "org.mozilla.firefox",                // Firefox
        "org.mozilla.firefox_beta",           // Firefox Beta
        "com.opera.browser",                  // Opera
        "com.opera.mini.native",              // Opera Mini
        "com.microsoft.emmx",                 // Microsoft Edge
        "com.brave.browser",                  // Brave
        "com.duckduckgo.mobile.android",      // DuckDuckGo
        "com.vivaldi.browser"                 // Vivaldi
    )

    /**
     * See [com.mafazaa.ainaa.data.remote.KtorRepo.getLatestVersion]
     */
    const val releaseApkName = "ainaa"
    const val maxNodes = 1000//todo if screen analysis exceeds this value stop analyzing


}