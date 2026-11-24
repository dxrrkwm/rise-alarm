package com.dxrrkwm.rise.alarm

import android.content.Context
import com.dxrrkwm.rise.data.Alarm
import com.dxrrkwm.rise.data.AlarmStore
import java.time.ZonedDateTime

object AlarmActions {

    fun save(context: Context, alarm: Alarm): ZonedDateTime? {
        AlarmStore.save(alarm)
        if (alarm.enabled) return AlarmScheduler.schedule(context, alarm)
        AlarmScheduler.cancel(context, alarm.id)
        return null
    }

    fun delete(context: Context, alarm: Alarm) {
        AlarmScheduler.cancel(context, alarm.id)
        AlarmStore.delete(alarm.id)
    }
}
