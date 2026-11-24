# Rise

A no-nonsense Android alarm for heavy sleepers, plus a sleep timer. No ads, no accounts, no network access.

Written by [Claude](https://claude.com/claude-code) together with [@dxrrkwm](https://github.com/dxrrkwm).

## Features

**Alarm**
- Missions to turn it off: math problems, shaking, scanning your own QR code / barcode.
- Gradual volume ramp and growing vibration.
- Wake-up check: a few minutes after you dismiss the alarm it asks "are you awake?".
  No answer within a minute → full alarm with a mission again.
- Volume drops while you are solving a mission and comes back if you stop.

**Anti-cheat** (accessibility service, idle when no alarm is active)
- Closes the power menu while the alarm is active, so the phone can't be turned off.
- Keeps the notification shade closed, swallows the volume keys, keeps alarm volume up.
- Brings the alarm back if you switch to another app (calls are allowed).
- A forced reboot does not help: the alarm state lives in device protected storage
  and the alarm resumes right after boot, before the first unlock.

**Sleep timer**
- Fall asleep to YouTube or music: after N minutes it fades the volume out,
  pauses playback and locks the screen. `+10 min` right from the notification.

## Structure

```
app/src/main/java/com/dxrrkwm/rise/
├── RiseApp.kt            # init + notification channels
├── MainActivity.kt       # tabs: alarms / sleep / settings
├── data/                 # Alarm model, JSON store, preferences (device protected storage)
├── alarm/
│   ├── AlarmScheduler.kt # AlarmManager.setAlarmClock
│   ├── AlarmReceiver.kt  # alarm fired / wake check due
│   ├── BootReceiver.kt   # reschedule, resume an interrupted wake-up
│   ├── WakeSession.kt    # state machine: RINGING → CHECK_WAIT → CHECK_PROMPT → IDLE
│   └── RingService.kt    # foreground service: sound, ramp, vibration, volume hold
├── guard/GuardService.kt # accessibility service (anti-cheat, screen lock)
├── sleep/                # sleep timer service
├── ring/                 # alarm screen and missions (math, shake, QR via CameraX + ZXing)
└── ui/                   # Compose screens and theme
```

Kotlin, Jetpack Compose, Material 3. Min Android 12 (API 31).

## Build

```
./gradlew assembleRelease
```

Needs JDK 17+ and Android SDK (platform 37.2). Release signing reads `keystore.properties`
(not in git); without it the debug key is used.

## Setup on the phone

Open **Settings** in the app and grant everything in the "Access" list. For a sideloaded app
Android blocks enabling the accessibility service until you allow it:
App info → ⋮ → *Allow restricted settings*. Or via adb:

```
adb shell settings put secure enabled_accessibility_services com.dxrrkwm.rise/.guard.GuardService
adb shell settings put secure accessibility_enabled 1
```

(the first command replaces the list, append to the existing value if you use other services)

## Roadmap

- Smart alarm with Xiaomi Smart Band (sleep phase window)
- Sound picker, snooze with limits
- More missions: memory, typing, steps
