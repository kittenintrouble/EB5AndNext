package com.eb5.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eb5.app.R
import com.eb5.app.data.model.Article
import com.eb5.app.data.model.ArticleStatus

@Composable
fun ArticleDetailScreen(
    article: Article,
    status: ArticleStatus,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleStatus: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = article.title, style = MaterialTheme.typography.headlineSmall)
                Text(text = article.description, style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
                    Text(text = if (isFavorite) stringResource(R.string.article_remove_favorite) else stringResource(R.string.article_add_favorite))
                }
                Button(onClick = onToggleStatus, modifier = Modifier.fillMaxWidth()) {
                    val textRes = if (status == ArticleStatus.COMPLETED) R.string.article_mark_in_progress else R.string.article_mark_completed
                    Text(text = stringResource(textRes))
                }
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.action_back))
                }
            }
        }
        if (article.examples.isNotEmpty()) {
            item {
                Text(text = stringResource(R.string.article_examples_title), style = MaterialTheme.typography.titleMedium)
            }
            items(article.examples) { example ->
                Text(text = example)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
