package com.turkuaz.beta.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Veri modelleri + manuel JSON (de)serilestirme.
 *
 * Bilerek Moshi/Gson/kotlinx.serialization kullanilmiyor: org.json Android
 * SDK'sinin bir parcasi (ekstra Gradle bagimliligi gerektirmez), boylece
 * bagimlilik yuzeyi kucuk tutuluyor (.NET istemcisindeki "NuGet paketi yok"
 * yaklasimiyla ayni ruh).
 */

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val role: String,
) {
    companion object {
        fun fromJson(o: JSONObject) = TokenResponse(
            accessToken = o.getString("access_token"),
            refreshToken = o.getString("refresh_token"),
            role = o.getString("role"),
        )
    }
}

data class UserMemoryItem(
    val id: Int,
    val key: String,
    val value: String,
) {
    companion object {
        fun fromJson(o: JSONObject) = UserMemoryItem(
            id = o.getInt("id"), key = o.getString("key"), value = o.getString("value"),
        )
    }
}

data class LearningCandidateResult(
    val id: Int,
    val status: String,
    val confidenceScore: Double,
) {
    companion object {
        fun fromJson(o: JSONObject) = LearningCandidateResult(
            id = o.getInt("id"),
            status = o.getString("status"),
            confidenceScore = o.optDouble("confidence_score", 0.0),
        )
    }
}

data class ChatReply(
    val reply: String,
) {
    companion object {
        fun fromJson(o: JSONObject) = ChatReply(reply = o.getString("reply"))
    }
}

data class FeatureFlagItem(
    val key: String,
    val enabled: Boolean,
) {
    companion object {
        fun fromJson(o: JSONObject) = FeatureFlagItem(
            key = o.getString("key"), enabled = o.getBoolean("enabled"),
        )
    }
}

fun parseFeatureFlags(arr: JSONArray): List<FeatureFlagItem> =
    (0 until arr.length()).map { FeatureFlagItem.fromJson(arr.getJSONObject(it)) }

fun parseUserMemory(arr: JSONArray): List<UserMemoryItem> =
    (0 until arr.length()).map { UserMemoryItem.fromJson(arr.getJSONObject(it)) }

/** Sunucudan gelen hata govdesi: {"detail": "..."} */
fun parseErrorDetail(body: String): String =
    try {
        JSONObject(body).optString("detail", body)
    } catch (e: Exception) {
        body
    }

class ApiException(val statusCode: Int, message: String) : Exception(message)
