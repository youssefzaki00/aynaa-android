package com.mafazaa.ainaa.domain.repo

import com.mafazaa.ainaa.data.models.NetworkResult
import com.mafazaa.ainaa.data.models.ReportModel
import com.mafazaa.ainaa.data.models.VersionModel
import com.mafazaa.ainaa.domain.models.UninstallRequest
import kotlinx.coroutines.flow.Flow
import java.io.File

interface RemoteRepo {
    fun submitReportToGoogleForm(reportModel: ReportModel): Flow<NetworkResult>
    suspend fun getLatestVersion(): VersionModel?
    suspend fun downloadFile(
        url: String,
        file: File,
    ): Boolean

    suspend fun getAllBlockedWords(): List<String>?

    suspend fun authenticate(deviceId: String): String?

    suspend fun getExcludedApps(): List<String>?

    suspend fun getApps(lastSync: String? =null): List<String>?

    suspend fun createUninstallRequest(reason: String): String?

    suspend fun updateUninstallRequest(id: String, reason: String): Boolean

    suspend fun getUninstallRequests(): List<UninstallRequest>?
}
