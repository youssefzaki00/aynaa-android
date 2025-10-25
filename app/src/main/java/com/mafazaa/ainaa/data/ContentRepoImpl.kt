package com.mafazaa.ainaa.data

import com.mafazaa.ainaa.data.data_source.LocalContentRepo
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.data.local.add
import com.mafazaa.ainaa.data.local.remove
import com.mafazaa.ainaa.domain.repo.ContentRepo
import com.mafazaa.ainaa.domain.repo.RemoteRepo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ContentRepoImpl(
    private val localContentRepo: LocalContentRepo,
    private val remoteRepo: RemoteRepo,
) : ContentRepo {
    var lastTimeChecked: Long = 0

    override fun getBlockedWords(): List<String> {
        android.util.Log.d("ContentRepoImpl", "getBlockedWords called")
        if (System.currentTimeMillis() - lastTimeChecked > checkEvery) {
            lastTimeChecked = System.currentTimeMillis()
            GlobalScope.launch {//todo enhance
                val blockedWords = remoteRepo.getAllBlockedWords() ?: emptyList()
                localContentRepo.blockedWords = blockedWords.toMutableSet()
                android.util.Log.d("ContentRepoImpl", "Fetched new blocked words: ${blockedWords.size}")
            }
        }
        return localContentRepo.blockedWords.toList()
    }

    override fun removeKeyWord(word:String) {
        localContentRepo.blockedWords=localContentRepo.blockedWords.remove(word)
    }

    override fun addKeyWord(word: String) {
        localContentRepo.blockedWords=localContentRepo.blockedWords.add(word)
    }

    companion object{
        const val checkEvery = 12 * 60 * 60 * 1000L
    }
}