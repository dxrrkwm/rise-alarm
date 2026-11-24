package com.dxrrkwm.rise.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.ui.graphics.vector.ImageVector
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.data.Mission

val Mission.icon: ImageVector
    get() = when (this) {
        Mission.MATH -> Icons.Rounded.Calculate
        Mission.SHAKE -> Icons.Rounded.Vibration
        Mission.QR -> Icons.Rounded.QrCodeScanner
    }

@get:StringRes
val Mission.title: Int
    get() = when (this) {
        Mission.MATH -> R.string.mission_math
        Mission.SHAKE -> R.string.mission_shake
        Mission.QR -> R.string.mission_qr
    }
