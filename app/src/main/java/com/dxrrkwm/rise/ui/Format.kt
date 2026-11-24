package com.dxrrkwm.rise.ui

import android.content.Context
import android.content.res.Resources
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.data.Alarm
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZonedDateTime

private val WEEKDAYS = DayOfWeek.entries.take(5).toSet()

fun Resources.dayName(day: DayOfWeek): String = getStringArray(R.array.days_short)[day.value - 1]

fun Resources.daysText(alarm: Alarm): String = when (alarm.days) {
    emptySet<DayOfWeek>() -> getString(R.string.days_once)
    DayOfWeek.entries.toSet() -> getString(R.string.days_every)
    WEEKDAYS -> getString(R.string.days_weekdays)
    else -> alarm.days.sorted().joinToString(", ") { dayName(it) }
}

fun Context.untilText(at: ZonedDateTime): String {
    val minutes = Duration.between(ZonedDateTime.now(), at).toMinutes() + 1
    return getString(R.string.rings_in, minutes / 60, minutes % 60)
}

fun Alarm.timeText(): String = "%02d:%02d".format(hour, minute)
