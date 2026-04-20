package com.example.diplomproject.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_preferences")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val tokenKey = stringPreferencesKey("jwt_token")
    private val guestSessionKey = stringPreferencesKey("guest_session_key")

    val tokenFlow: Flow<String?> = context.authDataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences: Preferences -> preferences[tokenKey] }

    val guestSessionKeyFlow: Flow<String?> = context.authDataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences: Preferences -> preferences[guestSessionKey] }

    suspend fun getToken(): String? = tokenFlow.firstOrNull()

    suspend fun getGuestSessionKey(): String? = guestSessionKeyFlow.firstOrNull()

    suspend fun saveToken(token: String) {
        context.authDataStore.edit { preferences ->
            preferences[tokenKey] = token
            preferences.remove(guestSessionKey)
        }
    }

    suspend fun saveGuestSessionKey(key: String) {
        context.authDataStore.edit { preferences ->
            preferences[guestSessionKey] = key
        }
    }

    suspend fun clearGuestSessionKey() {
        context.authDataStore.edit { preferences ->
            preferences.remove(guestSessionKey)
        }
    }

    suspend fun clearSession() {
        context.authDataStore.edit { preferences ->
            preferences.remove(tokenKey)
            preferences.remove(guestSessionKey)
        }
    }
}
