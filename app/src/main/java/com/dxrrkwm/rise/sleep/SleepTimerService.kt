package com.dxrrkwm.rise.sleep

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.dxrrkwm.rise.MainActivity
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.RiseApp
import com.dxrrkwm.rise.guard.GuardService
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// counts down, fades the media volume out, pauses whatever is playing and locks the screen
class SleepTimerService : Service() {
    private val scope = MainScope()
    private var job: Job? = null
    private lateinit var audio: AudioManager
    private var savedVolume = -1
    private var focus: AudioFocusRequest? = null

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        audio = getSystemService(AudioManager::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val minutes = intent?.getIntExtra(EXTRA_MINUTES, 0) ?: 0
        when (intent?.action) {
            ACTION_CANCEL -> _endsAt.value = null
            ACTION_EXTEND -> _endsAt.value = (_endsAt.value ?: now()) + minutes * MINUTE
            else -> _endsAt.value = now() + minutes * MINUTE
        }
        val type = if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification(), type)
        if (_endsAt.value == null) finish() else restart()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        restoreVolume()
        scope.cancel()
        _endsAt.value = null
        super.onDestroy()
    }

    private fun restart() {
        job?.cancel()
        restoreVolume()
        job = scope.launch {
            val end = _endsAt.value ?: return@launch
            delay(end - FADE - now())
            fadeOut(end)
            pauseMedia()
            delay(800)
            GuardService.instance?.lockScreen()
            delay(1500)
            finish()
        }
    }

    private suspend fun fadeOut(end: Long) {
        val start = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        savedVolume = start
        while (now() < end) {
            val left = (end - now()).toFloat() / FADE
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, (start * left).roundToInt(), 0)
            delay(1000)
        }
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
    }

    private fun pauseMedia() {
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE))
        // players that ignore the media key still pause when they lose audio focus
        val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()
        focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(attrs).build()
            .also { audio.requestAudioFocus(it) }
    }

    private fun restoreVolume() {
        if (savedVolume >= 0) audio.setStreamVolume(AudioManager.STREAM_MUSIC, savedVolume, 0)
        savedVolume = -1
    }

    private fun finish() {
        job?.cancel()
        restoreVolume()
        focus?.let { audio.abandonAudioFocusRequest(it) }
        _endsAt.value = null
        stopSelf()
    }

    private fun notification(): Notification {
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, RiseApp.CHANNEL_SLEEP)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.sleep_notification_title))
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(_endsAt.value ?: now())
            .setShowWhen(true)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(open)
            .addAction(0, getString(R.string.sleep_plus_10), action(ACTION_EXTEND, 10))
            .addAction(0, getString(R.string.sleep_stop), action(ACTION_CANCEL, 0))
            .build()
    }

    private fun action(name: String, minutes: Int): PendingIntent {
        val intent = Intent(this, SleepTimerService::class.java).setAction(name).putExtra(EXTRA_MINUTES, minutes)
        return PendingIntent.getService(this, name.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        private const val NOTIFICATION_ID = 2
        private const val MINUTE = 60_000L
        private const val FADE = 30_000L
        private const val ACTION_EXTEND = "extend"
        private const val ACTION_CANCEL = "cancel"
        private const val EXTRA_MINUTES = "minutes"

        private val _endsAt = MutableStateFlow<Long?>(null)
        val endsAt: StateFlow<Long?> = _endsAt

        fun start(context: Context, minutes: Int) = send(context, null, minutes)

        fun extend(context: Context, minutes: Int) = send(context, ACTION_EXTEND, minutes)

        fun cancel(context: Context) = send(context, ACTION_CANCEL, 0)

        private fun send(context: Context, action: String?, minutes: Int) {
            val intent = Intent(context, SleepTimerService::class.java).setAction(action).putExtra(EXTRA_MINUTES, minutes)
            ContextCompat.startForegroundService(context, intent)
        }

        private fun now() = System.currentTimeMillis()
    }
}
