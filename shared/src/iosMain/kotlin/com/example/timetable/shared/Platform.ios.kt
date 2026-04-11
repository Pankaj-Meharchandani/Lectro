package com.example.timetable.shared

import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

class IOSPlatform : Platform {
    override val name: String = "iOS"
    override val versionName: String = "1.0"
    override fun showToast(message: String) {
        val alert = UIAlertController.alertControllerWithTitle(
            title = null,
            message = message,
            preferredStyle = 1 // UIAlertControllerStyleAlert
        )
        alert.addAction(UIAlertAction.actionWithTitle("OK", style = 0, handler = null))
        
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            alert,
            animated = true,
            completion = null
        )
    }
}

actual fun getPlatform(): Platform = IOSPlatform()
