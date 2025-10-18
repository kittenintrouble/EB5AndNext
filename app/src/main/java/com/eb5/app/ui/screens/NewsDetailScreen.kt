package com.eb5.app.ui.screens


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.eb5.app.ui.localization.stringResource
import com.eb5.app.R
import com.eb5.app.data.model.ArticleBlock
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.NewsArticle
import com.eb5.app.ui.news.NewsDetailUiState
import com.eb5.app.ui.components.DetailTopBar
import com.eb5.app.ui.theme.LocalAppLanguage
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun NewsDetailScreen(
    state: NewsDetailUiState,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold { innerPadding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(innerPadding))
            state.error != null -> ErrorState(
                message = state.error,
                onRetry = onRetry,
                modifier = Modifier.padding(innerPadding)
            )
            state.article != null -> ArticleContent(
                article = state.article,
                isFavorite = isFavorite,
                isLoading = state.isLoading,
                onToggleFavorite = onToggleFavorite,
                onBack = onBack,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = stringResource(R.string.news_loading))
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.news_error_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.news_retry))
        }
    }
}

@Composable
private fun ArticleContent(
    article: NewsArticle,
    isFavorite: Boolean,
    isLoading: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appLanguage = LocalAppLanguage.current
    val publishedLabel = remember(article.publishedAt, appLanguage) { formatPublishedDate(article.publishedAt, appLanguage) }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        DetailTopBar(
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            onBack = onBack,
            favoriteEnabled = !isLoading,
            favoriteOnContentDescription = R.string.article_remove_favorite,
            favoriteOffContentDescription = R.string.article_add_favorite
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = publishedLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            article.heroImage?.let { hero ->
                item {
                    Card(shape = MaterialTheme.shapes.large) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(hero.url)
                                .crossfade(true)
                                .build(),
                            contentDescription = hero.alt,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (!hero.caption.isNullOrBlank()) {
                            Text(
                                text = hero.caption,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
            if (!article.shortDescription.isNullOrBlank()) {
                item {
                    Text(
                        text = article.shortDescription,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            items(article.article) { block ->
                ArticleBlockView(block = block)
            }
            article.meta?.let { meta ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        meta.author?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = stringResource(R.string.news_author, it),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        meta.source?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = stringResource(R.string.news_source, it),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ArticleBlockView(block: ArticleBlock) {
    when (block.type.lowercase(Locale.getDefault())) {
        "heading" -> {
            Text(
                text = block.text.orEmpty(),
                style = when (block.level) {
                    1 -> MaterialTheme.typography.headlineSmall
                    2 -> MaterialTheme.typography.titleLarge
                    else -> MaterialTheme.typography.titleMedium
                },
                fontWeight = FontWeight.SemiBold
            )
        }

        "subheading" -> {
            Text(
                text = block.text.orEmpty(),
                style = when (block.level) {
                    1 -> MaterialTheme.typography.titleLarge
                    2 -> MaterialTheme.typography.titleMedium
                    else -> MaterialTheme.typography.titleSmall
                },
                fontWeight = FontWeight.SemiBold
            )
        }

        "paragraph" -> {
            Text(text = block.text.orEmpty(), style = MaterialTheme.typography.bodyLarge)
        }

        "quote" -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "“${block.quote.orEmpty()}”",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                block.author?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        "image" -> {
            block.url?.let { url ->
                Card(shape = MaterialTheme.shapes.medium) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = block.alt,
                        modifier = Modifier.fillMaxWidth()
                    )
                    block.caption?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp)
                        )
                    }
                }
            }
        }

        "callout" -> {
            CalloutBlock(block = block)
        }

        "list" -> {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                block.items.orEmpty().forEach { item ->
                    Text(
                        text = "• $item",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        "table" -> {
            val headers = block.headers.orEmpty()
            val rows = block.rows.orEmpty()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
            ) {
                if (headers.isNotEmpty()) {
                    RowWithCells(headers, header = true)
                }
                rows.forEach { row ->
                    RowWithCells(row, header = false)
                }
            }
        }

        else -> {
            block.text?.takeIf { it.isNotBlank() }?.let {
                Text(text = it, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun CalloutBlock(block: ArticleBlock) {
    val variant = block.variant?.lowercase(Locale.getDefault()) ?: "info"
    val colorScheme = MaterialTheme.colorScheme
    val style = when (variant) {
        "success" -> CalloutStyle(
            containerColor = colorScheme.tertiaryContainer,
            contentColor = colorScheme.onTertiaryContainer,
            accentColor = colorScheme.tertiary,
            icon = Icons.Outlined.CheckCircle
        )

        "warning" -> CalloutStyle(
            containerColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer,
            accentColor = colorScheme.secondary,
            icon = Icons.Outlined.Warning
        )

        "danger", "error" -> CalloutStyle(
            containerColor = colorScheme.errorContainer,
            contentColor = colorScheme.onErrorContainer,
            accentColor = colorScheme.error,
            icon = Icons.Outlined.ErrorOutline
        )

        else -> CalloutStyle(
            containerColor = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer,
            accentColor = colorScheme.primary,
            icon = Icons.Outlined.Info
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = style.containerColor),
        border = BorderStroke(1.dp, style.accentColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.accentColor,
                modifier = Modifier.padding(top = 4.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                block.title?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = style.contentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                block.text?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = style.contentColor
                    )
                }
            }
        }
    }
}

private data class CalloutStyle(
    val containerColor: Color,
    val contentColor: Color,
    val accentColor: Color,
    val icon: ImageVector
)

@Composable
private fun RowWithCells(values: List<String>, header: Boolean) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (header) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        values.forEach { cell ->
            Text(
                text = cell,
                style = if (header) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
                fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

private fun formatPublishedDate(raw: String, language: AppLanguage): String {
    return runCatching {
        val parsed = OffsetDateTime.parse(raw)
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(Locale.forLanguageTag(language.tag))
            .format(parsed)
    }.getOrElse { raw }
}
