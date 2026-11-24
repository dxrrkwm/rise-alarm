package com.dxrrkwm.rise.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dxrrkwm.rise.MainActivity
import com.dxrrkwm.rise.data.Alarm
import com.dxrrkwm.rise.data.AlarmStore
import java.time.ZonedDateTime

object AlarmScheduler {
    const val ACTION_FIRE = "com.dxrrkwm.rise.FIRE"
    const val ACTION_CHECK = "com.dxrrkwm.rise.CHECK"
    const val EXTRA_ID = "id"

    private const val CHECK_CODE = -100
    private const val FLAGS = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    fun sync(context: Context) {
        AlarmStore.alarms.value.forEach { if (it.enabled) schedule(context, it) else cancel(context, it.id) }
    }

    fun schedule(context: Context, alarm: Alarm): ZonedDateTime {
        val at = alarm.nextTrigger()
        set(context, at.toInstant().toEpochMilli(), fireIntent(context, alarm.id))
        return at
    }

    fun cancel(context: Context, id: Int) = manager(context).cancel(fireIntent(context, id))

    fun scheduleTest(context: Context, delayMillis: Long) =
        set(context, System.currentTimeMillis() + delayMillis, fireIntent(context, Alarm.TEST_ID))

    fun scheduleCheck(context: Context, delayMinutes: Int) =
        set(context, System.currentTimeMillis() + delayMinutes * 60_000L, checkIntent(context))

    fun cancelCheck(context: Context) = manager(context).cancel(checkIntent(context))

    private fun set(context: Context, at: Long, operation: PendingIntent) {
        val show = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), FLAGS)
        manager(context).setAlarmClock(AlarmManager.AlarmClockInfo(at, show), operation)
    }

    private fun fireIntent(context: Context, id: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).setAction(ACTION_FIRE).putExtra(EXTRA_ID, id)
        return PendingIntent.getBroadcast(context, id, intent, FLAGS)
    }

    private fun checkIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).setAction(ACTION_CHECK)
        return PendingIntent.getBroadcast(context, CHECK_CODE, intent, FLAGS)
    }

    private fun manager(context: Context) = context.getSystemService(AlarmManager::class.java)
}
