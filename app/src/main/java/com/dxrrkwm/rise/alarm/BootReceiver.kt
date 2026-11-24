package com.dxrrkwm.rise.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        AlarmScheduler.sync(context)
        if (intent.action in BOOT_ACTIONS) WakeSession.resume(context)
    }

    private companion object {
        val BOOT_ACTIONS = setOf(Intent.ACTION_LOCKED_BOOT_COMPLETED, Intent.ACTION_BOOT_COMPLETED)
    }
}
