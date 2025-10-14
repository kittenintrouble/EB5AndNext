package com.eb5.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.eb5.app.R
import com.eb5.app.data.model.Article
import com.eb5.app.data.model.ArticleStatus
import com.eb5.app.data.model.Project
import com.eb5.app.data.model.NewsArticle
import java.text.NumberFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    articles: List<Article>,
    articleStatuses: Map<Int, ArticleStatus>,
    favorites: Set<Int>,
    news: List<NewsArticle>,
    newsFavorites: Set<String>,
    projects: List<Project>,
    projectFavorites: Set<String>,
    onExploreArticles: () -> Unit,
    onOpenArticle: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onOpenNews: (NewsArticle) -> Unit,
    onToggleNewsFavorite: (String) -> Unit,
    onOpenProject: (Project) -> Unit,
    onToggleProjectFavorite: (String) -> Unit
) {
    val completedArticles = articleStatuses.count { it.value == ArticleStatus.COMPLETED }
    val favoriteArticles = remember(articles, favorites) {
        articles.filter { it.id in favorites }
            .sortedWith(compareBy<Article> { it.dayNumber ?: Int.MAX_VALUE }.thenBy { it.id })
    }
    val favoriteNews = remember(news, newsFavorites) {
        news.filter { newsFavorites.contains(it.id) }
    }
    val context = LocalContext.current
    val newsDateFormatter = remember {
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    }
    val favoriteProjects = remember(projects, projectFavorites) {
        projects.filter { projectFavorites.contains(it.id) }
    }
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.US)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    val progress = if (articles.isNotEmpty()) completedArticles.toFloat() / articles.size.toFloat() else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onExploreArticles, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(R.string.home_explore_articles))
                    }
                }
            }
        }
        if (favoriteArticles.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.home_favorites_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(favoriteArticles, key = { it.id }) { article ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onOpenArticle(article.id) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    text = article.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val status = articleStatuses[article.id]
                                val statusText = when (status) {
                                    ArticleStatus.COMPLETED -> stringResource(R.string.base_mark_completed)
                                    ArticleStatus.IN_PROGRESS -> stringResource(R.string.base_mark_in_progress)
                                    else -> stringResource(R.string.home_favorite_status_viewed)
                                }
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            IconButton(onClick = { onToggleFavorite(article.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = stringResource(R.string.article_remove_favorite),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(
                            text = article.shortDescription ?: article.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        if (favoriteNews.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.home_favorites_news_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(favoriteNews, key = { "news_${it.id}" }) { news ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenNews(news) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val publishedLabel = remember(news.id, news.publishedAt) {
                            runCatching {
                                val parsed = OffsetDateTime.parse(news.publishedAt)
                                newsDateFormatter.format(parsed)
                            }.getOrElse { news.publishedAt }
                        }
                        news.heroImage?.let { hero ->
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(hero.url)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = hero.alt,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = news.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            val isFavorite = newsFavorites.contains(news.id)
                            IconButton(onClick = { onToggleNewsFavorite(news.id) }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = stringResource(
                                        if (isFavorite) R.string.article_remove_favorite else R.string.article_add_favorite
                                    ),
                                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.news_published_on, publishedLabel),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = news.shortDescription ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        if (favoriteProjects.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.home_favorites_projects_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(favoriteProjects, key = { "project_${it.id}" }) { project ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenProject(project) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(project.heroImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = project.title,
                            placeholder = painterResource(id = R.drawable.bg_us_flag),
                            error = painterResource(id = R.drawable.bg_us_flag),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = project.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    project.type?.takeIf { it.isNotBlank() }?.let { HomeProjectBadge(text = it) }
                                    project.status?.takeIf { it.isNotBlank() }?.let { HomeProjectBadge(text = it) }
                                }
                            }
                            IconButton(onClick = { onToggleProjectFavorite(project.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = stringResource(R.string.project_remove_favorite),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        project.location?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = stringResource(R.string.project_location, it),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val description = project.shortDescription?.takeIf { it.isNotBlank() }
                            ?: project.fullDescription?.takeIf { it.isNotBlank() }
                        description?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        val minInvestment = project.financials?.minInvestment ?: project.minInvestmentUsd
                        val minInvestmentText = minInvestment?.let { currencyFormatter.format(it) } ?: "N/A"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.project_investment, minInvestmentText),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            project.tea?.designation
                                ?.takeIf { it.isNotBlank() }
                                ?.let {
                                    Text(
                                        text = stringResource(R.string.project_tea_status, it),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                        }
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

@Composable
private fun HomeProjectBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
