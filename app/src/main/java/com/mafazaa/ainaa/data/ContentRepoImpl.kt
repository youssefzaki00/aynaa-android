package com.mafazaa.ainaa.data

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

    private val _blockedWordsStatus = MutableStateFlow<List<String>>(localContentRepo.blockedWords.toList())
    override val blockedWordsStatus: StateFlow<List<String>> = _blockedWordsStatus.asStateFlow()

    override fun getBlockedWords() {
        android.util.Log.d("ContentRepoImpl", "getBlockedWords called")
        if (System.currentTimeMillis() - lastTimeChecked > checkEvery) {
            lastTimeChecked = System.currentTimeMillis()
            GlobalScope.launch {//todo enhance
                val blockedWords = remoteRepo.getAllBlockedWords() ?: emptyList()
                localContentRepo.blockedWords = blockedWords.toMutableSet()
                _blockedWordsStatus.value = localContentRepo.blockedWords.toList()
                android.util.Log.d("ContentRepoImpl", "Fetched new blocked words: ${blockedWords.size}")
            }
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