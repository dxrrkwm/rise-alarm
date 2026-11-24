package com.dxrrkwm.rise.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.RiseApp
import com.dxrrkwm.rise.data.Alarm
import com.dxrrkwm.rise.data.Prefs
import com.dxrrkwm.rise.ring.RingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class RingService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loop: Job? = null
    private var player: MediaPlayer? = null
    private lateinit var audio: AudioManager
    private lateinit var vibrator: Vibrator
    private lateinit var wakeLock: PowerManager.WakeLock
    private var vibrationStep = -1

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        audio = getSystemService(AudioManager::class.java)
        vibrator = getSystemService(VibratorManager::class.java).defaultVibrator
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "rise:ring")
            .apply { acquire((Prefs.maxRingMinutes + 1) * 60_000L) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val type = if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification(), type)
        if (!WakeSession.strict) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (loop?.isActive != true) loop = scope.launch { ring(WakeSession.alarm()) }
        RingActivity.open(this)
        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        player?.release()
        vibrator.cancel()
        if (wakeLock.isHeld) wakeLock.release()
        super.onDestroy()
    }

    private suspend fun ring(alarm: Alarm) {
        val player = createPlayer().also { player = it }
        player.setVolume(0f, 0f)
        player.start()

        val startedAt = now()
        var phase = WakeSession.phase.value
        var phaseSince = now()

        while (scope.isActive) {
            if (WakeSession.phase.value != phase) {
                phase = WakeSession.phase.value
                phaseSince = now()
            }
            val elapsed = now() - phaseSince
            if (now() - startedAt > Prefs.maxRingMinutes * 60_000L) return WakeSession.finish(this)
            if (phase == Phase.CHECK_PROMPT && elapsed > CHECK_TIMEOUT) WakeSession.escalate()

            val level = when (phase) {
                Phase.CHECK_PROMPT -> CHECK_LEVEL
                else -> rampLevel(elapsed, alarm.rampSeconds)
            }
            val heard = if (now() < quietUntil) minOf(level, QUIET_LEVEL) else level
            player.setVolume(heard, heard)
            holdVolume()
            if (alarm.vibrate) vibrate(if (now() < quietUntil) 0f else maxOf(level, 0.25f))
            delay(TICK)
        }
    }

    private fun rampLevel(elapsed: Long, rampSeconds: Int): Float {
        if (rampSeconds <= 0) return 1f
        return (0.05f + elapsed / (rampSeconds * 1000f)).coerceAtMost(1f)
    }

    // whatever happens to the alarm stream volume, put it back
    private fun holdVolume() {
        val target = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM) * Prefs.volumePercent / 100
        if (audio.getStreamVolume(AudioManager.STREAM_ALARM) < target) {
            runCatching { audio.setStreamVolume(AudioManager.STREAM_ALARM, target, 0) }
        }
    }

    private fun vibrate(level: Float) {
        val step = (level * 4).roundToInt()
        if (step == vibrationStep) return
        vibrationStep = step
        if (step == 0) return vibrator.cancel()

        val effect = VibrationEffect.createWaveform(longArrayOf(0, 600, 700), intArrayOf(0, 255 * step / 4, 0), 1)
        if (Build.VERSION.SDK_INT >= 33) {
            vibrator.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM))
        } else {
            vibrator.vibrate(effect)
        }
    }

    private fun createPlayer(): MediaPlayer {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val system = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
        return runCatching { player(attrs) { setDataSource(this@RingService, system!!) } }
            .getOrElse { player(attrs) { resources.openRawResourceFd(R.raw.chime).use { setDataSource(it) } } }
    }

    private fun player(attrs: AudioAttributes, source: MediaPlayer.() -> Unit): MediaPlayer {
        val player = MediaPlayer()
        try {
            player.setAudioAttributes(attrs)
            player.source()
            player.isLooping = true
            player.prepare()
            return player
        } catch (e: Exception) {
            player.release()
            throw e
        }
    }

    private fun notification(): Notification {
        val open = PendingIntent.getActivity(
            this, 0, RingActivity.intent(this),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, RiseApp.CHANNEL_RING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.ring_notification_title))
            .setContentText(getString(R.string.ring_notification_text))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(open, true)
            .setContentIntent(open)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val TICK = 250L
        private const val CHECK_TIMEOUT = 60_000L
        private const val CHECK_LEVEL = 0.35f
        private const val QUIET_LEVEL = 0.12f
        private const val QUIET_MILLIS = 20_000L

        @Volatile
        private var quietUntil = 0L

        // called on every touch while solving a mission
        fun quiet() {
            quietUntil = now() + QUIET_MILLIS
        }

        fun start(context: Context) {
            quietUntil = 0L
            ContextCompat.startForegroundService(context, Intent(context, RingService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, RingService::class.java))
        }

        private fun now() = System.currentTimeMillis()
    }
}
