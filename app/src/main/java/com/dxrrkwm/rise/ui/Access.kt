package com.dxrrkwm.rise.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.guard.GuardService

data class Access(@StringRes val title: Int, @StringRes val hint: Int, val granted: Boolean, val settings: Intent)

object AccessList {

    @SuppressLint("BatteryLife")
    fun all(context: Context): List<Access> {
        val pkg = context.packageName
        val packageUri = "package:$pkg".toUri()
        return listOf(
            Access(
                R.string.access_notifications,
                R.string.access_notifications_hint,
                NotificationManagerCompat.from(context).areNotificationsEnabled(),
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, pkg),
            ),
            Access(
                R.string.access_full_screen,
                R.string.access_full_screen_hint,
                Build.VERSION.SDK_INT < 34 || context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent(),
                Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, packageUri),
            ),
            Access(
                R.string.access_battery,
                R.string.access_battery_hint,
                context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(pkg),
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri),
            ),
            Access(
                R.string.access_guard,
                R.string.access_guard_hint,
                GuardService.isEnabled(context),
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
            ),
            Access(
                R.string.access_camera,
                R.string.access_camera_hint,
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri),
            ),
        )
    }
}
