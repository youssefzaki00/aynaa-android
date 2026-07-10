package com.mafazaa.ainaa.data

import android.util.Log
import com.mafazaa.ainaa.data.data_source.LocalContentRepo
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.data.local.add
import com.mafazaa.ainaa.data.local.remove
import com.mafazaa.ainaa.domain.repo.ContentRepo
import com.mafazaa.ainaa.domain.repo.RemoteRepo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class ContentRepoImpl(
    private val localContentRepo: LocalContentRepo,
    private val remoteRepo: RemoteRepo,
) : ContentRepo {
    var lastTimeChecked: Long = 0
    var lastTimeExcludedAppsChecked: Long = 0
    var lastTimeRemoteBlockedAppsChecked: Long = 0

    private val _blockedWordsStatus = MutableStateFlow(localContentRepo.blockedWords.toList())
    override val blockedWordsStatus: StateFlow<List<String>> = _blockedWordsStatus.asStateFlow()

    private val _excludedAppsStatus = MutableStateFlow(localContentRepo.excludedApps.toList())
    override val excludedAppsStatus: StateFlow<List<String>> = _excludedAppsStatus.asStateFlow()

    private val _remoteBlockedAppsStatus = MutableStateFlow(localContentRepo.remoteBlockedApps.toList())
    override val remoteBlockedAppsStatus: StateFlow<List<String>> = _remoteBlockedAppsStatus.asStateFlow()

    override fun getBlockedWords() {
        Log.d("ContentRepoImpl", "getBlockedWords called")
        if (System.currentTimeMillis() - lastTimeChecked <= checkEvery) return
        lastTimeChecked = System.currentTimeMillis()
        GlobalScope.launch {//todo enhance
            val blockedWords = remoteRepo.getAllBlockedWords() ?: emptyList()
            localContentRepo.blockedWords = blockedWords.toMutableSet()
            _blockedWordsStatus.value = localContentRepo.blockedWords.toList()
            Log.d("ContentRepoImpl", "Fetched new blocked words: ${blockedWords.size}")
        }
    }

    override fun getExcludedApps() {
     Log.d("ContentRepoImpl", "getExcludedApps called")
        if (System.currentTimeMillis() - lastTimeExcludedAppsChecked <= checkEvery) return
        lastTimeExcludedAppsChecked = System.currentTimeMillis()
        GlobalScope.launch {
            val excludedApps = remoteRepo.getExcludedApps() ?: emptyList()
            localContentRepo.excludedApps = excludedApps.toMutableSet()
            _excludedAppsStatus.value = localContentRepo.excludedApps.toList()
            Log.d("ContentRepoImpl", "Fetched new excluded apps: ${excludedApps.size}")
        }
    }

    override fun getRemoteBlockedApps() {
        Log.d("ContentRepoImpl", "getRemoteBlockedApps called")
        if (System.currentTimeMillis() - lastTimeRemoteBlockedAppsChecked <= checkEvery) return
        lastTimeRemoteBlockedAppsChecked = System.currentTimeMillis()
        GlobalScope.launch {
            val remoteBlockedApps = remoteRepo.getApps() ?: emptyList()
            localContentRepo.remoteBlockedApps = remoteBlockedApps.toMutableSet()
            _remoteBlockedAppsStatus.value = localContentRepo.remoteBlockedApps.toList()
            Log.d("ContentRepoImpl", "Fetched new remote blocked apps: ${remoteBlockedApps.size}")
        }
    }


    override fun removeKeyWord(word:String) {
        localContentRepo.blockedWords=localContentRepo.blockedWords.remove(word)
        _blockedWordsStatus.value = localContentRepo.blockedWords.toList()
    }

    override fun addKeyWord(word: String) {
        localContentRepo.blockedWords=localContentRepo.blockedWords.add(word)
        _blockedWordsStatus.value = localContentRepo.blockedWords.toList()
    }

    companion object{
        const val checkEvery = 12 * 60 * 60 * 1000L
    }
}