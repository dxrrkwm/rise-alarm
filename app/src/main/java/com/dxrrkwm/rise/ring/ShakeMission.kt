package com.dxrrkwm.rise.ring

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.alarm.RingService
import kotlin.math.sqrt

@Composable
fun ShakeMission(level: Int, onDone: () -> Unit) {
    val context = LocalContext.current
    val target = SHAKES_PER_LEVEL * level
    var count by remember { mutableIntStateOf(0) }
    val done by rememberUpdatedState(onDone)

    DisposableEffect(Unit) {
        val sensors = context.getSystemService(SensorManager::class.java)
        var lastShake = 0L
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val (x, y, z) = event.values
                val force = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
                val now = System.currentTimeMillis()
                if (force < THRESHOLD || now - lastShake < DEBOUNCE) return
                lastShake = now
                RingService.quiet()
                count += 1
                if (count == target) done()
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensors.registerListener(listener, sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensors.unregisterListener(listener) }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(stringResource(R.string.shake_title), style = MaterialTheme.typography.headlineSmall)
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { count.toFloat() / target },
                modifier = Modifier.size(200.dp),
                strokeWidth = 12.dp,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text("${target - count}", style = MaterialTheme.typography.displayMedium)
        }
    }
}

private const val SHAKES_PER_LEVEL = 20
private const val THRESHOLD = 9f
private const val DEBOUNCE = 180L
