package com.example.laba7

import android.os.Bundle
import android.widget.Toast
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.laba7.data.User
import com.example.laba7.data.UserRepository
import com.example.laba7.screens.*
import com.example.laba7.examples.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class MainActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val preferences = getSharedPreferences("lab7_settings", MODE_PRIVATE)
        setContent {
            var darkTheme by remember { mutableStateOf(preferences.getBoolean("dark_theme", false)) }
            MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
                SideEffect {
                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                        AppNavigation(darkTheme, { enabled ->
                            darkTheme = enabled
                            preferences.edit().putBoolean("dark_theme", enabled).apply()
                        }, { finish() })
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(darkTheme: Boolean, onThemeChange: (Boolean) -> Unit, onExit: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { UserRepository(context.applicationContext) }
    LaunchedEffect(repository) { repository.importLegacyAuthLog(context.applicationContext) }
    val databaseDelayEnabled by repository.databaseDelayEnabled.collectAsState()
    val scope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf("login") }
    var user by remember { mutableStateOf<User?>(null) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var loginLoading by remember { mutableStateOf(false) }
    var registrationLoading by remember { mutableStateOf(false) }
    var profileLoading by remember { mutableStateOf(false) }

    fun previousScreen(screen: String) = when (screen) {
        "registration" -> "login"
        "menu" -> "login"
        "users", "profile", "settings", "examples", "auth_log", "game" -> "menu"
        "text_example", "layout_example", "button_example", "list_example" -> "examples"
        else -> "login"
    }
    BackHandler(enabled = currentScreen != "login") { currentScreen = previousScreen(currentScreen); error = null }

    when (currentScreen) {
        "login" -> LoginScreen({ login, password ->
            if (loginLoading) return@LoginScreen
            error = null
            scope.launch {
                loginLoading = true
                try {
                    val found = repository.authenticate(login, password)
                    if (found == null) error = "Неверный логин или пароль" else {
                        user = found
                        notice = "Вход выполнен"; currentScreen = "menu"
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    error = "Не удалось выполнить вход. Попробуйте ещё раз."
                } finally {
                    loginLoading = false
                }
            }
        }, { currentScreen = "registration" }, onExit, error, loginLoading)
        "registration" -> RegistrationScreen({ login, password, name, date, gender, avatarUri ->
            if (registrationLoading) return@RegistrationScreen
            error = null
            scope.launch {
                registrationLoading = true
                try {
                    val result = repository.register(login, password, name, date, gender, avatarUri)
                    result.onSuccess {
                        notice = if (it.isAdmin) "Регистрация выполнена. Создан первый администратор" else "Регистрация выполнена"
                        currentScreen = "login"
                    }.onFailure { error = it.message ?: "Ошибка регистрации" }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    error = "Не удалось зарегистрировать пользователя. Попробуйте ещё раз."
                } finally {
                    registrationLoading = false
                }
            }
        }, { currentScreen = "login" }, error, registrationLoading)
        "menu" -> {
            val activeUser = user
            if (activeUser == null) currentScreen = "login" else {
                if (notice != null) { Toast.makeText(context, notice, Toast.LENGTH_SHORT).show(); notice = null }
                MenuScreen(activeUser, { destination ->
                    if (destination in listOf("users", "auth_log") && !canAccessAdminScreens(activeUser)) currentScreen = "menu" else currentScreen = destination
                }, {
                    user = null; currentScreen = "login"
                })
            }
        }
        "users" -> {
            if (!canAccessAdminScreens(user)) currentScreen = "menu" else UsersScreen(repository, { chosen -> selectedUser = chosen; currentScreen = "profile" }, { currentScreen = "menu" })
        }
        "profile" -> {
            val profile = if (selectedUser == null) user else selectedUser
            if (profile == null) currentScreen = "menu" else ProfileScreen(profile, user?.isAdmin == true, {
                if (profileLoading) return@ProfileScreen
                val actorId = user?.id ?: return@ProfileScreen
                scope.launch {
                    profileLoading = true
                    try {
                        if (repository.promote(actorId, profile.id)) {
                            selectedUser = profile.copy(isAdmin = true)
                            notice = "Права администратора назначены"
                        } else error = "Недостаточно прав для изменения роли"
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        error = "Не удалось изменить роль. Попробуйте ещё раз."
                    } finally {
                        profileLoading = false
                    }
                }
            }, { avatarUri ->
                val actor = user
                if (actor != null && profile.id == actor.id) {
                    scope.launch {
                        profileLoading = true
                        try {
                            if (repository.updateAvatar(profile.id, avatarUri)) {
                                user = actor.copy(avatarUri = avatarUri)
                                selectedUser = selectedUser?.copy(avatarUri = avatarUri)
                            } else error = "Не удалось сохранить аватар"
                        } catch (cancellation: CancellationException) {
                            throw cancellation
                        } catch (_: Exception) {
                            error = "Не удалось сохранить аватар"
                        } finally {
                            profileLoading = false
                        }
                    }
                }
            }, profile.id == user?.id, { selectedUser = null; currentScreen = if (user?.isAdmin == true) "users" else "menu" }, profileLoading)
        }
        "settings" -> SettingsScreen(darkTheme, onThemeChange, databaseDelayEnabled, repository::setDatabaseDelayEnabled, { currentScreen = "menu" })
        "auth_log" -> if (canAccessAdminScreens(user)) AuthLogScreen(repository, onBack = { currentScreen = "menu" }) else currentScreen = "menu"
        "game" -> GameScreen(onBack = { currentScreen = "menu" })
        "examples" -> ExamplesMenu({ currentScreen = it }, { currentScreen = "menu" })
        "text_example" -> TextExample { currentScreen = "examples" }
        "layout_example" -> LayoutExample { currentScreen = "examples" }
        "button_example" -> ButtonExample { currentScreen = "examples" }
        "list_example" -> ListExample { currentScreen = "examples" }
    }
}

fun canAccessAdminScreens(user: User?): Boolean = user?.isAdmin == true
