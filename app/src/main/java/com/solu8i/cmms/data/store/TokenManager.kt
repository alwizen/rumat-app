package com.solu8i.cmms.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cmms_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ROLES_KEY = stringPreferencesKey("user_roles")
    }

    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    val userName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME_KEY]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_EMAIL_KEY]
    }

    val userRoles: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_ROLES_KEY]
    }

    suspend fun saveAuthData(token: String, name: String, email: String, roles: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_NAME_KEY] = name
            prefs[USER_EMAIL_KEY] = email
            prefs[USER_ROLES_KEY] = roles.joinToString(",")
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
