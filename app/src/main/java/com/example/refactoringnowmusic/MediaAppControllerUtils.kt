package com.example.refactoringnowmusic

import android.annotation.TargetApi
import android.content.ContentValues
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.session.MediaController
import android.os.Build
import android.util.Log
import java.util.ArrayList

object MediaAppControllerUtils {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @JvmStatic
    fun getMediaAppsFromControllers(
            controllers: Collection<MediaController>,
            packageManager: PackageManager,
            resources: Resources
    ): List<MediaAppDetails> {
        val mediaApps = ArrayList<MediaAppDetails>()
        for (controller in controllers) {
            val packageName = controller.packageName
            val info: ApplicationInfo
            try {
                info = packageManager.getApplicationInfo(packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                // This should not happen. If we get a media session for a package, then the
                // package must be installed on the device.
                Log.e(ContentValues.TAG, "Unable to load package details", e)
                continue
            }

            mediaApps.add(
                    MediaAppDetails(info, packageManager, resources, controller.sessionToken)
            )
        }
        return mediaApps
    }
}