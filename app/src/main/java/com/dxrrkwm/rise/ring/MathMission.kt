package com.dxrrkwm.rise.ring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dxrrkwm.rise.R
import kotlin.random.Random

data class MathTask(val question: String, val answer: Int) {
    companion object {
        fun random(level: Int): MathTask {
            val r = Random
            return when (level) {
                1 -> {
                    val a = r.nextInt(12, 60)
                    val b = r.nextInt(12, 60)
                    MathTask("$a + $b", a + b)
                }
                2 -> {
                    val a = r.nextInt(11, 30)
                    val b = r.nextInt(3, 10)
                    val c = r.nextInt(11, 100)
                    MathTask("$a × $b + $c", a * b + c)
                }
                else -> {
                    val a = r.nextInt(11, 40)
                    val b = r.nextInt(6, 13)
                    val c = r.nextInt(11, 30)
                    val d = r.nextInt(3, 10)
                    MathTask("$a × $b + $c × $d", a * b + c * d)
                }
            }
        }
    }
}

@Composable
fun MathMission(level: Int, onDone: () -> Unit, total: Int = 3) {
    var solved by remember { mutableIntStateOf(0) }
    var task by remember { mutableStateOf(MathTask.random(level)) }
    var input by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }

    fun submit() {
        if (input.toIntOrNull() != task.answer) {
            wrong = true
            input = ""
            return
        }
        solved += 1
        input = ""
        if (solved == total) onDone() else task = MathTask.random(level)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.math_progress, solved + 1, total), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(task.question, style = MaterialTheme.typography.displaySmall)
        Text(
            input.ifEmpty { "?" },
            style = MaterialTheme.typography.displayMedium,
            color = if (wrong) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
        )
        Keypad(
            onDigit = {
                if (input.length < 6) input += it
                wrong = false
            },
            onErase = { input = input.dropLast(1) },
            onSubmit = ::submit,
        )
    }
}

@Composable
private fun Keypad(onDigit: (Char) -> Unit, onErase: () -> Unit, onSubmit: () -> Unit) {
    val rows = listOf("123", "456", "789", "⌫0✓")
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key ->
                    FilledTonalButton(
                        onClick = {
                            when (key) {
                                '⌫' -> onErase()
                                '✓' -> onSubmit()
                                else -> onDigit(key)
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                    ) {
                        Text(key.toString(), style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}
