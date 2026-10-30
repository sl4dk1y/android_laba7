package com.example.laba7.data

import android.content.SharedPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Keeps the optional laboratory delay out of the UI thread. */
class DatabaseDelayController(
    private val preferences: SharedPreferences? = null
) {
    private val mutableEnabled = MutableStateFlow(
        preferences?.getBoolean(KEY_ENABLED, false) ?: false
    )
    val enabled: StateFlow<Boolean> = mutableEnabled

    fun setEnabled(enabled: Boolean) {
        preferences?.edit()?.putBoolean(KEY_ENABLED, enabled)?.apply()
        mutableEnabled.value = enabled
    }

    suspend fun waitIfEnabled() {
        if (mutableEnabled.value) delay(DELAY_MILLIS)
    }

    private companion object {
        const val KEY_ENABLED = "database_delay_enabled"
        const val DELAY_MILLIS = 3_000L
    }
}
