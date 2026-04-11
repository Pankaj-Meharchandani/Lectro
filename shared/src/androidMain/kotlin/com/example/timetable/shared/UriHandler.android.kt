package com.example.timetable.shared

import android.content.Context
import android.content.Intent
import android.net.Uri

class AndroidUriHandler(private val context: Context) : UriHandler {
    override fun openUri(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

private var uriHandler: UriHandler? = null

fun initUriHandler(context: Context) {
    uriHandler = AndroidUriHandler(context)
}

actual fun getUriHandler(): UriHandler = uriHandler ?: throw Exception("UriHandler not initialized")
