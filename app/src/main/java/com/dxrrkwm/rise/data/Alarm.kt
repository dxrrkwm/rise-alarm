package com.dxrrkwm.rise.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.ZonedDateTime

enum class Mission { NONE, MATH, SHAKE, QR }

data class Alarm(
    val id: Int,
    val hour: Int = 8,
    val minute: Int = 0,
    val days: Set<DayOfWeek> = emptySet(),
    val enabled: Boolean = true,
    val label: String = "",
    val mission: Mission = Mission.MATH,
    val level: Int = 2,
    val rampSeconds: Int = 60,
    val vibrate: Boolean = true,
    val wakeCheck: Boolean = true,
    val sound: String = Sounds.DEFAULT,
) {
    fun nextTrigger(now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime {
        val today = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        return (0L..7L).asSequence()
            .map { today.plusDays(it) }
            .first { it.isAfter(now) && (days.isEmpty() || it.dayOfWeek in days) }
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("hour", hour)
        .put("minute", minute)
        .put("days", JSONArray(days.map { it.value }))
        .put("enabled", enabled)
        .put("label", label)
        .put("mission", mission.name)
        .put("level", level)
        .put("ramp", rampSeconds)
        .put("vibrate", vibrate)
        .put("wake_check", wakeCheck)
        .put("sound", sound)

    companion object {
        const val TEST_ID = -1

        val TEST = Alarm(id = TEST_ID, level = 1, rampSeconds = 10, wakeCheck = false)

        fun fromJson(json: JSONObject): Alarm {
            val days = json.optJSONArray("days") ?: JSONArray()
            return Alarm(
                id = json.getInt("id"),
                hour = json.getInt("hour"),
                minute = json.getInt("minute"),
                days = (0 until days.length()).map { DayOfWeek.of(days.getInt(it)) }.toSet(),
                enabled = json.optBoolean("enabled", true),
                label = json.optString("label"),
                mission = Mission.entries.find { it.name == json.optString("mission") } ?: Mission.MATH,
                level = json.optInt("level", 2),
                rampSeconds = json.optInt("ramp", 60),
                vibrate = json.optBoolean("vibrate", true),
                wakeCheck = json.optBoolean("wake_check", true),
                sound = json.optString("sound", Sounds.DEFAULT),
            )
        }
    }
}
