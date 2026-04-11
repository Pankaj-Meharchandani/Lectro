package com.example.timetable.shared

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IOSUriHandler : UriHandler {
    override fun openUri(uri: String) {
        val url = NSURL.URLWithString(uri) ?: return
        UIApplication.sharedApplication.openURL(url)
    }
}

actual fun getUriHandler(): UriHandler = IOSUriHandler()
