package com.example.laba7.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ListExample(onBack: () -> Unit) {

    // Счётчики нажатий для каждого элемента списка.
    // rememberSaveable сохраняет значения при повороте экрана.
    var counts by rememberSaveable {
        mutableStateOf(List(6) { 0 })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Elements:",
            style = MaterialTheme.typography.headlineMedium
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.LightGray)
        ) {

            items((0..5).toList()) { index ->

                OutlinedButton(
                    onClick = {

                        // Увеличиваем счётчик выбранного элемента.
                        counts = counts.toMutableList().also {
                            it[index]++
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "Element $index clicked: ${counts[index]}",
                        color = Color.Cyan
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onBack
        ) {
            Text("Назад")
        }
    }
}