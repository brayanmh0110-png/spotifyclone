package com.example.spotifyclone.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * UserPreferencesRepository: Encargado de guardar datos "pequeños" en la memoria del teléfono.
 * Usamos Jetpack DataStore para persistir el ID del usuario y que no tenga que loguearse cada vez que abre la app.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        // Llaves para identificar los datos guardados
        val USER_UID = stringPreferencesKey("user_uid")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    /**
     * Flujo que emite el UID guardado. Si no hay nada, emite null.
     */
    val userUid: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_UID] }

    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_EMAIL] }

    /**
     * Guarda físicamente los datos del usuario en el almacenamiento privado de la app.
     */
    suspend fun saveUserSession(uid: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_UID] = uid
            preferences[USER_EMAIL] = email
        }
    }

    /**
     * Borra los datos al cerrar sesión.
     */
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_UID)
            preferences.remove(USER_EMAIL)
        }
    }
}
