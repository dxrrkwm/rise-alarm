package com.dxrrkwm.rise.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import java.io.File

object AlarmStore {
    private lateinit var file: File
    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms: StateFlow<List<Alarm>> = _alarms

    fun init(context: Context) {
        file = File(context.storage().filesDir, "alarms.json")
        _alarms.value = runCatching { read() }.getOrDefault(emptyList())
    }

    fun get(id: Int): Alarm? = _alarms.value.find { it.id == id }

    fun nextId(): Int = (_alarms.value.maxOfOrNull { it.id } ?: 0) + 1

    @Synchronized
    fun save(alarm: Alarm) {
        _alarms.value = (_alarms.value.filter { it.id != alarm.id } + alarm).sortedBy { it.hour * 60 + it.minute }
        write()
    }

    @Synchronized
    fun delete(id: Int) {
        _alarms.value = _alarms.value.filter { it.id != id }
        write()
    }

    private fun read(): List<Alarm> {
        val json = JSONArray(file.readText())
        return (0 until json.length()).map { Alarm.fromJson(json.getJSONObject(it)) }
    }

    private fun write() {
        val json = JSONArray()
        _alarms.value.forEach { json.put(it.toJson()) }
        file.writeText(json.toString())
    }
}
