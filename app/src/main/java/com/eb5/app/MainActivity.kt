package com.eb5.app

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eb5.app.ui.AppViewModel
import com.eb5.app.ui.EB5App
import com.eb5.app.R
import com.eb5.app.ui.navigation.AppDestination
import com.eb5.app.AppMessagingService
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels {
        val app = application as EB5Application
        viewModelFactory {
            initializer {
                AppViewModel(
                    preferencesRepository = app.container.userPreferencesRepository,
                    contentRepository = app.container.contentRepository,
                    quizRepository = app.container.quizRepository,
                    projectRepository = app.container.projectRepository,
                    newsRepository = app.container.newsRepository
                )
            }
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(
                    this,
                    getString(R.string.notification_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                subscribeToNewsTopic()
                subscribeToProjectsTopic()
            }
        }

    private var newsUpdateReceiver: BroadcastReceiver? = null
    private var projectUpdateReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        subscribeToNewsTopic()
        subscribeToProjectsTopic()
        registerNewsUpdates()
        registerProjectUpdates()
        setContent {
            EB5App(viewModel = appViewModel)
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onDestroy() {
        newsUpdateReceiver?.let { unregisterReceiver(it) }
        newsUpdateReceiver = null
        projectUpdateReceiver?.let { unregisterReceiver(it) }
        projectUpdateReceiver = null
        super.onDestroy()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Already granted
                subscribeToNewsTopic()
                subscribeToProjectsTopic()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                Toast.makeText(
                    this,
                    getString(R.string.notification_permission_rationale),
                    Toast.LENGTH_LONG
                ).show()
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

            else -> requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun registerNewsUpdates() {
        if (newsUpdateReceiver != null) return
        newsUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                appViewModel.refreshNews()
            }
        }
        val filter = IntentFilter(AppMessagingService.ACTION_REFRESH_NEWS)
        registerReceiver(
            this,
            newsUpdateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun registerProjectUpdates() {
        if (projectUpdateReceiver != null) return
        projectUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val projectId = intent?.getStringExtra(AppMessagingService.EXTRA_PROJECT_ID)
                val language = intent?.getStringExtra(AppMessagingService.EXTRA_LANGUAGE)
                appViewModel.refreshProjects()
                if (!projectId.isNullOrBlank()) {
                    appViewModel.openProject(projectId, language, AppDestination.Projects.route)
                }
            }
        }
        val filter = IntentFilter(AppMessagingService.ACTION_REFRESH_PROJECTS)
        registerReceiver(
            this,
            projectUpdateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun subscribeToNewsTopic() {
        FirebaseMessaging.getInstance()
            .subscribeToTopic(NEWS_TOPIC)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to $NEWS_TOPIC")
                } else {
                    Log.w(TAG, "Failed to subscribe to $NEWS_TOPIC", task.exception)
                }
            }
    }

    private fun subscribeToProjectsTopic() {
        FirebaseMessaging.getInstance()
            .subscribeToTopic(PROJECTS_TOPIC)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to $PROJECTS_TOPIC")
                } else {
                    Log.w(TAG, "Failed to subscribe to $PROJECTS_TOPIC", task.exception)
                }
            }
    }

    private fun handleIntent(intent: Intent?) {
        val articleId = intent?.getStringExtra(EXTRA_TARGET_NEWS_ID)
        if (!articleId.isNullOrBlank()) {
            val language = intent.getStringExtra(EXTRA_TARGET_NEWS_LANGUAGE)
            appViewModel.refreshNews()
            appViewModel.openNewsArticle(articleId, language, AppDestination.Progress.route)
            intent.removeExtra(EXTRA_TARGET_NEWS_ID)
            intent.removeExtra(EXTRA_TARGET_NEWS_LANGUAGE)
        }
        val projectId = intent?.getStringExtra(EXTRA_TARGET_PROJECT_ID)
        if (!projectId.isNullOrBlank()) {
            val language = intent.getStringExtra(EXTRA_TARGET_PROJECT_LANGUAGE)
            appViewModel.refreshProjects()
            appViewModel.openProject(projectId, language, AppDestination.Projects.route)
            intent.removeExtra(EXTRA_TARGET_PROJECT_ID)
            intent.removeExtra(EXTRA_TARGET_PROJECT_LANGUAGE)
        }
    }

    companion object {
        private const val NEWS_TOPIC = "eb5_news"
        private const val PROJECTS_TOPIC = "eb5_projects"
        private const val TAG = "MainActivity"
        const val EXTRA_TARGET_NEWS_ID = "extra_target_news_id"
        const val EXTRA_TARGET_NEWS_LANGUAGE = "extra_target_news_lang"
        const val EXTRA_TARGET_PROJECT_ID = "extra_target_project_id"
        const val EXTRA_TARGET_PROJECT_LANGUAGE = "extra_target_project_lang"
    }
}
