package com.mafazaa.ainaa.data

import android.util.Log
import com.mafazaa.ainaa.data.data_source.LocalContentRepo
import com.mafazaa.ainaa.data.local.add
import com.mafazaa.ainaa.data.local.remove
import com.mafazaa.ainaa.domain.repo.ContentRepo
import com.mafazaa.ainaa.domain.repo.RemoteRepo
import com.mafazaa.ainaa.utils.MyLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow

class ContentRepoImpl(
    private val localContentRepo: LocalContentRepo,
    private val remoteRepo: RemoteRepo,
) : ContentRepo {
    var lastTimeBlockedWordsChecked: Long = 0
    var lastTimeExcludedAppsChecked: Long = 0
    var lastTimeRemoteBlockedAppsChecked: Long = 0

    private val _blockedWordsStatus = MutableStateFlow(localContentRepo.blockedWords.toList())
    override val blockedWordsStatus: StateFlow<List<String>> = _blockedWordsStatus.asStateFlow()

    private val _excludedAppsStatus = MutableStateFlow(localContentRepo.excludedApps.toList())
    override val excludedAppsStatus: StateFlow<List<String>> = _excludedAppsStatus.asStateFlow()

    private val _remoteBlockedAppsStatus =
        MutableStateFlow(localContentRepo.remoteBlockedApps.toList())
    override val remoteBlockedAppsStatus: StateFlow<List<String>> =
        _remoteBlockedAppsStatus.asStateFlow()

    override suspend fun getBlockedWords() {
        Log.d(TAG, "getBlockedWords")
        if (System.currentTimeMillis() - lastTimeBlockedWordsChecked <= checkEvery) return
        val lastSize = localContentRepo.blockedWords.size
            val blockedWords = remoteRepo.getAllBlockedWords()
            if (blockedWords == null) {
                MyLog.d(TAG, "Failed to fetch blocked words")
                return
            }
            lastTimeBlockedWordsChecked = System.currentTimeMillis()
            localContentRepo.blockedWords = blockedWords.toMutableSet()
            _blockedWordsStatus.value = localContentRepo.blockedWords.toList()
            if (lastSize != blockedWords.size) {
                MyLog.d(TAG, "Fetched new blocked words: ${blockedWords.size}")
        }
    }

    override suspend fun getExcludedApps() {
        Log.d(TAG, "getExcludedApps")
        if (System.currentTimeMillis() - lastTimeExcludedAppsChecked <= checkEvery) return
        val lastSize = localContentRepo.excludedApps.size
            val excludedApps = remoteRepo.getExcludedApps()
            if (excludedApps == null) {
                MyLog.d(TAG, "Failed to fetch excluded apps")
                return
            }
            lastTimeExcludedAppsChecked = System.currentTimeMillis()
            localContentRepo.excludedApps = excludedApps.toMutableSet()
            _excludedAppsStatus.value = localContentRepo.excludedApps.toList()
            if (lastSize != excludedApps.size) {
                MyLog.d(TAG, "Fetched new excluded apps: ${excludedApps.size}")
            }
    }

    override suspend fun getRemoteBlockedApps() {
        Log.d(TAG, "getRemoteBlockedApps")
        if (System.currentTimeMillis() - lastTimeRemoteBlockedAppsChecked <= checkEvery) return
        val lastSize = localContentRepo.remoteBlockedApps.size
            val remoteBlockedApps = remoteRepo.getApps()
            if (remoteBlockedApps == null) {
                MyLog.d(TAG, "Failed to fetch remote blocked apps")
                return
            }
            lastTimeRemoteBlockedAppsChecked = System.currentTimeMillis()
            localContentRepo.remoteBlockedApps = remoteBlockedApps.toMutableSet()
            _remoteBlockedAppsStatus.value = localContentRepo.remoteBlockedApps.toList()
            if (lastSize != remoteBlockedApps.size) {
                MyLog.d(TAG, "Fetched new remote blocked apps: ${remoteBlockedApps.size}")
            }
    }


    override fun removeKeyWord(word: String) {
        localContentRepo.blockedWords = localContentRepo.blockedWords.remove(word)
        _blockedWordsStatus.value = localContentRepo.blockedWords.toList()
    }

    override fun addKeyWord(word: String) {
        localContentRepo.blockedWords = localContentRepo.blockedWords.add(word)
        _blockedWordsStatus.value = localContentRepo.blockedWords.toList()
    }

    companion object {
        const val TAG = "ContentRepoImpl"
        const val checkEvery = 12 * 60 * 60 * 1000L
    }
}