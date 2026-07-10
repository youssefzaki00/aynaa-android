package com.mafazaa.ainaa.data.remote

import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.domain.repo.RemoteRepo

/**
 * we use this fake repo in debug builds to avoid actual forms submissions
 * but the update check and download is real
 */
class FakeRemoteRepo(private val sharedPrefs: SharedPrefs) : RemoteRepo by KtorRepo(sharedPrefs) {
    override suspend fun authenticate(deviceId: String): String? {
        return "fake_token_for_debug"
    }


}
