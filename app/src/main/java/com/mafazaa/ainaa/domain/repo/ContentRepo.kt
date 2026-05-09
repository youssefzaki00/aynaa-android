package com.mafazaa.ainaa.domain.repo

import com.mafazaa.ainaa.data.local.SharedPrefs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ContentRepo {
    val blockedWordsStatus: StateFlow<List<String>>
    fun getBlockedWords()
    fun removeKeyWord(word: String)
    fun addKeyWord(word: String)
}
