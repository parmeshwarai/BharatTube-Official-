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
    val pushNotificationsOptIn: Boolean = true,
    val notifyUploads: Boolean = true,
    val notifyComments: Boolean = true,
    val notifyLive: Boolean = true,
    val playbackAutoPlayNext: Boolean = true,
    val playbackDefaultQuality: String = "1080p",
    val playbackCaptionsEnabled: Boolean = true,
    val playbackDataSaver: Boolean = false,
    val downloadsWifiOnly: Boolean = true,
    val downloadsQuality: String = "720p",
    val downloadedVideoIdsCsv: String = "",
    val privacySubscriptionsPrivate: Boolean = false,
    val privacyPlaylistsPrivate: Boolean = false,
    val isSignedOut: Boolean = false
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
        val NOTIFY_UPLOADS = booleanPreferencesKey("notify_uploads")
        val NOTIFY_COMMENTS = booleanPreferencesKey("notify_comments")
        val NOTIFY_LIVE = booleanPreferencesKey("notify_live")
        val PLAYBACK_AUTOPLAY = booleanPreferencesKey("playback_autoplay")
        val PLAYBACK_QUALITY = stringPreferencesKey("playback_quality")
        val PLAYBACK_CAPTIONS = booleanPreferencesKey("playback_captions")
        val PLAYBACK_DATA_SAVER = booleanPreferencesKey("playback_data_saver")
        val DOWNLOADS_WIFI_ONLY = booleanPreferencesKey("downloads_wifi_only")
        val DOWNLOADS_QUALITY = stringPreferencesKey("downloads_quality")
        val DOWNLOADED_VIDEO_IDS = stringPreferencesKey("downloaded_video_ids")
        val PRIVACY_SUBS = booleanPreferencesKey("privacy_subs")
        val PRIVACY_PLAYLISTS = booleanPreferencesKey("privacy_playlists")
        val IS_SIGNED_OUT = booleanPreferencesKey("is_signed_out")
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
            pushNotificationsOptIn = prefs[Keys.PUSH_OPT_IN] ?: true,
            notifyUploads = prefs[Keys.NOTIFY_UPLOADS] ?: true,
            notifyComments = prefs[Keys.NOTIFY_COMMENTS] ?: true,
            notifyLive = prefs[Keys.NOTIFY_LIVE] ?: true,
            playbackAutoPlayNext = prefs[Keys.PLAYBACK_AUTOPLAY] ?: true,
            playbackDefaultQuality = prefs[Keys.PLAYBACK_QUALITY] ?: "1080p",
            playbackCaptionsEnabled = prefs[Keys.PLAYBACK_CAPTIONS] ?: true,
            playbackDataSaver = prefs[Keys.PLAYBACK_DATA_SAVER] ?: false,
            downloadsWifiOnly = prefs[Keys.DOWNLOADS_WIFI_ONLY] ?: true,
            downloadsQuality = prefs[Keys.DOWNLOADS_QUALITY] ?: "720p",
            downloadedVideoIdsCsv = prefs[Keys.DOWNLOADED_VIDEO_IDS] ?: "",
            privacySubscriptionsPrivate = prefs[Keys.PRIVACY_SUBS] ?: false,
            privacyPlaylistsPrivate = prefs[Keys.PRIVACY_PLAYLISTS] ?: false,
            isSignedOut = prefs[Keys.IS_SIGNED_OUT] ?: false
        )
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setActiveUser(uid: String) {
        context.dataStore.edit {
            it[Keys.ACTIVE_USER_ID] = uid
            it[Keys.IS_SIGNED_OUT] = false
        }
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

    suspend fun setNotifyUploads(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFY_UPLOADS] = enabled }
    }

    suspend fun setNotifyComments(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFY_COMMENTS] = enabled }
    }

    suspend fun setNotifyLive(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFY_LIVE] = enabled }
    }

    suspend fun setPlaybackAutoPlayNext(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PLAYBACK_AUTOPLAY] = enabled }
    }

    suspend fun setPlaybackDefaultQuality(quality: String) {
        context.dataStore.edit { it[Keys.PLAYBACK_QUALITY] = quality }
    }

    suspend fun setPlaybackCaptionsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PLAYBACK_CAPTIONS] = enabled }
    }

    suspend fun setPlaybackDataSaver(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PLAYBACK_DATA_SAVER] = enabled }
    }

    suspend fun setDownloadsWifiOnly(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DOWNLOADS_WIFI_ONLY] = enabled }
    }

    suspend fun setDownloadsQuality(quality: String) {
        context.dataStore.edit { it[Keys.DOWNLOADS_QUALITY] = quality }
    }

    suspend fun toggleDownloadedVideo(videoId: String): Boolean {
        var added = false
        context.dataStore.edit { prefs ->
            val current = (prefs[Keys.DOWNLOADED_VIDEO_IDS] ?: "")
                .split(",")
                .filter { it.isNotBlank() }
                .toMutableSet()
            added = if (videoId in current) {
                current.remove(videoId)
                false
            } else {
                current.add(videoId)
                true
            }
            prefs[Keys.DOWNLOADED_VIDEO_IDS] = current.joinToString(",")
        }
        return added
    }

    suspend fun clearDownloadedVideos() {
        context.dataStore.edit { it[Keys.DOWNLOADED_VIDEO_IDS] = "" }
    }

    suspend fun setPrivacySubscriptionsPrivate(isPrivate: Boolean) {
        context.dataStore.edit { it[Keys.PRIVACY_SUBS] = isPrivate }
    }

    suspend fun setPrivacyPlaylistsPrivate(isPrivate: Boolean) {
        context.dataStore.edit { it[Keys.PRIVACY_PLAYLISTS] = isPrivate }
    }

    suspend fun setSignedOut(signedOut: Boolean) {
        context.dataStore.edit { it[Keys.IS_SIGNED_OUT] = signedOut }
    }
}
