package com.dxrrkwm.rise.ui

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.annotation.RequiresApi

// per-app language, the same list Android shows in system settings → app languages
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
object AppLanguage {
    const val SYSTEM = ""

    val names = mapOf("uk" to "Українська", "en" to "English", "ru" to "Русский")

    fun current(context: Context): String =
        manager(context).applicationLocales.get(0)?.language ?: SYSTEM

    fun set(context: Context, language: String) {
        manager(context).applicationLocales = LocaleList.forLanguageTags(language)
    }

    private fun manager(context: Context) = context.getSystemService(LocaleManager::class.java)
}
