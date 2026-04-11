package com.example.timetable.shared

import android.content.Context
import android.widget.Toast

class AndroidPlatform(private val context: Context) : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
    override val versionName: String = try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

// We need a way to pass context. In many KMP setups, a static context or DI is used.
// For now, let's use an actual that can be initialized.
private var platform: Platform? = null

fun initPlatform(context: Context) {
    platform = AndroidPlatform(context)
}

actual fun getPlatform(): Platform = platform ?: throw Exception("Platform not initialized")
