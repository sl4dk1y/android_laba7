package com.example.laba7.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import com.example.laba7.data.User
import com.example.laba7.data.UserRepository

@Composable
fun UsersScreen(repository: UserRepository, onSelect: (User) -> Unit, onBack: () -> Unit) {
    var users by remember(repository) { mutableStateOf<List<User>?>(null) }
    var loadFailed by remember(repository) { mutableStateOf(false) }
    LaunchedEffect(repository) {
        try { users = repository.getUsers() }
        catch (cancellation: CancellationException) { throw cancellation }
        catch (_: Exception) { loadFailed = true }
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Зарегистрированные пользователи", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        when {
            users == null && !loadFailed -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            loadFailed -> Text("Не удалось загрузить список пользователей", color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
            else -> LazyColumn(Modifier.weight(1f)) {
                items(users.orEmpty(), key = { it.id }) { user ->
                    Column(Modifier.fillMaxWidth().clickable { onSelect(user) }.padding(12.dp)) {
                        Text(user.fullName, style = MaterialTheme.typography.titleMedium)
                        Text("${user.login} · ${if (user.isAdmin) "Администратор" else "Пользователь"}")
                    }
                    HorizontalDivider()
                }
            }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
    }
}
