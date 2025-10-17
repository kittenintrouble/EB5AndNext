package com.eb5.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eb5.app.R
import com.eb5.app.data.model.Article
import com.eb5.app.data.model.ArticleStatus
import com.eb5.app.ui.AppViewModel
import android.util.Log
import kotlin.math.ceil
import kotlin.math.max

private data class CategorySummary(
    val name: String,
    val articles: List<Article>,
    val completedCount: Int,
    val totalCount: Int,
    val resumeArticle: Article?,
    val nextArticle: Article?,
    val targetArticle: Article?,
    val progressFraction: Float
)

@Composable
fun BaseScreen(
    articles: List<Article>,
    articleStatuses: Map<Int, ArticleStatus>,
    favorites: Set<Int>,
    onToggleFavorite: (Int) -> Unit,
    onToggleStatus: (Int) -> Unit,
    onArticleSelected: (Int) -> Unit,
    onNavigateToFavorites: (() -> Unit)? = null,
    scrollToArticleId: Int? = null,
    onScrollHandled: () -> Unit = {},
    resetToken: Int = 0,
    baseReturn: AppViewModel.BaseReturnSnapshot? = null,
    selectedCategory: String?,
    selectedSubcategory: String?,
    onSelectCategory: (String?) -> Unit,
    onSelectSubcategory: (String?) -> Unit,
    onRecordBaseReturn: (String?, String?, Int, Int) -> Unit,
    onConsumeBaseReturn: () -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(baseReturn) {
        baseReturn?.let { snapshot ->
            if (!snapshot.pending) {
                onSelectCategory(snapshot.category)
                onSelectSubcategory(snapshot.subcategory)
            }
        }
    }

    LaunchedEffect(selectedCategory, selectedSubcategory) {
        Log.d("BaseScreen", "selectedCategory=$selectedCategory selectedSubcategory=$selectedSubcategory")
    }

    LaunchedEffect(baseReturn?.pending, selectedCategory, selectedSubcategory) {
        val snapshot = baseReturn ?: return@LaunchedEffect
        if (!snapshot.pending &&
            selectedCategory == snapshot.category &&
            selectedSubcategory == snapshot.subcategory
        ) {
            scope.launch { listState.scrollToItem(snapshot.index, snapshot.offset) }
            onConsumeBaseReturn()
        }
    }

    val groupedByCategory = remember(articles) { articles.groupBy { it.category } }
    val categoryOrder = remember(articles) { articles.map { it.category }.distinct() }
    val articleReadTimes = remember(articles) {
        articles.associate { article ->
            val wordCount = article.description.split("\\s+".toRegex())
                .count { it.isNotBlank() }
            val minutes = max(1, ceil(wordCount / 200.0).toInt())
            article.id to minutes
        }
    }
    val categorySummaries = remember(articles, articleStatuses) {
        fun orderArticles(list: List<Article>): List<Article> =
            list.sortedWith(
                compareBy<Article> { articleStatuses[it.id] == ArticleStatus.COMPLETED }
                    .thenBy { it.dayNumber ?: Int.MAX_VALUE }
                    .thenBy { it.id }
            )
        categoryOrder.mapNotNull { categoryName ->
            val orderedArticles = orderArticles(groupedByCategory[categoryName].orEmpty())
            if (orderedArticles.isEmpty()) return@mapNotNull null
            val total = orderedArticles.size
            val completed = orderedArticles.count { articleStatuses[it.id] == ArticleStatus.COMPLETED }
            val pendingArticles = orderedArticles.filter { articleStatuses[it.id] != ArticleStatus.COMPLETED }
            val resumeArticle = orderedArticles.firstOrNull { articleStatuses[it.id] == ArticleStatus.IN_PROGRESS }
            val nextArticle = pendingArticles.firstOrNull()
            val targetArticle = resumeArticle ?: nextArticle ?: orderedArticles.firstOrNull()
            CategorySummary(
                name = categoryName,
                articles = orderedArticles,
                completedCount = completed,
                totalCount = total,
                resumeArticle = resumeArticle,
                nextArticle = nextArticle,
                targetArticle = targetArticle,
                progressFraction = if (total > 0) completed / total.toFloat() else 0f
            )
        }
    }
    val groupedBySubcategory = remember(selectedCategory, groupedByCategory, articleStatuses) {
        fun orderArticles(list: List<Article>): List<Article> =
            list.sortedWith(
                compareBy<Article> { articleStatuses[it.id] == ArticleStatus.COMPLETED }
                    .thenBy { it.dayNumber ?: Int.MAX_VALUE }
                    .thenBy { it.id }
            )
        selectedCategory?.let { category ->
            groupedByCategory[category]
                ?.groupBy { it.subcategory }
                ?.mapValues { (_, list) -> orderArticles(list) }
        } ?: emptyMap()
    }
    val subcategoryOrder = remember(selectedCategory, articles) {
        if (selectedCategory == null) emptyList() else {
            articles.filter { it.category == selectedCategory }.map { it.subcategory }.distinct()
        }
    }
    val articlesInSubcategory = remember(selectedSubcategory, groupedBySubcategory) {
        groupedBySubcategory[selectedSubcategory].orEmpty()
    }
    val targetArticle = remember(scrollToArticleId, articles) {
        scrollToArticleId?.let { id -> articles.firstOrNull { it.id == id } }
    }

    val subSummaries = remember(selectedCategory, groupedBySubcategory, articleStatuses) {
        if (selectedCategory == null) emptyList() else {
            subcategoryOrder.mapNotNull { subName ->
                val list = groupedBySubcategory[subName].orEmpty()
                if (list.isEmpty()) return@mapNotNull null
                val total = list.size
                val completed = list.count { articleStatuses[it.id] == ArticleStatus.COMPLETED }
                val pending = list.filter { articleStatuses[it.id] != ArticleStatus.COMPLETED }
                val resume = list.firstOrNull { articleStatuses[it.id] == ArticleStatus.IN_PROGRESS }
                val next = pending.firstOrNull()
                val target = resume ?: next ?: list.firstOrNull()
                CategorySummary(
                    name = subName,
                    articles = list,
                    completedCount = completed,
                    totalCount = total,
                    resumeArticle = resume,
                    nextArticle = next,
                    targetArticle = target,
                    progressFraction = if (total > 0) completed / total.toFloat() else 0f
                )
            }
        }
    }

    LaunchedEffect(resetToken) {
        if (resetToken > 0) {
            onSelectCategory(null)
            onSelectSubcategory(null)
            listState.scrollToItem(0)
            onScrollHandled()
        }
    }

    LaunchedEffect(scrollToArticleId, targetArticle) {
        if (scrollToArticleId != null && targetArticle == null) {
            onScrollHandled()
        }
    }

    LaunchedEffect(targetArticle) {
        val article = targetArticle ?: return@LaunchedEffect
        if (selectedCategory != article.category) {
            onSelectCategory(article.category)
        }
        if (selectedSubcategory != article.subcategory) {
            onSelectSubcategory(article.subcategory)
        }
    }

    LaunchedEffect(scrollToArticleId, selectedCategory, selectedSubcategory) {
        val targetId = scrollToArticleId ?: return@LaunchedEffect
        val currentList = if (selectedCategory != null && selectedSubcategory != null) {
            articlesInSubcategory
        } else {
            emptyList()
        }
        val index = currentList.indexOfFirst { it.id == targetId }
        if (index >= 0) {
            listState.scrollToItem(index)
            onScrollHandled()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.base_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                // Selected Category Row
                if (selectedCategory != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectCategory(null)
                                onSelectSubcategory(null)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = selectedCategory ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(R.string.base_change_category).split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(R.string.base_change_category).split(" ").last(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                // Selected Subcategory Row
                if (selectedCategory != null && selectedSubcategory != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSubcategory(null) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = selectedSubcategory ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(R.string.base_change_subcategory).split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(R.string.base_change_subcategory).substringAfter(" "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        when {
            selectedCategory == null -> {
                items(categorySummaries, key = { it.name }) { summary ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Title row: title is clickable to open the category (not the article)
                            Text(
                                text = summary.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Clip,
                                modifier = Modifier.clickable {
                                    onSelectCategory(summary.name)
                                    onSelectSubcategory(null)
                                    scope.launch { listState.scrollToItem(0) }
                                }
                            )

                            // Progress text + horizontal progress bar
                            Text(
                                text = stringResource(
                                    R.string.base_category_progress,
                                    summary.completedCount,
                                    summary.totalCount
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // Custom compact progress bar: no gaps, no end dot
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
                                        .fillMaxWidth(summary.progressFraction.coerceIn(0f, 1f))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }

                            // Visual separation and Next/Resume link
                            val (statusText, timeArticle) = when {
                                summary.totalCount == 0 || summary.completedCount == summary.totalCount ->
                                    stringResource(R.string.base_category_all_done) to null
                                summary.resumeArticle != null ->
                                    stringResource(R.string.base_category_resume) to summary.resumeArticle
                                summary.nextArticle != null ->
                                    stringResource(R.string.base_category_next, summary.nextArticle.title) to summary.nextArticle
                                else -> stringResource(R.string.base_category_all_done) to null
                            }
                            val readMinutes = timeArticle?.let { articleReadTimes[it.id] ?: 1 }
                            val timeSuffix = readMinutes?.let { " • " + stringResource(R.string.base_read_time_format, it) } ?: ""

                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = statusText + timeSuffix,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable {
                                    summary.targetArticle?.let { article ->
                                        onRecordBaseReturn(
                                            null,
                                            null,
                                            listState.firstVisibleItemIndex,
                                            listState.firstVisibleItemScrollOffset
                                        )
                                        onArticleSelected(article.id)
                                    }
                                }
                            )
                            // Removed AssistChip for view category, now handled by title tap
                        }
                    }
                }
            }
            selectedSubcategory == null -> {
                items(subSummaries, key = { it.name }) { summary ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Title: tap selects subcategory (does not open article)
                            Text(
                                text = summary.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Clip,
                                modifier = Modifier.clickable {
                                    onSelectSubcategory(summary.name)
                                    scope.launch { listState.scrollToItem(0) }
                                }
                            )

                            // Progress line
                            Text(
                                text = stringResource(
                                    R.string.base_category_progress,
                                    summary.completedCount,
                                    summary.totalCount
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                                        .fillMaxWidth(summary.progressFraction.coerceIn(0f, 1f))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }

                            // Visual separation and link area
                            val (statusText, timeArticle) = when {
                                summary.totalCount == 0 || summary.completedCount == summary.totalCount ->
                                    stringResource(R.string.base_category_all_done) to null
                                summary.resumeArticle != null ->
                                    stringResource(R.string.base_category_resume) to summary.resumeArticle
                                summary.nextArticle != null ->
                                    stringResource(R.string.base_category_next, summary.nextArticle.title) to summary.nextArticle
                                else -> stringResource(R.string.base_category_all_done) to null
                            }
                            val readMinutes = timeArticle?.let { articleReadTimes[it.id] ?: 1 }
                            val timeSuffix = readMinutes?.let { " • " + stringResource(R.string.base_read_time_format, it) } ?: ""

                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(4.dp))

                           Text(
                               text = statusText + timeSuffix,
                               style = MaterialTheme.typography.bodyMedium,
                               color = MaterialTheme.colorScheme.primary,
                               maxLines = 2,
                               overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable {
                                    summary.targetArticle?.let { article ->
                                        onRecordBaseReturn(
                                            selectedCategory,
                                            selectedSubcategory,
                                            listState.firstVisibleItemIndex,
                                            listState.firstVisibleItemScrollOffset
                                        )
                                        onArticleSelected(article.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            else -> {
                items(articlesInSubcategory, key = { it.id }) { article ->
                    ArticleRow(
                        article = article,
                        status = articleStatuses[article.id],
                        isFavorite = article.id in favorites,
                        onOpen = {
                            onRecordBaseReturn(
                                selectedCategory,
                                selectedSubcategory,
                                listState.firstVisibleItemIndex,
                                listState.firstVisibleItemScrollOffset
                            )
                            onArticleSelected(article.id)
                        },
                        onToggleFavorite = {
                            val wasFavorite = article.id in favorites
                            onToggleFavorite(article.id)
                            if (!wasFavorite) {
                                onNavigateToFavorites?.invoke()
                            }
                        },
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onOpen() }
                    )
                    Text(
                        text = article.shortDescription ?: article.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    if (isFavorite) {
                        Icon(imageVector = Icons.Outlined.Favorite, contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Outlined.FavoriteBorder, contentDescription = null)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onOpen) { Text(text = stringResource(R.string.action_open)) }
                Spacer(modifier = Modifier.weight(1f))

                if (status == ArticleStatus.COMPLETED) {
                    val dottedColor = MaterialTheme.colorScheme.primary
                    // Right-aligned cluster: check + clickable "uncompleted"
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null)
                        Text(
                            text = stringResource(R.string.action_uncompleted),
                            style = MaterialTheme.typography.labelLarge,
                            color = dottedColor,
                            modifier = Modifier
                                .clickable { onToggleStatus() }
                                .drawBehind {
                                    val y = size.height - 2.dp.toPx()
                                    val dash = 4.dp.toPx()
                                    drawLine(
                                        color = dottedColor,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash), 0f)
                                    )
                                }
                        )
                    }
                } else {
                    Button(onClick = onToggleStatus) {
                        Text(text = stringResource(R.string.action_mark_completed))
                    }
                }
            }
        }
    }
}
