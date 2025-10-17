package com.addev.listaspam.util

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale

/**
 * Utility object for interacting with the RedRoot Spam Call Blocker (SCB) API.
 */
object ApiUtils {
    private const val SCB_API_HOST = "scb-api.redroot.cc"

    private val client = OkHttpClient()

    enum class RiskLevel {
        SAFE,
        SUSPICIOUS,
        SPAM,
        UNKNOWN;

        companion object {
            fun fromValue(value: String?): RiskLevel {
                if (value.isNullOrBlank()) return UNKNOWN
                return when (value.lowercase(Locale.ROOT)) {
                    "safe" -> SAFE
                    "suspicious" -> SUSPICIOUS
                    "spam", "spam_confirmed" -> SPAM
                    else -> if (value.contains("spam", ignoreCase = true)) SPAM else UNKNOWN
                }
            }
        }
    }

    data class ScbApiResponse(
        val originalNumber: String?,
        val normalizedNumber: String?,
        val consultations: Int?,
        val riskLevel: RiskLevel,
    )

    fun fetchRiskLevel(number: String): ScbApiResponse? {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(SCB_API_HOST)
            .addPathSegments("v1/check")
            .addQueryParameter("number", number)
            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val bodyString = response.body?.string() ?: return null
                val json = JSONObject(bodyString)

                val riskLevel = RiskLevel.fromValue(json.optString("risk_level"))
                ScbApiResponse(
                    originalNumber = json.optString("original_number", null),
                    normalizedNumber = json.optString("normalized_number", null),
                    consultations = json.optInt("consultations", 0),
                    riskLevel = riskLevel,
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}
