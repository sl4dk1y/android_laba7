package com.example.laba7.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LayoutExample(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Column(Modifier.fillMaxWidth().background(Color.LightGray).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Использование Column")
            Text("Элемент 1")
            Text("Элемент 2")
            Row { Text("Слева"); Spacer(Modifier.width(16.dp)); Text("Справа") }
        }
        OutlinedButton(onClick = onBack) { Text("Назад") }
    }
}
