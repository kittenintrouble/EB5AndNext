package com.eb5.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eb5.app.R
import com.eb5.app.data.model.Article
import com.eb5.app.data.model.ArticleStatus
import com.eb5.app.data.model.QuizProgress
import com.eb5.app.data.model.QuizTopic

@Composable
fun ProgressScreen(
    articles: List<Article>,
    articleStatuses: Map<Int, ArticleStatus>,
    favorites: Set<Int>,
    quizzes: List<QuizTopic>,
    quizProgress: Map<String, QuizProgress>
) {
    val completedArticles = articleStatuses.count { it.value == ArticleStatus.COMPLETED }
    val inProgressArticles = articleStatuses.count { it.value == ArticleStatus.IN_PROGRESS }
    val totalQuizzes = quizzes.size
    val completedQuizzes = quizzes.count { quiz ->
        (quizProgress[quiz.id]?.bestScore ?: 0) >= quiz.questions.size
    }
    val averageScore = if (quizzes.isNotEmpty()) {
        quizzes.map { quiz -> quizProgress[quiz.id]?.bestScore?.toDouble() ?: 0.0 }.average()
    } else {
        0.0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.progress_articles_title), style = MaterialTheme.typography.titleMedium)
                    Text(text = stringResource(R.string.progress_articles_total, articles.size))
                    Text(text = stringResource(R.string.progress_articles_completed, completedArticles))
                    Text(text = stringResource(R.string.progress_articles_in_progress, inProgressArticles))
                    Text(text = stringResource(R.string.progress_articles_favorites, favorites.size))
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.progress_quizzes_title), style = MaterialTheme.typography.titleMedium)
                    Text(text = stringResource(R.string.progress_quizzes_total, totalQuizzes))
                    Text(text = stringResource(R.string.progress_quizzes_completed, completedQuizzes))
                    Text(text = stringResource(R.string.progress_quizzes_average, String.format("%.1f", averageScore)))
                }
            }
        }
    }
}
