package com.mafazaa.ainaa.data.data_source

import android.content.SharedPreferences
import com.mafazaa.ainaa.data.local.delegates

class LocalContentImpl(sharedPreferences: SharedPreferences) :LocalContentRepo{

    override var blockedWords: Set<String> by sharedPreferences.delegates.stringSet()
    override var excludedApps: Set<String> by sharedPreferences.delegates.stringSet()
    override var remoteBlockedApps: Set<String> by sharedPreferences.delegates.stringSet()
}
