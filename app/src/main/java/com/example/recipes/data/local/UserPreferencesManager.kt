package com.example.recipes.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val USER_TOKEN = stringPreferencesKey("user_token")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val userToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_TOKEN]
    }

    val userName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_NAME]
    }

    val userEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_EMAIL]
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    suspend fun saveUserData(username: String, email: String, token: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME] = username
            preferences[USER_EMAIL] = email
            preferences[USER_TOKEN] = token
            preferences[IS_LOGGED_IN] = true
        }
    }

    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
