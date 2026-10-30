package com.example.laba7.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.laba7.data.User

@Composable
fun MenuScreen(user: User, onNavigate: (String) -> Unit, onExit: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Меню приложения", style = MaterialTheme.typography.headlineMedium)
        Text("Здравствуйте, ${user.fullName}")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onNavigate("game") }, modifier = Modifier.fillMaxWidth()) { Text("Старт") }
        if (user.isAdmin) {
            Button(onClick = { onNavigate("users") }, modifier = Modifier.fillMaxWidth()) { Text("Администрирование") }
            Button(onClick = { onNavigate("auth_log") }, modifier = Modifier.fillMaxWidth()) { Text("Журнал авторизаций") }
        }
        Button(onClick = { onNavigate("profile") }, modifier = Modifier.fillMaxWidth()) { Text("Профиль") }
        Button(onClick = { onNavigate("settings") }, modifier = Modifier.fillMaxWidth()) { Text("Настройки") }
        Button(onClick = { onNavigate("examples") }, modifier = Modifier.fillMaxWidth()) { Text("Примеры Compose") }
        OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("Выйти из аккаунта") }
    }
}
