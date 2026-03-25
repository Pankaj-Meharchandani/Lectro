package com.example.timetable.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val latestVersion: String,
    val releaseNotes: String,
    val downloadUrl: String
)

object UpdateManager {
    private const val GITHUB_API_URL = "https://api.github.com/repos/Pankaj-Meharchandani/Lectro/releases/latest"
    private const val KEY_IGNORE_VERSION = "ignored_version"

    suspend fun checkForUpdates(currentVersion: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Lectro-App")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val latestVersion = json.getString("tag_name").removePrefix("v")
                val releaseNotes = json.getString("body")
                val downloadUrl = json.getString("html_url")

                if (isVersionNewer(currentVersion, latestVersion)) {
                    return@withContext UpdateInfo(latestVersion, releaseNotes, downloadUrl)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    private fun isVersionNewer(current: String, latest: String): Boolean {
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }

        val size = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until size) {
            val curr = currentParts.getOrNull(i) ?: 0
            val late = latestParts.getOrNull(i) ?: 0
            if (late > curr) return true
            if (late < curr) return false
        }
        return false
    }

    fun ignoreVersion(context: Context, version: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(KEY_IGNORE_VERSION, version)
            .apply()
    }

    fun isVersionIgnored(context: Context, version: String): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_IGNORE_VERSION, "") == version
    }

    fun openUpdateUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
