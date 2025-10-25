package com.mafazaa.ainaa.domain.repo

import com.mafazaa.ainaa.data.local.SharedPrefs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface ContentRepo {
    fun getBlockedWords(): List<String>
    fun removeKeyWord(word: String)
    fun addKeyWord(word: String)
}

