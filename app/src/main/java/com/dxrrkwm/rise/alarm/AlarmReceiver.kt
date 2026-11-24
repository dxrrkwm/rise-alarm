package com.dxrrkwm.rise.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dxrrkwm.rise.data.Alarm
import com.dxrrkwm.rise.data.AlarmStore

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AlarmScheduler.ACTION_FIRE -> fire(context, intent.getIntExtra(AlarmScheduler.EXTRA_ID, Alarm.TEST_ID))
            AlarmScheduler.ACTION_CHECK -> WakeSession.checkDue(context)
        }
    }

    private fun fire(context: Context, id: Int) {
        if (id == Alarm.TEST_ID) return WakeSession.start(context, Alarm.TEST)

        val alarm = AlarmStore.get(id)?.takeIf { it.enabled } ?: return
        if (alarm.days.isEmpty()) AlarmStore.save(alarm.copy(enabled = false)) else AlarmScheduler.schedule(context, alarm)
        WakeSession.start(context, alarm)
    }
}
