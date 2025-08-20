package com.example.testapp.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class   DataStoreProvider(context: Context) {

    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { File(context.filesDir, "datastore.preferences_pb") },
    )

    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val USER_AGE_KEY = stringPreferencesKey("user_age")

    suspend fun saveUser(userName: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = userName
        }
    }

    val getUserFlow: Flow<String> = dataStore.data
        .map { preferences ->
            delay(1000)
            preferences[USER_NAME_KEY] ?: "Nom inconnu"
        }

    suspend fun saveUserAge(userAge: String) {
        dataStore.edit { preferences ->
            preferences[USER_AGE_KEY] = userAge
        }
    }

    val getUserAgeFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[USER_AGE_KEY] ?: "Âge inconnu"
        }
}