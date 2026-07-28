package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cyberguard_settings")

enum class ThemeMode { DARK, LIGHT, SYSTEM }
enum class FontSizeOption { SMALL, MEDIUM, LARGE }

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val LANGUAGE = stringPreferencesKey("language")
        val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val ANONYMIZE_LOGS = booleanPreferencesKey("anonymize_logs")
        val LOGGED_IN_USER_EMAIL = stringPreferencesKey("logged_in_user_email")
    }

    val loggedInUserEmailFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LOGGED_IN_USER_EMAIL]
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val mode = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.DARK.name
        try { ThemeMode.valueOf(mode) } catch (e: Exception) { ThemeMode.DARK }
    }

    val fontSizeFlow: Flow<FontSizeOption> = context.dataStore.data.map { preferences ->
        val size = preferences[PreferencesKeys.FONT_SIZE] ?: FontSizeOption.MEDIUM.name
        try { FontSizeOption.valueOf(size) } catch (e: Exception) { FontSizeOption.MEDIUM }
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LANGUAGE] ?: "English"
    }

    val offlineModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.OFFLINE_MODE] ?: false
    }

    val anonymizeLogsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ANONYMIZE_LOGS] ?: true
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setFontSize(size: FontSizeOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = size.name
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    suspend fun setOfflineMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OFFLINE_MODE] = enabled
        }
    }

    suspend fun setAnonymizeLogs(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANONYMIZE_LOGS] = enabled
        }
    }

    suspend fun setLoggedInUserEmail(email: String?) {
        context.dataStore.edit { preferences ->
            if (email.isNullOrBlank()) {
                preferences.remove(PreferencesKeys.LOGGED_IN_USER_EMAIL)
            } else {
                preferences[PreferencesKeys.LOGGED_IN_USER_EMAIL] = email
            }
        }
    }
}
