package com.dxrrkwm.rise.data

import android.content.Context
import android.content.SharedPreferences

// device protected storage is readable before the first unlock after reboot,
// so an alarm interrupted by a reboot can ring again right away
fun Context.storage(): Context = createDeviceProtectedStorageContext()

fun Context.storagePrefs(name: String): SharedPreferences =
    storage().getSharedPreferences(name, Context.MODE_PRIVATE)
