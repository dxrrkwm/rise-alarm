package com.dxrrkwm.rise.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object Prefs {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.storagePrefs("prefs")
    }

    var checkDelayMinutes by int("check_delay", 5)
    var checkCount by int("check_count", 1)
    var volumePercent by int("volume", 100)
    var maxRingMinutes by int("max_ring", 30)
    var sleepMinutes by int("sleep_minutes", 45)
    var qrCode by string("qr_code", "")

    private fun int(key: String, default: Int) = object : ReadWriteProperty<Any?, Int> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) = prefs.getInt(key, default)
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) = prefs.edit { putInt(key, value) }
    }

    private fun string(key: String, default: String) = object : ReadWriteProperty<Any?, String> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) = prefs.getString(key, default) ?: default
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) = prefs.edit { putString(key, value) }
    }
}
