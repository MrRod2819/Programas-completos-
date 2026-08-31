package com.ctos.player.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ctos.player.data.model.RingStyle
import com.ctos.player.data.model.ThemePalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("ctos_settings")

/** Persists the selected palette, ring style and favourite tracks. */
class SettingsRepository(private val context: Context) {

    val palette: Flow<ThemePalette> = context.dataStore.data.map {
        ThemePalette.fromId(it[KEY_PALETTE])
    }

    val ringStyle: Flow<RingStyle> = context.dataStore.data.map {
        RingStyle.fromId(it[KEY_RING])
    }

    val favorites: Flow<Set<Long>> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAVORITES].orEmpty().mapNotNull(String::toLongOrNull).toSet()
    }

    suspend fun setPalette(palette: ThemePalette) {
        context.dataStore.edit { it[KEY_PALETTE] = palette.id }
    }

    suspend fun setRingStyle(style: RingStyle) {
        context.dataStore.edit { it[KEY_RING] = style.id }
    }

    suspend fun toggleFavorite(songId: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FAVORITES].orEmpty().toMutableSet()
            val key = songId.toString()
            if (!current.remove(key)) current += key
            prefs[KEY_FAVORITES] = current
        }
    }

    private companion object {
        val KEY_PALETTE = stringPreferencesKey("palette")
        val KEY_RING = stringPreferencesKey("ring_style")
        val KEY_FAVORITES = stringSetPreferencesKey("favorites")
    }
}
