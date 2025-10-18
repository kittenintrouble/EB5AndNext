package com.eb5.app.ui.quizzes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import com.eb5.app.ui.localization.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.os.Build
import java.lang.String.CASE_INSENSITIVE_ORDER
import java.time.Instant
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eb5.app.R

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
        onResetFilters = viewModel::onResetFilters
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
    onResetFilters: () -> Unit
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
        ) {
            val selectedIndexLocal = tabs.indexOf(state.tab)
            TabRow(
                selectedTabIndex = selectedIndexLocal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                indicator = { tabPositions ->
                    Box(Modifier.fillMaxSize()) {
                        if (selectedIndexLocal in tabPositions.indices) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[selectedIndexLocal])
                                    .align(Alignment.BottomCenter),
                                height = 4.dp
                            )
                        }
                    }
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, tab ->
                    val label = when (tab) {
                        QuizzesTab.Tracks -> stringResource(R.string.quizzes_tab_tracks)
                        QuizzesTab.All -> stringResource(R.string.quizzes_tab_all)
                        QuizzesTab.Results -> stringResource(R.string.quizzes_tab_results)
                    }
                    Tab(
                        selected = index == selectedIndexLocal,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.fillMaxHeight(),
                        text = { Text(label.uppercase(Locale.getDefault())) }
                    )
                }
            }
            val layoutDirection = LocalLayoutDirection.current
            val startPadding = padding.calculateStartPadding(layoutDirection)
            val endPadding = padding.calculateEndPadding(layoutDirection)
            val bottomPadding = padding.calculateBottomPadding()
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(
                        start = startPadding,
                        end = endPadding,
                        bottom = bottomPadding
                    )
                    .padding(horizontal = 16.dp)
            ) {
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
                    QuizzesTab.Results -> ResultsTab(state = state)
                }
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
                    quiz.questionsCount
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
        val grouped = state.allQuizzes.takeUnless { it.isEmpty() }
            ?: state.quizList.groupBy { it.category }
                .toSortedMap(CASE_INSENSITIVE_ORDER)
        if (grouped.isEmpty()) {
            EmptyState(text = stringResource(R.string.quizzes_all_empty))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                grouped.entries.forEach { (category, quizzes) ->
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
        state.goals.forEach {
            AssistChip(
                onClick = { onToggle(FilterGroup.Goal, it.id) },
                label = { Text(it.label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (it.selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (it.selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        state.durations.forEach {
            AssistChip(
                onClick = { onToggle(FilterGroup.Duration, it.id) },
                label = { Text(it.label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (it.selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (it.selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        state.levels.forEach {
            AssistChip(
                onClick = { onToggle(FilterGroup.Level, it.id) },
                label = { Text(it.label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (it.selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (it.selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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
    val isCompleted = quiz.passed || (!quiz.inProgress && (quiz.bestScore != null))
    val cardModifier = Modifier
        .fillMaxWidth()
        .let { if (isCompleted) it.alpha(0.5f) else it }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = cardModifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(quiz.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text(
                text = stringResource(
                    R.string.quizzes_quiz_meta,
                    quiz.durationMin,
                    quiz.questionsCount
                ),
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
                val pinTint = if (quiz.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                IconButton(onClick = { onSaveToggle(quiz.id, !quiz.isSaved) }) {
                    Icon(
                        imageVector = if (quiz.isSaved) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (quiz.isSaved) stringResource(R.string.quizzes_saved_toggle_remove) else stringResource(
                            R.string.quizzes_saved_toggle_add
                        ),
                        tint = pinTint
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
private fun ResultsTab(state: QuizzesUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val showOverview = state.categoryProgress.isNotEmpty() || state.overallProgress.total > 0
        if (showOverview) {
            item {
                ResultsOverview(
                    overall = state.overallProgress,
                    categories = state.categoryProgress
                )
            }
        }
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
    }
}

@Composable
private fun ResultsOverview(
    overall: OverallQuizProgress,
    categories: List<CategoryProgressUi>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val containerShape = RoundedCornerShape(16.dp)
        if (overall.total > 0) {
            val overallPercent = (overall.progressFraction * 100).roundToInt().coerceIn(0, 100)
            Surface(
                shape = containerShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.quizzes_results_overall_summary,
                            overall.completed,
                            overall.total
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        QuizProgressBar(
                            progress = overall.progressFraction,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "${overallPercent}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = stringResource(R.string.quizzes_results_overall_remaining, overall.remaining),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        // Removed extra vertical space between overall progress card and first category card
        // if (overall.total > 0 && categories.isNotEmpty()) {
        //     Spacer(modifier = Modifier.height(4.dp))
        // }
        if (categories.isNotEmpty()) {
            Surface(
                shape = containerShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categories.forEach { category ->
                        CategoryProgressRow(categoryProgress = category)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryProgressRow(categoryProgress: CategoryProgressUi) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = categoryProgress.category,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val percent = (categoryProgress.progressFraction * 100).roundToInt().coerceIn(0, 100)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(
                    R.string.quizzes_results_category_summary,
                    categoryProgress.completed,
                    categoryProgress.total,
                    categoryProgress.remaining
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f, fill = true)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "${percent}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
        QuizProgressBar(
            progress = categoryProgress.progressFraction
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun QuizProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(clamped)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
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
            val bestLabel = stringResource(
                R.string.quizzes_attempt_best,
                summary.bestScore,
                summary.totalQuestions
            )
            val completedLabel = stringResource(
                R.string.quizzes_attempt_completed,
                summary.completedDateLabel()
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(bestLabel)
                    }
                    append(" ")
                    append(completedLabel)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return ""
    }
    val attempt = lastAttemptAt ?: return ""
    val days = remember(attempt) {
        Duration.between(attempt, Instant.now()).toDays()
    }
    return when {
        days <= 0 -> stringResource(R.string.quizzes_last_attempt_today)
        days < 7 -> stringResource(R.string.quizzes_last_attempt_days, days)
        else -> {
            val formatter = remember {
                DateTimeFormatter.ofPattern("MMM d, yyyy")
                    .withZone(ZoneId.systemDefault())
            }
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
                    containerColor = if (chip.selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (chip.selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
