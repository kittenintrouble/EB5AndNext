package com.eb5.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eb5.app.R
import com.eb5.app.data.model.Article
import com.eb5.app.data.model.ArticleStatus

@Composable
fun BaseScreen(
    articles: List<Article>,
    articleStatuses: Map<Int, ArticleStatus>,
    favorites: Set<Int>,
    onToggleFavorite: (Int) -> Unit,
    onToggleStatus: (Int) -> Unit,
    onArticleSelected: (Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedSubcategory by remember { mutableStateOf<String?>(null) }

    val groupedByCategory = remember(articles) { articles.groupBy { it.category } }
    val groupedBySubcategory = remember(selectedCategory, groupedByCategory) {
        selectedCategory?.let { category -> groupedByCategory[category]?.groupBy { it.subcategory } } ?: emptyMap()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.base_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (selectedCategory != null) {
                    AssistChip(
                        onClick = { selectedCategory = null; selectedSubcategory = null },
                        label = { Text(text = stringResource(R.string.base_change_category)) },
                        colors = AssistChipDefaults.assistChipColors()
                    )
                }
                if (selectedCategory != null && selectedSubcategory != null) {
                    AssistChip(
                        onClick = { selectedSubcategory = null },
                        label = { Text(text = stringResource(R.string.base_change_subcategory)) }
                    )
                }
            }
        }

        when {
            selectedCategory == null -> {
                items(groupedByCategory.keys.sorted()) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCategory = category
                                selectedSubcategory = null
                            }
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            selectedSubcategory == null -> {
                items(groupedBySubcategory.keys.sorted()) { subcategory ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSubcategory = subcategory }
                    ) {
                        Text(
                            text = subcategory,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            else -> {
                val articlesInSubcategory = groupedBySubcategory[selectedSubcategory].orEmpty()
                items(articlesInSubcategory, key = { it.id }) { article ->
                    ArticleRow(
                        article = article,
                        status = articleStatuses[article.id],
                        isFavorite = article.id in favorites,
                        onOpen = { onArticleSelected(article.id) },
                        onToggleFavorite = { onToggleFavorite(article.id) },
                        onToggleStatus = { onToggleStatus(article.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleRow(
    article: Article,
    status: ArticleStatus?,
    isFavorite: Boolean,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = article.title, style = MaterialTheme.typography.titleMedium)
            Text(text = article.shortDescription ?: article.description.take(120) + "…")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpen) { Text(text = stringResource(R.string.base_read)) }
                Button(onClick = onToggleFavorite) {
                    Text(text = if (isFavorite) stringResource(R.string.base_remove_favorite) else stringResource(R.string.base_add_favorite))
                }
                Button(onClick = onToggleStatus) {
                    val textRes = if (status == ArticleStatus.COMPLETED) R.string.base_mark_in_progress else R.string.base_mark_completed
                    Text(text = stringResource(textRes))
                }
            }
        }
    }
}
