package com.eb5.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.eb5.app.data.AppContainer

private val Application.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class EB5Application : Application() {

    val container: AppContainer by lazy {
        AppContainer(this, dataStore)
    }
}
