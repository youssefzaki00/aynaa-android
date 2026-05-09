package com.mafazaa.ainaa.data.data_source

import android.content.SharedPreferences
import com.mafazaa.ainaa.data.local.delegates

interface LocalContentRepo {
    var  blockedWords: Set<String>
}

