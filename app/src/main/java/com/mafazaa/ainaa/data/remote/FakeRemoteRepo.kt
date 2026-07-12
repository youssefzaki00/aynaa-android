package com.mafazaa.ainaa.data.remote

import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.domain.models.UninstallRequest
import com.mafazaa.ainaa.domain.repo.RemoteRepo

/**
 * we use this fake repo in debug builds to avoid actual forms submissions
 * but the update check and download is real
 */
class FakeRemoteRepo(private val sharedPrefs: SharedPrefs) : RemoteRepo by KtorRepo(sharedPrefs) {
    override suspend fun authenticate(deviceId: String): String? {
        return "fake_token_for_debug"
    }

    override suspend fun createUninstallRequest(reason: String): String? {
        return "fake_request_id"
    }

    override suspend fun updateUninstallRequest(id: String, reason: String): Boolean {
        return true
    }

    override suspend fun getUninstallRequests(): List<UninstallRequest>? {
        return emptyList()
    }
}

