package com.smarttoolkit.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        val FAVORITE_ORDER = stringPreferencesKey("favorite_order")
        val USE_SYSTEM_THEME = booleanPreferencesKey("use_system_theme")
        val OCR_HINT_SHOWN = booleanPreferencesKey("ocr_hint_shown")

        // Utility persistence keys
        val TALLY_COUNT = intPreferencesKey("tally_count")
        val TIP_PERCENTAGE = intPreferencesKey("tip_percentage")
        val UC_CATEGORY = intPreferencesKey("uc_category")
        val UC_FROM_UNIT = intPreferencesKey("uc_from_unit")
        val UC_TO_UNIT = intPreferencesKey("uc_to_unit")
        val TIMER_HOURS = intPreferencesKey("timer_hours")
        val TIMER_MINUTES = intPreferencesKey("timer_minutes")
        val TIMER_SECONDS = intPreferencesKey("timer_seconds")
    }

    val darkMode: Flow<Boolean> = dataStore.data.map { it[DARK_MODE] ?: false }
    val useSystemTheme: Flow<Boolean> = dataStore.data.map { it[USE_SYSTEM_THEME] ?: true }
    val favorites: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[FAVORITE_ORDER]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    val ocrHintShown: Flow<Boolean> = dataStore.data.map { it[OCR_HINT_SHOWN] ?: false }

    suspend fun setOcrHintShown() {
        dataStore.edit { it[OCR_HINT_SHOWN] = true }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setUseSystemTheme(enabled: Boolean) {
        dataStore.edit { it[USE_SYSTEM_THEME] = enabled }
    }

    suspend fun toggleFavorite(utilityId: String) {
        dataStore.edit { prefs ->
            val current = prefs[FAVORITE_ORDER]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
            prefs[FAVORITE_ORDER] = if (utilityId in current) {
                (current - utilityId).joinToString(",")
            } else {
                (current + utilityId).joinToString(",")
            }
        }
    }

    suspend fun reorderFavorite(fromIndex: Int, toIndex: Int) {
        dataStore.edit { prefs ->
            val current = prefs[FAVORITE_ORDER]?.split(",")?.filter { it.isNotEmpty() }?.toMutableList() ?: return@edit
            if (fromIndex in current.indices && toIndex in current.indices) {
                val item = current.removeAt(fromIndex)
                current.add(toIndex, item)
                prefs[FAVORITE_ORDER] = current.joinToString(",")
            }
        }
    }

    // Tally Counter
    val tallyCount: Flow<Int> = dataStore.data.map { it[TALLY_COUNT] ?: 0 }
    suspend fun setTallyCount(count: Int) { dataStore.edit { it[TALLY_COUNT] = count } }

    // Tip Calculator
    val tipPercentage: Flow<Int> = dataStore.data.map { it[TIP_PERCENTAGE] ?: 15 }
    suspend fun setTipPercentage(percent: Int) { dataStore.edit { it[TIP_PERCENTAGE] = percent } }

    // Unit Converter
    val ucCategory: Flow<Int> = dataStore.data.map { it[UC_CATEGORY] ?: 0 }
    val ucFromUnit: Flow<Int> = dataStore.data.map { it[UC_FROM_UNIT] ?: 0 }
    val ucToUnit: Flow<Int> = dataStore.data.map { it[UC_TO_UNIT] ?: 1 }
    suspend fun setUcSelections(category: Int, from: Int, to: Int) {
        dataStore.edit {
            it[UC_CATEGORY] = category
            it[UC_FROM_UNIT] = from
            it[UC_TO_UNIT] = to
        }
    }

    // Timer
    val timerHours: Flow<Int> = dataStore.data.map { it[TIMER_HOURS] ?: 0 }
    val timerMinutes: Flow<Int> = dataStore.data.map { it[TIMER_MINUTES] ?: 5 }
    val timerSeconds: Flow<Int> = dataStore.data.map { it[TIMER_SECONDS] ?: 0 }
    suspend fun setTimerDuration(h: Int, m: Int, s: Int) {
        dataStore.edit {
            it[TIMER_HOURS] = h
            it[TIMER_MINUTES] = m
            it[TIMER_SECONDS] = s
        }
    }
}
