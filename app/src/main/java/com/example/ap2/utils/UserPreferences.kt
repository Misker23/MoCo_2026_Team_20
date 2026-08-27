package com.example.ap2.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        private val KEY_LAST_USERNAME = stringPreferencesKey("last_username")
    }

    val lastUsernameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_USERNAME] ?: ""
    }

    suspend fun saveLastUsername(username: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_USERNAME] = username
        }
    }
}