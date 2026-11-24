package com.dxrrkwm.rise.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.data.Sound
import com.dxrrkwm.rise.data.Sounds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundPicker(selected: String, onPick: (String) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    var current by remember { mutableStateOf(selected) }
    var custom by remember { mutableStateOf(Sounds.custom(context)) }
    val system = remember { Sounds.system(context) }
    val preview = remember { Preview(context) }
    val defaultSound = Sound(Sounds.DEFAULT, stringResource(R.string.sound_default))

    DisposableEffect(Unit) { onDispose { preview.stop() } }
    BackHandler(onBack = onClose)

    fun choose(uri: String) {
        current = uri
        preview.play(Sounds.resolve(context, uri))
    }

    val addFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val sound = uri?.let { Sounds.add(context, it) } ?: return@rememberLauncherForActivityResult
        custom = Sounds.custom(context)
        choose(sound.uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sound)) },
                navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, contentDescription = null) } },
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    onPick(current)
                    onClose()
                },
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(56.dp),
            ) { Text(stringResource(R.string.done)) }
        },
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            item { SoundRow(defaultSound, current == Sounds.DEFAULT, ::choose) }
            item { Sounds.builtIn(context).let { SoundRow(it, current == it.uri, ::choose) } }

            item { Header(stringResource(R.string.sound_custom)) }
            items(custom, key = { it.uri }) { sound ->
                SoundRow(sound, current == sound.uri, ::choose, onDelete = {
                    Sounds.delete(sound)
                    custom = Sounds.custom(context)
                    if (current == sound.uri) current = Sounds.DEFAULT
                })
            }
            item {
                TextButton(onClick = { addFile.launch(arrayOf("audio/*")) }, modifier = Modifier.padding(horizontal = 12.dp)) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Text(stringResource(R.string.sound_add), modifier = Modifier.padding(start = 8.dp))
                }
            }

            item { Header(stringResource(R.string.sound_system)) }
            items(system, key = { it.uri }) { SoundRow(it, current == it.uri, ::choose) }
        }
    }
}

@Composable
private fun Header(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 4.dp),
    )
}

@Composable
private fun SoundRow(sound: Sound, selected: Boolean, onClick: (String) -> Unit, onDelete: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick(sound.uri) }.padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            sound.title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (selected) Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.alarm_delete))
            }
        }
    }
}

private class Preview(private val context: Context) {
    private var player: MediaPlayer? = null

    fun play(uri: Uri) {
        stop()
        val next = MediaPlayer()
        player = next
        runCatching {
            next.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build())
            next.setDataSource(context, uri)
            next.prepare()
            next.start()
        }.onFailure { stop() }
    }

    fun stop() {
        player?.release()
        player = null
    }
}
