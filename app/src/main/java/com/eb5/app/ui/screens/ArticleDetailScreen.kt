package com.eb5.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eb5.app.ui.localization.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.eb5.app.R
import com.eb5.app.data.model.Article
import com.eb5.app.data.model.ArticleStatus
import com.eb5.app.ui.components.DetailTopBar

@Composable
fun ArticleDetailScreen(
    article: Article,
    status: ArticleStatus,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleStatus: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(modifier = Modifier.fillMaxSize()) {
        DetailTopBar(
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            onBack = onBack,
            favoriteOnContentDescription = R.string.article_remove_favorite,
            favoriteOffContentDescription = R.string.article_add_favorite
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (status == ArticleStatus.COMPLETED) {
                        val dottedColor = MaterialTheme.colorScheme.primary
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
                    } else {
                        Button(onClick = onToggleStatus) {
                            Text(text = stringResource(R.string.action_mark_completed))
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                Text(
                    text = article.description,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
                )
            }
            if (article.examples.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.article_examples_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            article.examples.forEach { example ->
                                Text(text = example, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
