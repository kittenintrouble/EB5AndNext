package com.eb5.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.eb5.app.ui.localization.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.eb5.app.R
import com.eb5.app.data.model.Project
import com.eb5.app.data.model.ProjectImage
import com.eb5.app.ui.projects.projectStatusLabel
import com.eb5.app.ui.projects.projectTypeLabel
import java.text.NumberFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.util.Locale

@Composable
fun ProjectsScreen(
    projects: List<Project>,
    projectFavorites: Set<String>,
    onToggleProjectFavorite: (String) -> Unit,
    onProjectSelected: (Project) -> Unit
) {
    val context = LocalContext.current
    val sortedProjects = remember(projects) {
        projects.sortedWith(
            compareByDescending<Project> { parseIsoDate(it.publishedAt) ?: Long.MIN_VALUE }
                .thenBy { it.title }
        )
    }

    if (sortedProjects.isEmpty()) {
        ProjectsEmptyState()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(sortedProjects, key = { it.id }) { project ->
            ProjectListItem(
                project = project,
                isFavorite = projectFavorites.contains(project.id),
                onToggleFavorite = { onToggleProjectFavorite(project.id) },
                onProjectSelected = { onProjectSelected(project) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProjectListItem(
    project: Project,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onProjectSelected: () -> Unit
) {
    val context = LocalContext.current
    val minInvestmentLabel = remember(project.financials?.minInvestment, project.minInvestmentUsd) {
        val amount = project.financials?.minInvestment ?: project.minInvestmentUsd
        amount?.let { currencyFormatter().format(it) } ?: "—"
    }
    val expectedOpening = project.expectedOpening?.takeIf { it.isNotBlank() }
    val heroImageUrl = project.heroImageUrl
    val gallery = remember(project.images, heroImageUrl) {
        val remoteImages = project.images?.takeIf { it.isNotEmpty() } ?: emptyList()
        when {
            remoteImages.isNotEmpty() -> remoteImages
            !heroImageUrl.isNullOrBlank() -> listOf(ProjectImage(url = heroImageUrl, alt = project.title))
            else -> emptyList()
        }
    }

    Card(
        onClick = onProjectSelected,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (gallery.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { gallery.size })
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) { page ->
                    val image = gallery[page]
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(image.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = image.alt,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        },
                        error = {
                            Image(
                                painter = painterResource(id = R.drawable.bg_us_flag),
                                contentDescription = image.alt,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.bg_us_flag),
                    contentDescription = project.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    project.shortDescription
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val typeLabel = remember(project.type) {
                            projectTypeLabel(context, project.type)
                        }
                        val statusLabel = remember(project.status) {
                            projectStatusLabel(context, project.status)
                        }
                        typeLabel?.let {
                            ProjectMetaPill(
                                label = stringResource(R.string.project_list_meta_type),
                                value = it
                            )
                        }
                        statusLabel?.let {
                            ProjectMetaPill(
                                label = stringResource(R.string.project_list_meta_status),
                                value = it
                            )
                        }
                    }
                }
                IconButton(onClick = onToggleFavorite) {
                    val icon = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder
                    val label = if (isFavorite) R.string.project_remove_favorite else R.string.project_add_favorite
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(id = label)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                project.location?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(
                        icon = Icons.Outlined.Place,
                        text = stringResource(R.string.project_location, it)
                    )
                }
                expectedOpening?.let {
                    InfoRow(
                        icon = Icons.Outlined.Event,
                        text = stringResource(R.string.project_expected_opening, it)
                    )
                }
            }


            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.project_financial_minimum, minInvestmentLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                project.financials?.eb5Offering?.let {
                    Text(
                        text = stringResource(
                            R.string.project_financial_offering,
                            currencyFormatter().format(it)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                project.financials?.eb5Investors?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = stringResource(R.string.project_financial_investors, it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectsEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {}
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.project_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.project_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProjectMetaPill(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun currencyFormatter(): NumberFormat =
    NumberFormat.getCurrencyInstance(Locale.US)

private fun parseInvestorProgress(value: String): Float? {
    val match = Regex("(\\d+(?:[\\.,]\\d+)?)").find(value) ?: return null
    val numeric = match.value.replace(',', '.').toFloatOrNull() ?: return null
    val normalized = when {
        value.contains("%") -> numeric / 100f
        numeric <= 1f -> numeric
        numeric in 1f..100f -> numeric / 100f
        else -> return null
    }
    return normalized.coerceIn(0f, 1f)
}

private fun parseIsoDate(value: String?): Long? = runCatching {
    when {
        value.isNullOrBlank() -> null
        value.endsWith("Z", ignoreCase = true) -> Instant.parse(value).toEpochMilli()
        else -> OffsetDateTime.parse(value).toInstant().toEpochMilli()
    }
}.getOrNull()
