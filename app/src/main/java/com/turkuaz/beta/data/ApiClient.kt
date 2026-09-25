package com.turkuaz.beta.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * TURKUAZ CORE API istemcisi.
 *
 * Retrofit/OkHttp gibi bir kutuphane yerine bilerek ciplak HttpURLConnection
 * kullaniliyor: bagimlilik yuzeyini kucuk tutmak icin (Windows istemcisindeki
 * "NuGet paketi yok" tercihiyle ayni gerekce). Is mantiginin TAMAMI sunucuda -
 * bu sinif sadece HTTP cagrilarini sarmalar (Bolum 0.1).
 */
class ApiClient(private val session: SessionStore, private val deviceFingerprint: String) {

    private suspend fun request(
        method: String,
        path: String,
        body: JSONObject? = null,
        authorized: Boolean = true,
    ): String = withContext(Dispatchers.IO) {
        val url = URL(Config.BASE_URL + path)
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("X-Device-Fingerprint", deviceFingerprint)
            if (authorized) {
                session.accessToken?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
            }
            if (body != null) {
                conn.doOutput = true
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }
            }

            val status = conn.responseCode
            val stream = if (status in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.let { s ->
                BufferedReader(InputStreamReader(s, Charsets.UTF_8)).use { it.readText() }
            } ?: ""

            if (status !in 200..299) {
                throw ApiException(status, parseErrorDetail(text))
            }
            text
        } finally {
            conn.disconnect()
        }
    }

    // ---- Auth ----

    suspend fun login(username: String, password: String): TokenResponse {
        val body = JSONObject()
            .put("username", username)
            .put("password", password)
            .put("device_fingerprint", deviceFingerprint)
        val resp = request("POST", "/api/v1/auth/login", body, authorized = false)
        val token = TokenResponse.fromJson(JSONObject(resp))
        session.accessToken = token.accessToken
        session.refreshToken = token.refreshToken
        session.role = token.role
        return token
    }

    // ---- Sohbet ----

    suspend fun sendChat(message: String): ChatReply {
        val body = JSONObject().put("message", message)
        val resp = request("POST", "/api/v1/chat", body)
        return ChatReply.fromJson(JSONObject(resp))
    }

    // ---- Kullanici Hafizasi ----

    suspend fun getUserMemory(): List<UserMemoryItem> {
        val resp = request("GET", "/api/v1/memory/me")
        return parseUserMemory(JSONArray(resp))
    }

    suspend fun writeUserMemory(key: String, value: String): UserMemoryItem {
        val body = JSONObject().put("key", key).put("value", value)
        val resp = request("POST", "/api/v1/memory/me", body)
        return UserMemoryItem.fromJson(JSONObject(resp))
    }

    suspend fun deleteUserMemory(memoryId: Int) {
        request("DELETE", "/api/v1/memory/me/$memoryId")
    }

    suspend fun exportUserMemory(): String = request("GET", "/api/v1/memory/me/export")

    // ---- Ogrenme Adayi ("sunu ogren") ----

    suspend fun submitLearnCandidate(text: String): LearningCandidateResult {
        val body = JSONObject().put("text", text).put("source", "beta_client")
        val resp = request("POST", "/api/v1/memory/learn-candidate", body)
        return LearningCandidateResult.fromJson(JSONObject(resp))
    }

    // ---- Geri bildirim ----

    suspend fun submitFeedback(message: String, category: String) {
        val body = JSONObject().put("message", message).put("category", category)
        request("POST", "/api/v1/feedback", body)
    }

    // ---- Feature Flags ----

    suspend fun getFeatureFlags(): List<FeatureFlagItem> {
        val resp = request("GET", "/api/v1/feature-flags")
        return parseFeatureFlags(JSONArray(resp))
    }
}
