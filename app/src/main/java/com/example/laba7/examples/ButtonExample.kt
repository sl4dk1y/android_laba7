package com.example.laba7.examples

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ButtonExample(onBack: () -> Unit) {
    var clicks by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Счётчик нажатий: $clicks")
        Button(onClick = { clicks++ }) { Text("Нажми меня") }
        OutlinedButton(onClick = onBack) { Text("Назад") }
    }
}
