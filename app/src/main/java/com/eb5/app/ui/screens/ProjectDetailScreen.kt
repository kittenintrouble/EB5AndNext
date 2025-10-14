package com.eb5.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.eb5.app.R
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.ui.projects.projectStatusLabel
import com.eb5.app.ui.projects.projectTypeLabel
import com.eb5.app.data.model.Project
import com.eb5.app.data.model.ProjectImage
import com.eb5.app.ui.projects.ProjectDetailUiState
import java.text.NumberFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectDetailScreen(
    state: ProjectDetailUiState,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.project != null -> {
            ProjectDetailContent(
                project = state.project,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                onBack = onBack
            )
        }

        else -> {
            ProjectDetailError(
                message = state.error ?: stringResource(R.string.project_detail_error),
                onRetry = onRetry,
                onBack = onBack
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProjectDetailContent(
    project: Project,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val heroImageUrl = project.heroImageUrl
    val gallery = remember(project.images, heroImageUrl) {
        val remoteImages = project.images?.takeIf { it.isNotEmpty() } ?: emptyList()
        when {
            remoteImages.isNotEmpty() -> remoteImages
            !heroImageUrl.isNullOrBlank() -> listOf(ProjectImage(heroImageUrl, project.title))
            else -> emptyList()
        }
    }
    val pagerState = rememberPagerState { if (gallery.isNotEmpty()) gallery.size else 1 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (gallery.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) { page ->
                    val image = gallery[page]
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(image.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = image.alt,
                        placeholder = painterResource(id = R.drawable.bg_us_flag),
                        error = painterResource(id = R.drawable.bg_us_flag),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                if (gallery.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(gallery.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .height(6.dp)
                                    .width(if (isSelected) 18.dp else 6.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.bg_us_flag),
                    contentDescription = project.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val typeLabel = remember(project.type) {
                        projectTypeLabel(context, project.type)
                    }
                    val statusLabel = remember(project.status) {
                        projectStatusLabel(context, project.status)
                    }
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        typeLabel?.let { ProjectPill(text = it) }
                        statusLabel?.let { ProjectPill(text = it) }
                        project.lang?.takeIf { it.isNotBlank() }?.let { tag ->
                            val languageName = stringResource(AppLanguage.fromTag(tag).displayName)
                            ProjectPill(text = languageName)
                        }
                    }
                }
                IconButton(onClick = onToggleFavorite) {
                    val icon = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder
                    val cd = if (isFavorite) R.string.project_remove_favorite else R.string.project_add_favorite
                    Icon(imageVector = icon, contentDescription = stringResource(cd))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                project.location?.takeIf { it.isNotBlank() }
                    ?.let { Text(stringResource(R.string.project_location, it), style = MaterialTheme.typography.bodyMedium) }
                val typeLabel = remember(project.type) {
                    projectTypeLabel(context, project.type)
                }
                val statusLabel = remember(project.status) {
                    projectStatusLabel(context, project.status)
                }
                typeLabel?.let {
                    Text(stringResource(R.string.project_category, it), style = MaterialTheme.typography.bodyMedium)
                }
                statusLabel?.let {
                    Text(stringResource(R.string.project_status, it), style = MaterialTheme.typography.bodyMedium)
                }
                project.developer?.takeIf { it.isNotBlank() }
                    ?.let { Text(stringResource(R.string.project_developer, it), style = MaterialTheme.typography.bodyMedium) }
                project.expectedOpening?.takeIf { it.isNotBlank() }
                    ?.let { Text(stringResource(R.string.project_expected_opening, it), style = MaterialTheme.typography.bodyMedium) }
                project.expectedOpening?.takeIf { it.isNotBlank() }
                    ?.let { Text(stringResource(R.string.project_expected_opening, it), style = MaterialTheme.typography.bodyMedium) }
                project.tea?.designation?.takeIf { it.isNotBlank() }
                    ?.let { Text(stringResource(R.string.project_tea_status, it), style = MaterialTheme.typography.bodyMedium) }
            }

            project.shortDescription
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            project.fullDescription
                ?.takeIf { it.isNotBlank() }
                ?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val minInvestment = project.financials?.minInvestment ?: project.minInvestmentUsd
            val financialItems = buildList {
                project.financials?.totalProject?.let { add(stringResource(R.string.project_financial_total, currencyFormatter.format(it))) }
                project.financials?.eb5Offering?.let { add(stringResource(R.string.project_financial_offering, currencyFormatter.format(it))) }
                minInvestment?.let { add(stringResource(R.string.project_financial_minimum, currencyFormatter.format(it))) }
                project.financials?.eb5Investors?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_financial_investors, it))
                }
                project.financials?.term?.let {
                    add(stringResource(R.string.project_financial_term, it.toString()))
                }
                project.financials?.interestRate?.let {
                    val rate = String.format(Locale.US, "%.1f%%", it)
                    add(stringResource(R.string.project_financial_interest, rate))
                }
            }
            ProjectDetailSection(
                title = stringResource(R.string.project_financials_title),
                items = financialItems
            )

            val loanItems = buildList {
                project.loanStructure?.type?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_loan_type, it))
                }
                project.loanStructure?.annualReturn?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_loan_return, it))
                }
                project.loanStructure?.termYears?.let {
                    add(stringResource(R.string.project_loan_term, it.toString()))
                }
                project.loanStructure?.escrow?.let {
                    val value = if (it) stringResource(R.string.common_yes) else stringResource(R.string.common_no)
                    add(stringResource(R.string.project_loan_escrow, value))
                }
            }
            ProjectDetailSection(
                title = stringResource(R.string.project_loan_title),
                items = loanItems
            )

            val teaItems = buildList {
                project.tea?.type?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_tea_type, it))
                }
                project.tea?.designation?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_tea_designation, it))
                }
            }
            ProjectDetailSection(
                title = stringResource(R.string.project_tea_title),
                items = teaItems
            )

            val uscisItems = buildList {
                project.uscis?.i956fStatus?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_uscis_i956f, it))
                }
                project.uscis?.i526eStatus?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_uscis_i526e, it))
                }
                formatDate(project.uscis?.approvalDate)?.let {
                    add(stringResource(R.string.project_uscis_approval_date, it))
                }
            }
            ProjectDetailSection(
                title = stringResource(R.string.project_uscis_title),
                items = uscisItems
            )

            val jobsItems = buildList {
                project.jobs?.total?.let {
                    add(stringResource(R.string.project_jobs_total, it.toString()))
                }
                project.jobs?.perInvestor?.let {
                    add(stringResource(R.string.project_jobs_per_investor, String.format(Locale.US, "%.1f", it)))
                }
                (project.jobs?.model ?: project.jobCreationModel)?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_jobs_model_label, it))
                }
            }
            ProjectDetailSection(
                title = stringResource(R.string.project_jobs_title),
                items = jobsItems
            )

            val metadataItems = buildList {
                add(stringResource(R.string.project_id, project.id))
                project.slug?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_slug, it))
                }
                project.lang?.takeIf { it.isNotBlank() }?.let { tag ->
                    val languageName = stringResource(AppLanguage.fromTag(tag).displayName)
                    add(stringResource(R.string.project_language, languageName))
                }
                add(
                    stringResource(
                        R.string.project_published,
                        if (project.published) stringResource(R.string.common_yes) else stringResource(R.string.common_no)
                    )
                )
            }
            ProjectDetailSection(
                title = stringResource(R.string.project_metadata_title),
                items = metadataItems
            )

            listOfNotNull(
                formatDate(project.updatedAt)?.let { stringResource(R.string.project_updated_at, it) },
                formatDate(project.createdAt)?.let { stringResource(R.string.project_created_at, it) },
                formatDate(project.publishedAt)?.let { stringResource(R.string.project_published_at, it) }
            ).takeIf { it.isNotEmpty() }?.let { timestamps ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    timestamps.forEach { Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.action_back))
            }
        }
    }
}

@Composable
private fun ProjectDetailSection(title: String, items: List<String>) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        items.forEach { item ->
            Text(text = item, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProjectDetailError(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.project_detail_retry))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.action_back))
        }
    }
}

@Composable
private fun ProjectPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun formatDate(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    return runCatching {
        OffsetDateTime.parse(raw).atZoneSameInstant(ZoneId.systemDefault()).format(formatter)
    }.getOrElse {
        runCatching { Instant.parse(raw).atZone(ZoneId.systemDefault()).format(formatter) }.getOrNull()
    }
}
