package com.dxrrkwm.rise

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import com.dxrrkwm.rise.alarm.WakeSession
import com.dxrrkwm.rise.data.AlarmStore
import com.dxrrkwm.rise.data.Prefs

class RiseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        AlarmStore.init(this)
        WakeSession.init(this)
        createChannels()
    }

    private fun createChannels() {
        val ring = NotificationChannel(CHANNEL_RING, getString(R.string.channel_ring), NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val sleep = NotificationChannel(CHANNEL_SLEEP, getString(R.string.channel_sleep), NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannels(listOf(ring, sleep))
    }

    companion object {
        const val CHANNEL_RING = "ring"
        const val CHANNEL_SLEEP = "sleep"
    }
}
