package com.mafazaa.ainaa.data.remote

import android.util.Log
import com.mafazaa.ainaa.Constants
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.data.models.NetworkResult
import com.mafazaa.ainaa.data.models.ReportModel
import com.mafazaa.ainaa.data.models.VersionModel
import com.mafazaa.ainaa.domain.repo.RemoteRepo
import com.mafazaa.ainaa.utils.MyLog
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File


class KtorRepo(
    private val sharedPrefs: SharedPrefs,
    private val client: HttpClient = HttpClient(Android) {
        install(Logging) {
            level = LogLevel.BODY
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
) : RemoteRepo {

    val reportProblemFormUrl =
        "https://docs.google.com/forms/d/e/1FAIpQLSdGddOCCrlfbCjmBhTyu38EczpW_CeBGWOqGwvaQmXv1kDNRA/formResponse"
    val reportNameEntryId = "entry.654511892"
    val reportPhoneNumberEntryId = "entry.1692603949"
    val reportEmailEntryId = "entry.742958279"
    val reportProblemEntryId = "entry.1462277143"

    val latestVersionUrl =
        "https://api.github.com/repos/mafazaa-org/Ainaa-android/releases/latest"

    /**
     * IMPORTANT:
     * we here make two assumptions:
     * 1. version code is an integer and is derived from the tag name by removing
     * the 'v' prefix. e.g. v23 -> 23
     * 2. the apk asset in the release contains the `Constants.releaseApkName`
     */
    override suspend fun getLatestVersion(): VersionModel? {
        return try {
            val client = HttpClient(Android)
            val response: HttpResponse =
                client.get(latestVersionUrl) {
                    header("Accept", "application/vnd.github+json")
                    header("X-GitHub-Api-Version", "2022-11-28")
                }
            val json = parseToJsonElement(response.bodyAsText()).jsonObject
            val tagName = json["tag_name"]?.jsonPrimitive?.content.orEmpty()
            val name = json["name"]?.jsonPrimitive?.content.orEmpty()
            val body = json["body"]?.jsonPrimitive?.content.orEmpty()
            val assets = json["assets"]?.jsonArray.orEmpty()
            val downloadAsset = assets
                .map { it.jsonObject }
                .firstOrNull { it["name"]?.jsonPrimitive?.content?.contains(Constants.releaseApkName) == true }
            val downloadUrl =
                downloadAsset?.get("browser_download_url")?.jsonPrimitive?.content.orEmpty()
            val size = downloadAsset?.get("size")?.jsonPrimitive?.longOrNull ?: 0L
            val version = tagName.removePrefix("v").toInt()
            return VersionModel(version, name, downloadUrl, body, size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun downloadFile(url: String, file: File): Boolean {
        return try {
            Log.d("KtorRepo", "Downloading file from $url to ${file.absolutePath}")
            val response: HttpResponse = client.get(url)
            if (response.status.isSuccess()) {
                val bytes = response.readRawBytes()
                file.apply {
                    parentFile?.mkdirs() // Ensure parent directories exist
                    writeBytes(bytes) // Write the downloaded bytes to the file
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun authenticate(deviceId: String): String? {
        return try {
            var response: HttpResponse = client.post("${Constants.BASE_URL}auth/signup") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("deviceId", deviceId)
                })
            }

            if (!response.status.isSuccess()) {
                val errorJson = parseToJsonElement(response.bodyAsText()).jsonObject
                val message = errorJson["message"]?.jsonPrimitive?.content
                if (message == "User already exists") {
                    MyLog.d("KtorRepo", "User already exists, attempting login")
                    response = client.post("${Constants.BASE_URL}auth/user/login") {
                        contentType(ContentType.Application.Json)
                        setBody(buildJsonObject {
                            put("deviceId", deviceId)
                        })
                    }
                }
            }

            if (response.status.isSuccess()) {
                val json = parseToJsonElement(response.bodyAsText()).jsonObject
                val token = json["data"]?.jsonObject?.get("token")?.jsonPrimitive?.content
                token
            } else {
                MyLog.e("KtorRepo", "Authentication failed: ${response.status}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getApps(lastSync: String?): List<String>? {
        if (sharedPrefs.token.isEmpty()) {
            val newToken = authenticate(sharedPrefs.deviceId.toString())
            if (newToken != null) {
                sharedPrefs.token = newToken
            } else {
                MyLog.e("KtorRepo", "Could not obtain token for apps")
                return null
            }
        }

        return try {
            val response: HttpResponse = client.get("${Constants.BASE_URL}apps") {
                header("accept", "application/json")
                header("Authorization", "Bearer ${sharedPrefs.token}")
                lastSync?.let {
                    url.parameters.append("lastSync", it)
                }
            }
            if (response.status.isSuccess()) {
                val json = parseToJsonElement(response.bodyAsText()).jsonObject
                val data = json["data"]?.jsonObject
                val apps = data?.get("apps")?.jsonArray
                val packageNames = apps?.map { appObj ->
                    appObj.jsonObject["packageName"]?.jsonPrimitive?.content.orEmpty()
                }
                packageNames
            } else {
                Log.e("KtorRepo", "Failed to fetch apps: ${response.status}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getExcludedApps(): List<String>? {
        if (sharedPrefs.token.isEmpty()) {
            val newToken = authenticate(sharedPrefs.deviceId.toString())
            if (newToken != null) {
                sharedPrefs.token = newToken
            } else {
                Log.e("KtorRepo", "Could not obtain token for excluded apps")
                return null
            }
        }

        return try {
            val response: HttpResponse = client.get("${Constants.BASE_URL}excluded-apps") {
                header("accept", "application/json")
                header("Authorization", "Bearer ${sharedPrefs.token}")
            }
            if (response.status.isSuccess()) {
                val json = parseToJsonElement(response.bodyAsText()).jsonObject
                val data = json["data"]?.jsonObject
                val excludedApps = data?.get("excludedApps")?.jsonArray
                excludedApps?.map { it.jsonObject["packageName"]?.jsonPrimitive?.content.orEmpty() }
            } else {
                Log.e("KtorRepo", "Failed to fetch excluded apps: ${response.status}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getAllBlockedWords(): List<String>? {
        val allWords = mutableListOf<String>()
        var page = 1
        var hasNextPage = true

        if (sharedPrefs.token.isEmpty()) {
            val newToken = authenticate(sharedPrefs.deviceId.toString())
            if (newToken != null) {
                sharedPrefs.token = newToken
            } else {
                Log.e("KtorRepo", "Could not obtain token")
                return null
            }
        }

        try {
            while (hasNextPage) {
                Log.d("KtorRepo", "Fetching page $page")
                val url = "${Constants.BASE_URL}keywords?page=$page&limit=100"
                val response: HttpResponse = client.get(url) {
                    header("accept", "application/json")
                    header("Authorization", "Bearer ${sharedPrefs.token}")
                }
                if (response.status.isSuccess()) {
                    val json = parseToJsonElement(response.bodyAsText()).jsonObject
                    val data = json["data"]?.jsonObject
                    val keywords = data?.get("keywords")?.jsonArray

                    keywords?.forEach { element ->
                        element.jsonObject["word"]?.jsonPrimitive?.content?.let { word ->
                            allWords.add(word)
                        }
                    }

                    val pagination = data?.get("pagination")?.jsonObject
                    hasNextPage =
                        pagination?.get("hasNextPage")?.jsonPrimitive?.booleanOrNull ?: false
                    page++
                } else {
                    hasNextPage = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null

        }
        return allWords
    }


    override fun submitReportToGoogleForm(reportModel: ReportModel): Flow<NetworkResult> = flow {
        emit(NetworkResult.Loading)
        try {
            val response: HttpResponse = client.post(reportProblemFormUrl) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(Parameters.build {
                    append(reportNameEntryId, reportModel.name)
                    append(reportPhoneNumberEntryId, reportModel.phone)
                    append(reportEmailEntryId, reportModel.email)
                    append(reportProblemEntryId, reportModel.problem)
                }.formUrlEncode())
            }

            if (!response.status.isSuccess()) {
                emit(NetworkResult.Error("Failed: ${response.status}"))
            } else {
                emit(NetworkResult.Success)
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage))
        }

    }
}
