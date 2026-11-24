package com.dxrrkwm.rise.guard

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import com.dxrrkwm.rise.alarm.WakeSession
import com.dxrrkwm.rise.ring.RingActivity

// does nothing while no alarm is active; during a wake-up it closes the power menu,
// and while ringing it also keeps the notification shade closed, eats volume keys
// and brings the alarm screen back when another app shows up
class GuardService : AccessibilityService() {

    override fun onServiceConnected() {
        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onInterrupt() = Unit

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!WakeSession.active) return
        val pkg = event.packageName?.toString()

        if (pkg == SYSTEM_UI || event.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            if (powerMenuOpen(event)) return closeSystemDialogs()
            if (pkg == SYSTEM_UI && WakeSession.strict) performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
            return
        }

        val leftAlarm = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            pkg != null && pkg != packageName && !isCall(pkg)
        if (WakeSession.strict && leftAlarm) RingActivity.open(this)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean =
        WakeSession.strict && event.keyCode in VOLUME_KEYS

    fun lockScreen() = performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)

    private fun powerMenuOpen(event: AccessibilityEvent): Boolean {
        if (event.className?.contains("GlobalActions") == true) return true
        return windows.any { window ->
            window.root?.findAccessibilityNodeInfosByViewId(POWER_MENU_ID)?.isNotEmpty() == true
        }
    }

    private fun closeSystemDialogs() {
        performGlobalAction(GLOBAL_ACTION_BACK)
        performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
    }

    private fun isCall(pkg: String) = CALL_APPS.any { it in pkg }

    companion object {
        private const val SYSTEM_UI = "com.android.systemui"
        private const val POWER_MENU_ID = "com.android.systemui:id/global_actions_container"
        private val VOLUME_KEYS = setOf(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_MUTE)
        private val CALL_APPS = listOf("dialer", "incallui", "telecom")

        var instance: GuardService? = null
            private set

        fun isEnabled(context: Context): Boolean =
            context.getSystemService(AccessibilityManager::class.java)
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                .any { it.resolveInfo.serviceInfo.packageName == context.packageName }
    }
}
