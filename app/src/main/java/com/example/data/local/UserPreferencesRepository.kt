package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bharattube_prefs")

data class UserPreferences(
    val isDarkTheme: Boolean = true,
    val activeUserId: String = "user_founder",
    val activeChannelId: String = "ch_bharattube_tech",
    val preferredLanguage: String = "en",
    val preferredCountry: String = "IN",
    val historyPaused: Boolean = false,
    val restrictedMode: Boolean = false,
    val pushNotificationsOptIn: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val ACTIVE_USER_ID = stringPreferencesKey("active_user_id")
        val ACTIVE_CHANNEL_ID = stringPreferencesKey("active_channel_id")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val PREFERRED_COUNTRY = stringPreferencesKey("preferred_country")
        val HISTORY_PAUSED = booleanPreferencesKey("history_paused")
        val RESTRICTED_MODE = booleanPreferencesKey("restricted_mode")
        val PUSH_OPT_IN = booleanPreferencesKey("push_opt_in")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            isDarkTheme = prefs[Keys.DARK_THEME] ?: true,
            activeUserId = prefs[Keys.ACTIVE_USER_ID] ?: "user_founder",
            activeChannelId = prefs[Keys.ACTIVE_CHANNEL_ID] ?: "ch_bharattube_tech",
            preferredLanguage = prefs[Keys.PREFERRED_LANGUAGE] ?: "en",
            preferredCountry = prefs[Keys.PREFERRED_COUNTRY] ?: "IN",
            historyPaused = prefs[Keys.HISTORY_PAUSED] ?: false,
            restrictedMode = prefs[Keys.RESTRICTED_MODE] ?: false,
            pushNotificationsOptIn = prefs[Keys.PUSH_OPT_IN] ?: true
        )
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setActiveUser(uid: String) {
        context.dataStore.edit { it[Keys.ACTIVE_USER_ID] = uid }
    }

    suspend fun setActiveChannel(channelId: String) {
        context.dataStore.edit { it[Keys.ACTIVE_CHANNEL_ID] = channelId }
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { it[Keys.PREFERRED_LANGUAGE] = languageCode }
    }

    suspend fun setCountry(countryCode: String) {
        context.dataStore.edit { it[Keys.PREFERRED_COUNTRY] = countryCode }
    }

    suspend fun setHistoryPaused(paused: Boolean) {
        context.dataStore.edit { it[Keys.HISTORY_PAUSED] = paused }
    }

    suspend fun setRestrictedMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.RESTRICTED_MODE] = enabled }
    }

    suspend fun setPushOptIn(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PUSH_OPT_IN] = enabled }
    }
}
