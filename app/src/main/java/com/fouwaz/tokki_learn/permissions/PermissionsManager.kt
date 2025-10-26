package com.fouwaz.tokki_learn.permissions

import android.accessibilityservice.AccessibilityService
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

class PermissionsManager(
    private val context: Context
) {

    fun overlayGranted(): Boolean = Settings.canDrawOverlays(context)

    fun usageAccessGranted(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun accessibilityGranted(serviceClass: Class<out AccessibilityService>): Boolean {
        val accessibilityManager =
            context.getSystemService(AccessibilityManager::class.java) ?: return false
        if (!accessibilityManager.isEnabled) return false

        val expectedComponent = ComponentName(context, serviceClass)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSeparated = enabledServices.split(':')
        return colonSeparated.any { it.equals(expectedComponent.flattenToString(), ignoreCase = true) }
    }

    fun allPermissionStatuses(serviceClass: Class<out AccessibilityService>): List<PermissionStatus> {
        return listOf(
            PermissionStatus(PermissionType.OVERLAY, overlayGranted()),
            PermissionStatus(PermissionType.ACCESSIBILITY, accessibilityGranted(serviceClass)),
            PermissionStatus(PermissionType.USAGE_ACCESS, usageAccessGranted())
        )
    }
}
