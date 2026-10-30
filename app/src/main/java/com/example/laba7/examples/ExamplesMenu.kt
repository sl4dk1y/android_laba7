package com.example.laba7.examples

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExamplesMenu(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Примеры Jetpack Compose", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        listOf("text_example" to "Text и Modifier", "layout_example" to "Column и Row", "button_example" to "Кнопка", "list_example" to "Список со счётчиками").forEach { (id, label) ->
            Button(onClick = { onNavigate(id) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
        }
        OutlinedButton(onClick = onBack) { Text("Назад") }
    }
}
