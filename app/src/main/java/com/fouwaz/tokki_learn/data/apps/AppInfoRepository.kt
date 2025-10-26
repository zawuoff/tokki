package com.fouwaz.tokki_learn.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

class AppInfoRepository(
    private val context: Context
) {

    private val packageManager: PackageManager = context.packageManager

    fun getLaunchableApps(): List<AppInfo> {
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launchIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launchIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        val appsByPackage = resolveInfos
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val packageName = activityInfo.packageName
                val label = resolveInfo.loadLabel(packageManager)?.toString() ?: packageName
                val icon = resolveInfo.loadIcon(packageManager)
                packageName to AppInfo(
                    packageName = packageName,
                    label = label,
                    icon = icon
                )
            }
            .toMap()

        return appsByPackage.values.sortedBy { it.label.lowercase() }
    }

    fun getAppLabel(packageName: String): String {
        return try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0)
            ).toString()
        } catch (error: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    fun getAppIcon(packageName: String) =
        packageManager.getApplicationIcon(packageName)
}
