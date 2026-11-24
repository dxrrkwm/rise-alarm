package com.dxrrkwm.rise.data

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import com.dxrrkwm.rise.R
import java.io.File

data class Sound(val uri: String, val title: String)

// alarm sounds: "" is the system default, then the bundled chime, system alarm tones
// and the user's own files, copied into device protected storage so they play before unlock too
object Sounds {
    const val DEFAULT = ""

    fun builtIn(context: Context) = Sound("android.resource://${context.packageName}/${R.raw.chime}", "Rise")

    fun system(context: Context): List<Sound> {
        val manager = RingtoneManager(context).apply { setType(RingtoneManager.TYPE_ALARM) }
        val cursor = manager.cursor
        return buildList {
            while (cursor.moveToNext()) {
                add(Sound(manager.getRingtoneUri(cursor.position).toString(), cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)))
            }
        }
    }

    fun custom(context: Context): List<Sound> =
        dir(context).listFiles().orEmpty().sortedBy { it.name.lowercase() }.map { Sound(Uri.fromFile(it).toString(), it.nameWithoutExtension) }

    fun add(context: Context, source: Uri): Sound? {
        val name = context.contentResolver.query(source, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { if (it.moveToFirst()) it.getString(0) else null }
            ?: "sound"
        val file = File(dir(context), name.replace(Regex("[^\\w.\\- ]"), "_"))
        val input = context.contentResolver.openInputStream(source) ?: return null
        input.use { file.outputStream().use(it::copyTo) }
        return Sound(Uri.fromFile(file).toString(), file.nameWithoutExtension)
    }

    fun delete(sound: Sound) {
        sound.uri.toUri().path?.let { File(it).delete() }
    }

    fun isCustom(sound: Sound) = sound.uri.startsWith("file:")

    fun title(context: Context, uri: String): String = when {
        uri == DEFAULT -> context.getString(R.string.sound_default)
        uri == builtIn(context).uri -> builtIn(context).title
        uri.startsWith("file:") -> File(uri.toUri().path.orEmpty()).nameWithoutExtension
        else -> runCatching { RingtoneManager.getRingtone(context, uri.toUri()).getTitle(context) }.getOrDefault(uri)
    }

    fun resolve(context: Context, uri: String): Uri = when (uri) {
        DEFAULT -> RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM) ?: builtIn(context).uri.toUri()
        else -> uri.toUri()
    }

    private fun dir(context: Context) = File(context.storage().filesDir, "sounds").apply { mkdirs() }
}
