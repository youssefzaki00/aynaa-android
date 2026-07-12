package com.mafazaa.ainaa.domain.repo

import com.mafazaa.ainaa.data.local.SharedPrefs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ContentRepo {
    val blockedWordsStatus: StateFlow<List<String>>
    val excludedAppsStatus: StateFlow<List<String>>
    val remoteBlockedAppsStatus: StateFlow<List<String>>
    suspend   fun getBlockedWords()
    suspend   fun getExcludedApps()
    suspend  fun getRemoteBlockedApps()
    fun removeKeyWord(word: String)
    fun addKeyWord(word: String)
}
