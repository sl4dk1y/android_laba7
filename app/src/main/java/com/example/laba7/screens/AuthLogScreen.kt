package com.example.laba7.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import com.example.laba7.data.UserRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuthLogScreen(repository: UserRepository, onBack: () -> Unit) {
    var entries by remember(repository) { mutableStateOf<List<com.example.laba7.data.AuthLog>?>(null) }
    var loadFailed by remember(repository) { mutableStateOf(false) }
    LaunchedEffect(repository) {
        try { entries = repository.getAuthLogs() }
        catch (cancellation: CancellationException) { throw cancellation }
        catch (_: Exception) { loadFailed = true }
    }
    val formatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Журнал авторизаций", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        when {
            entries == null && !loadFailed -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            loadFailed -> Text("Не удалось загрузить журнал", color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
            entries.orEmpty().isEmpty() -> Text("Записей пока нет", modifier = Modifier.weight(1f))
            else -> LazyColumn(Modifier.weight(1f)) {
                items(entries.orEmpty(), key = { it.id }) { entry ->
                    Text("${formatter.format(Date(entry.timestamp))} — ${entry.login}", Modifier.fillMaxWidth().padding(12.dp))
                    HorizontalDivider()
                }
            }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
    }
}
