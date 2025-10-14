package com.eb5.app.ui.quizzes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.eb5.app.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun QuizzesRoute(
    viewModel: QuizzesViewModel,
    onOpenQuiz: (String) -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenTrackDetails: (String) -> Unit = onOpenTrack
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    QuizzesScreen(
        state = state,
        onTabSelected = viewModel::onTabSelected,
        onResumeQuiz = {
            viewModel.onResumeQuiz(it)
            onOpenQuiz(it)
        },
        onOpenQuiz = {
            viewModel.onPrimaryCtaClick(it.id, it.ctaLabel())
            onOpenQuiz(it.id)
        },
        onSaveToggle = viewModel::onSavedToggle,
        onOpenTrack = {
            viewModel.onOpenTrack(it.id)
            onOpenTrack(it.id)
        },
        onShowFilters = viewModel::onToggleFilterSheet,
        onToggleFilter = viewModel::onToggleFilterChip,
        onResetFilters = viewModel::onResetFilters,
        onCertificateDownload = viewModel::onCertificateDownload,
        onCertificateShare = viewModel::onCertificateShare
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizzesScreen(
    state: QuizzesUiState,
    onTabSelected: (QuizzesTab) -> Unit,
    onResumeQuiz: (String) -> Unit,
    onOpenQuiz: (QuizUi) -> Unit,
    onSaveToggle: (String, Boolean) -> Unit,
    onOpenTrack: (TrackUi) -> Unit,
    onShowFilters: (Boolean) -> Unit,
    onToggleFilter: (FilterGroup, String) -> Unit,
    onResetFilters: () -> Unit,
    onCertificateDownload: (String) -> Unit,
    onCertificateShare: (String) -> Unit
) {
    val tabs = listOf(QuizzesTab.Tracks, QuizzesTab.All, QuizzesTab.Results)
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (state.isFilterSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { onShowFilters(false) },
            sheetState = sheetState
        ) {
            FilterSheetContent(
                filters = state.filterChips,
                onToggle = onToggleFilter,
                onReset = {
                    onResetFilters()
                },
                onApply = {
                    scope.launch {
                        sheetState.hide()
                        onShowFilters(false)
                    }
                }
            )
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.quizzes_subheader),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            val selectedIndex = tabs.indexOf(state.tab)
            TabRow(selectedTabIndex = selectedIndex) {
                tabs.forEachIndexed { index, tab ->
                    val label = when (tab) {
                        QuizzesTab.Tracks -> stringResource(R.string.quizzes_tab_tracks)
                        QuizzesTab.All -> stringResource(R.string.quizzes_tab_all)
                        QuizzesTab.Results -> stringResource(R.string.quizzes_tab_results)
                    }
                    Tab(
                        selected = index == selectedIndex,
                        onClick = { onTabSelected(tab) },
                        text = { Text(label) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            when (state.tab) {
                QuizzesTab.Tracks -> TracksTab(
                    state = state,
                    onResumeQuiz = onResumeQuiz,
                    onOpenTrack = onOpenTrack
                )
                QuizzesTab.All -> AllQuizzesTab(
                    state = state,
                    onShowFilters = onShowFilters,
                    onToggleFilter = onToggleFilter,
                    onOpenQuiz = onOpenQuiz,
                    onSaveToggle = onSaveToggle
                )
                QuizzesTab.Results -> ResultsTab(
                    state = state,
                    onDownloadCertificate = onCertificateDownload,
                    onShareCertificate = onCertificateShare
                )
            }
        }
    }
}

@Composable
private fun TracksTab(
    state: QuizzesUiState,
    onResumeQuiz: (String) -> Unit,
    onOpenTrack: (TrackUi) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (state.hasResume) {
            item {
                Text(
                    text = stringResource(R.string.quizzes_resume_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(state.resumeQuizzes, key = { it.id }) { quiz ->
                        ResumeCard(quiz, onResumeQuiz)
                    }
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.quizzes_tracks_recommended),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (state.tracks.isEmpty()) {
            item {
                EmptyState(text = stringResource(R.string.quizzes_tracks_empty))
            }
        } else {
            items(state.tracks, key = { it.id }) { track ->
                TrackCard(track = track, onOpenTrack = onOpenTrack)
            }
        }
    }
}

@Composable
private fun ResumeCard(
    quiz: QuizUi,
    onResumeQuiz: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .width(240.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = quiz.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    R.string.quizzes_quiz_meta,
                    quiz.durationMin,
                    quiz.questionsCount,
                    quiz.format
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onResumeQuiz(quiz.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.quizzes_cta_resume))
            }
        }
    }
}

@Composable
private fun TrackCard(
    track: TrackUi,
    onOpenTrack: (TrackUi) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onOpenTrack(track) },
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(track.title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = track.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicatorTrack(
                progress = track.progressFraction,
                label = stringResource(
                    R.string.quizzes_track_duration,
                    track.estimatedDurationMin,
                    track.total
                ),
                completed = track.completed,
                total = track.total
            )
            val cta = when {
                track.completed == 0 -> stringResource(R.string.quizzes_cta_start_track)
                track.completed < track.total -> stringResource(R.string.quizzes_cta_continue_track)
                else -> stringResource(R.string.quizzes_cta_view_track)
            }
            Button(onClick = { onOpenTrack(track) }, modifier = Modifier.fillMaxWidth()) {
                Text(cta)
            }
        }
    }
}

@Composable
private fun LinearProgressIndicatorTrack(
    progress: Float,
    label: String,
    completed: Int,
    total: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Text(
            text = "$completed/$total · $label",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AllQuizzesTab(
    state: QuizzesUiState,
    onShowFilters: (Boolean) -> Unit,
    onToggleFilter: (FilterGroup, String) -> Unit,
    onOpenQuiz: (QuizUi) -> Unit,
    onSaveToggle: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        FilterRow(
            state = state.filterChips,
            onToggle = onToggleFilter,
            onOpenSheet = { onShowFilters(true) }
        )
        Spacer(Modifier.height(12.dp))
        if (state.allQuizzes.isEmpty()) {
            EmptyState(text = stringResource(R.string.quizzes_all_empty))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                state.allQuizzes.entries.forEach { (category, quizzes) ->
                    item(key = "header_$category") {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(vertical = 8.dp)
                        )
                    }
                    items(quizzes, key = { it.id }) { quiz ->
                        QuizCard(
                            quiz = quiz,
                            onPrimaryClick = onOpenQuiz,
                            onSaveToggle = onSaveToggle
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    state: QuizFilterUi,
    onToggle: (FilterGroup, String) -> Unit,
    onOpenSheet: () -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(onClick = onOpenSheet) {
            Text(text = stringResource(R.string.quizzes_filters_button))
        }
        state.sorts.forEach { chip ->
            AssistChip(
                onClick = { onToggle(FilterGroup.Sort, chip.id) },
                label = { Text(chip.label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (chip.selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (chip.selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        state.goals.forEach {
            AssistChip(
                onClick = { onToggle(FilterGroup.Goal, it.id) },
                label = { Text(it.label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (it.selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (it.selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        state.durations.forEach {
            AssistChip(
                onClick = { onToggle(FilterGroup.Duration, it.id) },
                label = { Text(it.label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (it.selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (it.selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        state.formats.forEach {
            AssistChip(
                onClick = { onToggle(FilterGroup.Format, it.id) },
                label = { Text(it.label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (it.selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (it.selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        state.levels.forEach {
            AssistChip(
                onClick = { onToggle(FilterGroup.Level, it.id) },
                label = { Text(it.label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (it.selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (it.selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun QuizCard(
    quiz: QuizUi,
    onPrimaryClick: (QuizUi) -> Unit,
    onSaveToggle: (String, Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(quiz.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text(
                text = quiz.estimatedDurationLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LevelChip(level = quiz.level)
                Text(
                    text = if (quiz.passed) stringResource(R.string.quizzes_passed_label)
                    else stringResource(R.string.quizzes_best_score_label, quiz.bestScore ?: 0, quiz.questionsCount),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onSaveToggle(quiz.id, !quiz.isSaved) }) {
                    Icon(
                        imageVector = if (quiz.isSaved) Icons.Outlined.BookmarkAdded else Icons.Outlined.BookmarkAdd,
                        contentDescription = if (quiz.isSaved) stringResource(R.string.quizzes_saved_toggle_remove) else stringResource(
                            R.string.quizzes_saved_toggle_add
                        )
                    )
                }
            }
            Button(onClick = { onPrimaryClick(quiz) }, modifier = Modifier.fillMaxWidth()) {
                val label = when {
                    quiz.inProgress -> stringResource(R.string.quizzes_cta_resume)
                    quiz.bestScore != null -> stringResource(R.string.quizzes_cta_retake)
                    else -> stringResource(R.string.quizzes_cta_start)
                }
                Text(label)
            }
            val lastAttempt = quiz.lastAttemptRelative()
            if (lastAttempt.isNotBlank()) {
                Text(
                    text = lastAttempt,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LevelChip(level: String) {
    val label = when (level.uppercase()) {
        "L" -> stringResource(R.string.quizzes_level_beginner)
        "M" -> stringResource(R.string.quizzes_level_intermediate)
        "H" -> stringResource(R.string.quizzes_level_advanced)
        else -> level
    }
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ResultsTab(
    state: QuizzesUiState,
    onDownloadCertificate: (String) -> Unit,
    onShareCertificate: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.results.isEmpty()) {
            item { EmptyState(text = stringResource(R.string.quizzes_results_empty)) }
        } else {
            item {
                Text(
                    text = stringResource(R.string.quizzes_results_history),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(state.results, key = { it.id }) { attempt ->
                ResultCard(attempt)
            }
        }
        item {
            Text(
                text = stringResource(R.string.quizzes_results_certificates),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (state.certificates.isEmpty()) {
            item { EmptyState(text = stringResource(R.string.quizzes_certificates_empty)) }
        } else {
            items(state.certificates, key = { it.trackId }) { certificate ->
                CertificateCard(
                    certificate = certificate,
                    onDownload = { onDownloadCertificate(certificate.trackId) },
                    onShare = { onShareCertificate(certificate.trackId) }
                )
            }
        }
    }
}

@Composable
private fun ResultCard(summary: AttemptSummary) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(summary.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text(
                text = stringResource(R.string.quizzes_attempt_best, summary.bestScore, summary.totalQuestions),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.quizzes_attempt_completed, summary.completedDateLabel()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CertificateCard(
    certificate: CertificateUi,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(certificate.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text(
                text = certificate.completedLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDownload) {
                    Text(text = stringResource(R.string.quizzes_certificate_download))
                }
                OutlinedButton(onClick = onShare) {
                    Text(text = stringResource(R.string.quizzes_certificate_share))
                }
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
    }
}

@Composable
private fun QuizUi.lastAttemptRelative(): String {
    val now = Instant.now()
    val attempt = lastAttemptAt ?: return ""
    val days = java.time.Duration.between(attempt, now).toDays()
    return when {
        days <= 0 -> stringResource(R.string.quizzes_last_attempt_today)
        days < 7 -> stringResource(R.string.quizzes_last_attempt_days, days)
        else -> {
            val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
                .withZone(ZoneId.systemDefault())
            stringResource(R.string.quizzes_last_attempt_date, formatter.format(attempt))
        }
    }
}

@Composable
private fun FilterSheetContent(
    filters: QuizFilterUi,
    onToggle: (FilterGroup, String) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = stringResource(R.string.quizzes_filter_goal), style = MaterialTheme.typography.titleMedium)
        FlowChipGroup(chips = filters.goals) { onToggle(FilterGroup.Goal, it) }

        Text(text = stringResource(R.string.quizzes_filter_duration), style = MaterialTheme.typography.titleMedium)
        FlowChipGroup(chips = filters.durations) { onToggle(FilterGroup.Duration, it) }

        Text(text = stringResource(R.string.quizzes_filter_format), style = MaterialTheme.typography.titleMedium)
        FlowChipGroup(chips = filters.formats) { onToggle(FilterGroup.Format, it) }

        Text(text = stringResource(R.string.quizzes_filter_level), style = MaterialTheme.typography.titleMedium)
        FlowChipGroup(chips = filters.levels) { onToggle(FilterGroup.Level, it) }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onReset) {
                Text(text = stringResource(R.string.quizzes_filter_reset))
            }
            Button(onClick = onApply) {
                Text(text = stringResource(R.string.quizzes_filter_apply))
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FlowChipGroup(
    chips: List<FilterChipState>,
    onToggle: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        chips.forEach { chip ->
            AssistChip(
                onClick = { onToggle(chip.id) },
                label = { Text(chip.label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (chip.selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (chip.selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
