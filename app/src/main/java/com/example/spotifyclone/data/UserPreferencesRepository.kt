package com.example.spotifyclone.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val USER_UID = stringPreferencesKey("user_uid")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    val userUid: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_UID]
        }

    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL]
        }

    suspend fun saveUserSession(uid: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_UID] = uid
            preferences[USER_EMAIL] = email
        }
    }

    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_UID)
            preferences.remove(USER_EMAIL)
        }
    }
}
