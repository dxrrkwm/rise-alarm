package com.dxrrkwm.rise

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dxrrkwm.rise.data.Alarm
import com.dxrrkwm.rise.ui.AlarmEditScreen
import com.dxrrkwm.rise.ui.AlarmsScreen
import com.dxrrkwm.rise.ui.SettingsScreen
import com.dxrrkwm.rise.ui.SleepScreen
import com.dxrrkwm.rise.ui.theme.RiseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RiseTheme { RiseNav() } }
    }
}

private enum class Tab(@StringRes val title: Int, val icon: ImageVector) {
    ALARMS(R.string.tab_alarms, Icons.Rounded.Alarm),
    SLEEP(R.string.tab_sleep, Icons.Rounded.Bedtime),
    SETTINGS(R.string.tab_settings, Icons.Rounded.Settings),
}

@Composable
private fun RiseNav() {
    var tab by rememberSaveable { mutableStateOf(Tab.ALARMS) }
    var editing by remember { mutableStateOf<Alarm?>(null) }

    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) { notifications.launch(Manifest.permission.POST_NOTIFICATIONS) }

    val alarm = editing
    if (alarm != null) {
        BackHandler { editing = null }
        AlarmEditScreen(alarm, onClose = { editing = null })
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach {
                    NavigationBarItem(
                        selected = tab == it,
                        onClick = { tab = it },
                        icon = { Icon(it.icon, contentDescription = null) },
                        label = { Text(stringResource(it.title)) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                Tab.ALARMS -> AlarmsScreen(onEdit = { editing = it })
                Tab.SLEEP -> SleepScreen()
                Tab.SETTINGS -> SettingsScreen()
            }
        }
    }
}
