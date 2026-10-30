package com.example.laba7.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    darkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    databaseDelayEnabled: Boolean,
    onDatabaseDelayChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        // Заголовок экрана
        Text(
            text = "Настройки",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Настройка темы
        Text(
            text = "Тема приложения"
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row {

            // Светлая тема
            Button(
                onClick = {
                    onThemeChange(false)
                }
            ) {
                Text("День")
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            // Тёмная тема
            Button(
                onClick = {
                    onThemeChange(true)
                }
            ) {
                Text("Ночь")
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Отображение текущей темы
        Text(
            text = if (darkMode) {
                "Выбрана ночная тема"
            } else {
                "Выбрана дневная тема"
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text("Искусственная задержка запросов к базе данных")
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Switch(checked = databaseDelayEnabled, onCheckedChange = onDatabaseDelayChange)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (databaseDelayEnabled) "3 секунды" else "0 секунд")
        }
        Text(
            "Включите задержку, чтобы увидеть индикатор загрузки при операциях с базой.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Возврат в меню
        OutlinedButton(
            onClick = onBack
        ) {
            Text("Назад")
        }
    }
}
