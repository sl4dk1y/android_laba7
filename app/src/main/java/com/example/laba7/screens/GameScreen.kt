package com.example.laba7.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun GameScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var target by rememberSaveable { mutableIntStateOf(Random.nextInt(1, 11)) }
    var input by rememberSaveable { mutableStateOf("") }
    var outcome by rememberSaveable { mutableStateOf<GuessOutcome?>(null) }
    val won = outcome == GuessOutcome.CORRECT

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Игра", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Угадайте число от 1 до 10")
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it.take(3); outcome = null },
            label = { Text("Ваш вариант") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            enabled = !won,
            modifier = Modifier.fillMaxWidth()
        )
        outcome?.let { result ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = when (result) {
                    GuessOutcome.EMPTY -> "Введите число"
                    GuessOutcome.NOT_A_NUMBER -> "Введите целое число"
                    GuessOutcome.OUT_OF_RANGE -> "Число должно быть от 1 до 10"
                    GuessOutcome.INCORRECT -> "Неверно, попробуйте ещё"
                    GuessOutcome.CORRECT -> "Вы угадали!"
                },
                color = if (result == GuessOutcome.INCORRECT || result == GuessOutcome.OUT_OF_RANGE || result == GuessOutcome.NOT_A_NUMBER || result == GuessOutcome.EMPTY) {
                    MaterialTheme.colorScheme.error
                } else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(12.dp))
        if (!won) {
            Button(onClick = {
                outcome = evaluateGuess(input, target)
                if (outcome == GuessOutcome.INCORRECT) vibrateBriefly(context)
            }, modifier = Modifier.fillMaxWidth()) { Text("Проверить") }
        } else {
            Button(onClick = {
                target = Random.nextInt(1, 11)
                input = ""
                outcome = null
            }, modifier = Modifier.fillMaxWidth()) { Text("Новая игра") }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
    }
}

@Suppress("DEPRECATION")
private fun vibrateBriefly(context: Context) {
    runCatching {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200L)
        }
    }
}
