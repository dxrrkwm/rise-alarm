package com.dxrrkwm.rise.alarm

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.dxrrkwm.rise.data.Alarm
import com.dxrrkwm.rise.data.AlarmStore
import com.dxrrkwm.rise.data.Prefs
import com.dxrrkwm.rise.data.storagePrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class Phase { IDLE, RINGING, CHECK_WAIT, CHECK_PROMPT }

// one wake-up from the first ring until the last wake check is passed;
// persisted so a reboot or a killed process can't end it
object WakeSession {
    private lateinit var prefs: SharedPreferences
    private val _phase = MutableStateFlow(Phase.IDLE)
    val phase: StateFlow<Phase> = _phase

    val active get() = phase.value != Phase.IDLE
    val strict get() = phase.value == Phase.RINGING || phase.value == Phase.CHECK_PROMPT

    private var alarmId: Int
        get() = prefs.getInt("alarm_id", Alarm.TEST_ID)
        set(value) = prefs.edit { putInt("alarm_id", value) }

    private var checksLeft: Int
        get() = prefs.getInt("checks_left", 0)
        set(value) = prefs.edit { putInt("checks_left", value) }

    fun init(context: Context) {
        prefs = context.storagePrefs("session")
        _phase.value = Phase.entries.find { it.name == prefs.getString("phase", null) } ?: Phase.IDLE
    }

    fun alarm(): Alarm = AlarmStore.get(alarmId) ?: Alarm.TEST

    fun start(context: Context, alarm: Alarm) {
        AlarmScheduler.cancelCheck(context)
        alarmId = alarm.id
        checksLeft = if (alarm.wakeCheck) Prefs.checkCount else 0
        setPhase(Phase.RINGING)
        RingService.start(context)
    }

    // mission solved or "I'm awake" confirmed
    fun advance(context: Context) {
        RingService.stop(context)
        if (checksLeft <= 0) return finish(context)
        checksLeft -= 1
        setPhase(Phase.CHECK_WAIT)
        AlarmScheduler.scheduleCheck(context, Prefs.checkDelayMinutes)
    }

    fun checkDue(context: Context) {
        if (phase.value != Phase.CHECK_WAIT) return
        setPhase(Phase.CHECK_PROMPT)
        RingService.start(context)
    }

    // missed wake check: back to full alarm, and one more check after it
    fun escalate() {
        if (phase.value != Phase.CHECK_PROMPT) return
        checksLeft += 1
        setPhase(Phase.RINGING)
    }

    fun resume(context: Context) {
        if (!active) return
        AlarmScheduler.cancelCheck(context)
        if (phase.value != Phase.RINGING) checksLeft += 1
        setPhase(Phase.RINGING)
        RingService.start(context)
    }

    fun finish(context: Context) {
        RingService.stop(context)
        AlarmScheduler.cancelCheck(context)
        setPhase(Phase.IDLE)
    }

    private fun setPhase(value: Phase) {
        prefs.edit(commit = true) { putString("phase", value.name) }
        _phase.value = value
    }
}
