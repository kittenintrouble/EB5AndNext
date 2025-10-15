package com.eb5.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class AppMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val type = data["type"].orEmpty()

        when {
            type.equals("project", ignoreCase = true) ||
                data.containsKey("project_id") ||
                data.containsKey("projectId") -> handleProjectMessage(message)
            else -> handleNewsMessage(message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Optionally forward token to backend if needed.
    }

    private fun handleProjectMessage(message: RemoteMessage) {
        val data = message.data
        val projectId = data["projectId"] ?: data["project_id"]
        val title = message.notification?.title.orEmpty()
            .ifBlank { data["title"].orEmpty() }
            .ifBlank { getString(R.string.app_name) }
        val body = message.notification?.body.orEmpty()
            .ifBlank { data["body"].orEmpty() }
        val language = data["lang"].orEmpty().ifBlank { "en" }

        Log.d(TAG, "Received project push for id=$projectId lang=$language")
        if (!projectId.isNullOrBlank()) {
            showProjectNotification(projectId, language, title, body)
        } else {
            Log.w(TAG, "Project push missing project id, skipping notification but refreshing list.")
        }
        notifyProjectUpdate(projectId, language)
    }

    private fun handleNewsMessage(message: RemoteMessage) {
        val data = message.data
        val articleId = data["article_id"] ?: data["articleId"]
        val title = message.notification?.title.orEmpty()
            .ifBlank { data["title"].orEmpty() }
            .ifBlank { getString(R.string.app_name) }
        val body = message.notification?.body.orEmpty()
            .ifBlank { data["body"].orEmpty() }
        val language = data["lang"].orEmpty().ifBlank { "en" }
        val publishedAt = data["published_at"].orEmpty()

        Log.d(TAG, "Received news push for id=$articleId lang=$language")
        if (!articleId.isNullOrBlank()) {
            showNewsNotification(articleId, language, title, body, publishedAt)
        } else {
            Log.w(TAG, "News push missing article id, skipping notification but refreshing list.")
        }
        notifyNewsUpdate(articleId, language)
    }

    private fun showProjectNotification(
        projectId: String,
        language: String,
        title: String,
        body: String
    ) {
        ensureChannelExists(PROJECTS_CHANNEL_ID, getString(R.string.title_project_detail))

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_TARGET_PROJECT_ID, projectId)
            putExtra(MainActivity.EXTRA_TARGET_PROJECT_LANGUAGE, language)
        }

        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                projectId.hashCode(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(this, PROJECTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body.takeIf { it.isNotBlank() } ?: getString(R.string.title_project_detail))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(body.takeIf { it.isNotBlank() } ?: title)
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this).notify(projectId.hashCode(), notification)
        Log.d(TAG, "Displayed project notification for id=$projectId")
    }

    private fun showNewsNotification(
        articleId: String,
        language: String,
        title: String,
        body: String,
        publishedAt: String
    ) {
        ensureChannelExists(NEWS_CHANNEL_ID, getString(R.string.news_detail_title))

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_TARGET_NEWS_ID, articleId)
            putExtra(MainActivity.EXTRA_TARGET_NEWS_LANGUAGE, language)
        }

        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                articleId.hashCode(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val (formattedDate, eventTime) = formatPublishedDate(publishedAt, language)
        val summary = listOfNotNull(formattedDate, body.takeIf { it.isNotBlank() })
            .joinToString("\n")
            .ifBlank { null }
        val contentText = formattedDate ?: body.takeIf { it.isNotBlank() } ?: getString(R.string.news_detail_title)

        val notification = NotificationCompat.Builder(this, NEWS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(summary ?: title)
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setWhen(eventTime ?: System.currentTimeMillis())
            .setShowWhen(true)
            .build()

        NotificationManagerCompat.from(this).notify(articleId.hashCode(), notification)
        Log.d(TAG, "Displayed news notification for id=$articleId")
    }

    private fun notifyProjectUpdate(projectId: String?, language: String) {
        val intent = Intent(ACTION_REFRESH_PROJECTS).apply {
            setPackage(packageName)
            putExtra(EXTRA_LANGUAGE, language)
            if (!projectId.isNullOrBlank()) {
                putExtra(EXTRA_PROJECT_ID, projectId)
            }
        }
        sendBroadcast(intent)
        Log.d(TAG, "Broadcasted project update for id=$projectId")
    }

    private fun notifyNewsUpdate(articleId: String?, language: String) {
        val intent = Intent(ACTION_REFRESH_NEWS).apply {
            setPackage(packageName)
            putExtra(EXTRA_LANGUAGE, language)
            if (!articleId.isNullOrBlank()) {
                putExtra(EXTRA_ARTICLE_ID, articleId)
            }
        }
        sendBroadcast(intent)
        Log.d(TAG, "Broadcasted news update for id=$articleId")
    }

    private fun formatPublishedDate(
        publishedAt: String,
        languageCode: String
    ): Pair<String?, Long?> {
        if (publishedAt.isBlank()) return null to null
        return runCatching {
            val instant = OffsetDateTime.parse(publishedAt).toInstant()
            val locale = Locale(languageCode.ifBlank { Locale.getDefault().language })
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
            val formatted = formatter.format(instant.atZone(ZoneId.systemDefault()))
            formatted to instant.toEpochMilli()
        }.getOrElse {
            val fallbackInstant = runCatching { Instant.parse(publishedAt) }.getOrNull()
            val formatted = fallbackInstant?.let { instant ->
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    .withLocale(Locale.getDefault())
                    .format(instant.atZone(ZoneId.systemDefault()))
            }
            formatted to fallbackInstant?.toEpochMilli()
        }
    }

    private fun ensureChannelExists(channelId: String, name: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            name,
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "AppMessaging"
        private const val NEWS_CHANNEL_ID = "news_channel"
        private const val PROJECTS_CHANNEL_ID = "projects_channel"

        const val ACTION_REFRESH_NEWS = "com.eb5.app.action.REFRESH_NEWS"
        const val ACTION_REFRESH_PROJECTS = "com.eb5.app.action.REFRESH_PROJECTS"
        const val EXTRA_ARTICLE_ID = "extra_article_id"
        const val EXTRA_PROJECT_ID = "extra_project_id"
        const val EXTRA_LANGUAGE = "extra_language"
    }
}
