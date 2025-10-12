package com.eb5.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eb5.app.R
import com.eb5.app.data.model.Article
import com.eb5.app.data.model.ArticleStatus
import com.eb5.app.data.model.QuizProgress
import com.eb5.app.data.model.QuizTopic

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    articles: List<Article>,
    articleStatuses: Map<Int, ArticleStatus>,
    favorites: Set<Int>,
    quizzes: List<QuizTopic>,
    quizProgress: Map<String, QuizProgress>,
    onExploreArticles: () -> Unit,
    onOpenQuiz: (QuizTopic) -> Unit
) {
    val completedArticles = articleStatuses.count { it.value == ArticleStatus.COMPLETED }
    val inProgressArticles = articleStatuses.count { it.value == ArticleStatus.IN_PROGRESS }
    val completedQuizzes = quizzes.count { quiz ->
        (quizProgress[quiz.id]?.bestScore ?: 0) >= quiz.questions.size
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.home_greeting),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.home_articles_title), style = MaterialTheme.typography.titleMedium)
                    Text(text = stringResource(R.string.home_articles_summary, completedArticles, articles.size))
                    Text(text = stringResource(R.string.home_articles_in_progress, inProgressArticles))
                    Text(text = stringResource(R.string.home_articles_favorites, favorites.size))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onExploreArticles, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(R.string.home_explore_articles))
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.home_quizzes_title), style = MaterialTheme.typography.titleMedium)
                    Text(text = stringResource(R.string.home_quizzes_summary, completedQuizzes, quizzes.size))
                    Button(
                        onClick = { quizzes.firstOrNull()?.let(onOpenQuiz) },
                        enabled = quizzes.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.home_take_quiz))
                    }
                }
            }
        }
        if (articles.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = stringResource(R.string.home_empty_title), style = MaterialTheme.typography.titleMedium)
                        Text(text = stringResource(R.string.home_empty_description))
                        Button(onClick = onExploreArticles, modifier = Modifier.fillMaxWidth()) {
                            Text(text = stringResource(R.string.home_explore_articles))
                        }
                    }
                }
            }
        }
    }
}
