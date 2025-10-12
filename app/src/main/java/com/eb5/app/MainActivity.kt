package com.eb5.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eb5.app.ui.AppViewModel
import com.eb5.app.ui.EB5App

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels {
        val app = application as EB5Application
        viewModelFactory {
            initializer {
                AppViewModel(
                    preferencesRepository = app.container.userPreferencesRepository,
                    contentRepository = app.container.contentRepository,
                    quizRepository = app.container.quizRepository,
                    projectRepository = app.container.projectRepository
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EB5App(viewModel = appViewModel)
        }
    }
}
