package com.smartutilities.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FAVORITES = stringSetPreferencesKey("favorites")
        val USE_SYSTEM_THEME = booleanPreferencesKey("use_system_theme")
    }

    val darkMode: Flow<Boolean> = dataStore.data.map { it[DARK_MODE] ?: false }
    val useSystemTheme: Flow<Boolean> = dataStore.data.map { it[USE_SYSTEM_THEME] ?: true }
    val favorites: Flow<Set<String>> = dataStore.data.map { it[FAVORITES] ?: emptySet() }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setUseSystemTheme(enabled: Boolean) {
        dataStore.edit { it[USE_SYSTEM_THEME] = enabled }
    }

    suspend fun toggleFavorite(utilityId: String) {
        dataStore.edit { prefs ->
            val current = prefs[FAVORITES] ?: emptySet()
            prefs[FAVORITES] = if (utilityId in current) current - utilityId else current + utilityId
        }
    }
}
