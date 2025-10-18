package com.eb5.app.ui.quizzes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eb5.app.data.model.QuizCatalog
import com.eb5.app.data.model.QuizProgress
import com.eb5.app.data.model.QuizTopic
import com.eb5.app.data.model.QuizTrack
import com.eb5.app.data.model.QuizAttemptRecord
import com.eb5.app.data.model.QuizInProgressState
import com.eb5.app.ui.AppUiState
import com.eb5.app.ui.quizzes.QuizzesTab.All
import com.eb5.app.ui.quizzes.QuizzesTab.Tracks
import java.time.Instant
import java.lang.String.CASE_INSENSITIVE_ORDER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuizzesViewModel(
    private val appState: StateFlow<AppUiState>,
    private val setQuizSaved: (String, Boolean) -> Unit
) : ViewModel() {

    private val _state = MutableStateFlow(
        QuizzesUiState(
            tab = Tracks,
            filters = QuizFilters(),
            filterChips = buildFilterChips()
        )
    )
    val state: StateFlow<QuizzesUiState> = _state.asStateFlow()

    private val seedCatalog = QuizSeedData.defaultCatalog()
    private var latestCatalog: QuizCatalog = seedCatalog
    private var latestQuizzes: List<QuizUi> = emptyList()
    private var quizProgress: Map<String, QuizProgress> = emptyMap()
    private var savedQuizzes: Set<String> = emptySet()
    private var questionCountByQuizId: Map<String, Int> = seedCatalog.quizzes.associate { it.id to it.questions.size }
    private var quizAttempts: List<QuizAttemptRecord> = emptyList()
    private var quizInProgressStates: Map<String, QuizInProgressState> = emptyMap()
    private var usingSeed = true

    init {
        rebuildContent()
        applySnapshot(appState.value)
        viewModelScope.launch {
            appState.collect { app -> applySnapshot(app) }
        }
    }

    private fun applySnapshot(app: AppUiState) {
        val incoming = app.quizCatalog
        if (incoming.quizzes.isNotEmpty()) {
            latestCatalog = incoming
            usingSeed = false
        } else if (usingSeed) {
            latestCatalog = seedCatalog
        }
        quizProgress = app.quizProgress
        savedQuizzes = app.savedQuizzes
        questionCountByQuizId = latestCatalog.quizzes.associate { it.id to it.questions.size }
        quizAttempts = app.quizAttempts
        quizInProgressStates = app.quizInProgress
        runCatching { rebuildContent() }
            .onFailure { throwable ->
                logEvent("quizzes_rebuild_error", mapOf("error" to throwable.message))
                throwable.printStackTrace()
            }
    }

    fun onTabSelected(tab: QuizzesTab) {
        if (tab == _state.value.tab) return
        _state.update { it.copy(tab = tab) }
        logEvent("quizzes_tab_view", mapOf("tab" to tab.name))
    }

    fun onToggleFilterChip(group: FilterGroup, chipId: String) {
        when (group) {
            FilterGroup.Goal -> toggleSetChip(group, chipId)
            FilterGroup.Level -> toggleSetChip(group, chipId)
            FilterGroup.Duration -> toggleChipExclusive(chipId) { id ->
                when (id) {
                    "duration_short" -> 0..5
                    "duration_medium" -> 6..10
                    "duration_long" -> 11..Int.MAX_VALUE
                    else -> null
                }
            }
        }
        logEvent("quizzes_filter_apply", mapOf("filters" to _state.value.filters))
    }

    fun onResetFilters() {
        val chips = buildFilterChips()
        _state.update {
            val filters = QuizFilters()
            it.copy(
                filters = filters,
                filterChips = chips,
                allQuizzes = groupedWithFallback(filters),
                quizList = latestQuizzes
            )
        }
    }

    fun onSavedToggle(quizId: String, isSaved: Boolean) {
        setQuizSaved(quizId, isSaved)
        latestQuizzes = latestQuizzes.map {
            if (it.id == quizId) it.copy(isSaved = isSaved) else it
        }
        _state.update { current ->
            current.copy(
                resumeQuizzes = latestQuizzes.filter { quiz -> quiz.inProgress },
                allQuizzes = groupedWithFallback(current.filters),
                quizList = latestQuizzes
            )
        }
    }

    fun onPrimaryCtaClick(quizId: String, cta: String) {
        logEvent("quiz_card_cta_click", mapOf("quizId" to quizId, "cta" to cta))
    }

    fun onResumeQuiz(quizId: String) {
        logEvent("quizzes_resume_click", mapOf("quizId" to quizId))
    }

    fun onOpenTrack(trackId: String) {
        logEvent("quizzes_track_open", mapOf("trackId" to trackId))
    }

    fun onToggleFilterSheet(show: Boolean) {
        _state.update { it.copy(isFilterSheetVisible = show) }
    }

    private fun rebuildContent() {
        if (latestCatalog.quizzes.isEmpty()) {
            latestCatalog = seedCatalog
            questionCountByQuizId = latestCatalog.quizzes.associate { it.id to it.questions.size }
        }
        latestQuizzes = latestCatalog.quizzes.map { topic ->
            topic.toQuizUi()
        }
        val currentFilters = _state.value.filters
        val resume = latestQuizzes
            .filter { it.inProgress }
            .sortedByDescending { it.lastAttemptAt ?: Instant.MIN }
        val tracks = buildTracks()
        val results = buildResults()
        val (overallProgress, categoryProgress) = buildProgressOverview()
        _state.update {
            it.copy(
                resumeQuizzes = resume,
                tracks = tracks,
                allQuizzes = groupedWithFallback(currentFilters),
                quizList = latestQuizzes,
                results = results,
                overallProgress = overallProgress,
                categoryProgress = categoryProgress
            )
        }
    }

    private fun buildTracks(): List<TrackUi> {
        if (latestCatalog.tracks.isEmpty()) return emptyList()
        val quizById = latestCatalog.quizzes.associateBy { it.id }
        return latestCatalog.tracks.mapNotNull { track ->
            val topics = track.quizIds.mapNotNull { id -> quizById[id] }
            if (topics.isEmpty()) return@mapNotNull null
            val completed = track.quizIds.count { id ->
                val required = questionCountByQuizId[id] ?: return@count false
                (quizProgress[id]?.bestScore ?: 0) >= required
            }
            val estimatedDuration = track.estimatedDurationMinutes.takeIf { it > 0 }
                ?: topics.sumOf { it.durationMinutes }
            TrackUi(
                id = track.id,
                title = track.title,
                description = track.description,
                quizIds = track.quizIds,
                estimatedDurationMin = estimatedDuration,
                completed = completed,
                total = track.quizIds.size,
                certificateAvailable = completed >= track.quizIds.size
            )
        }
    }

    private fun buildResults(): List<AttemptSummary> {
        if (quizAttempts.isEmpty()) return emptyList()
        val quizById = latestCatalog.quizzes.associateBy { it.id }
        return quizAttempts.mapNotNull { record ->
            val quiz = quizById[record.quizId] ?: return@mapNotNull null
            AttemptSummary(
                id = record.id,
                title = quiz.title,
                quizId = record.quizId,
                trackId = record.trackId,
                bestScore = record.score,
                totalQuestions = record.totalQuestions,
                level = record.level,
                durationMin = record.durationMinutes,
                completedAt = Instant.ofEpochMilli(record.completedAt)
            )
        }.sortedByDescending { it.completedAt }
    }

    private fun buildProgressOverview(): Pair<OverallQuizProgress, List<CategoryProgressUi>> {
        if (latestQuizzes.isEmpty()) return OverallQuizProgress() to emptyList()
        val grouped = latestQuizzes.groupBy { it.category }
        val categoryProgress = grouped.keys
            .sortedWith(CASE_INSENSITIVE_ORDER)
            .map { category ->
                val quizzes = grouped[category].orEmpty()
                val completed = quizzes.count { quiz ->
                    val required = questionCountByQuizId[quiz.id] ?: quiz.questionsCount
                    val bestScore = quizProgress[quiz.id]?.bestScore ?: 0
                    required > 0 && bestScore >= required
                }
                CategoryProgressUi(
                    category = category,
                    completed = completed,
                    total = quizzes.size
                )
            }
        val overallCompleted = categoryProgress.sumOf { it.completed }
        val overall = OverallQuizProgress(
            completed = overallCompleted,
            total = latestQuizzes.size
        )
        return overall to categoryProgress
    }

    private fun applyFilters(
        quizzes: List<QuizUi>,
        filters: QuizFilters
    ): Map<String, List<QuizUi>> {
        var result = quizzes
        val goalTags = filters.goals.mapNotNull(::goalTagForChip).toSet()
        if (goalTags.isNotEmpty()) {
            result = result.filter { quiz -> quiz.tags.any { it in goalTags } }
        }
        filters.duration?.let { range ->
            result = result.filter { quiz -> quiz.durationMin in range }
        }
        val levels = filters.levels.mapNotNull(::levelForChip).toSet()
        if (levels.isNotEmpty()) {
            result = result.filter { quiz -> quiz.level.uppercase() in levels }
        }
        result = result.sortedWith(recommendedComparator())
        return groupByCategory(result)
    }

    private fun toggleSetChip(group: FilterGroup, chipId: String) {
        _state.update { state ->
            val filters = state.filters
            val (updatedFilters, updatedChips) = when (group) {
                FilterGroup.Goal -> {
                    val newSet = toggleExclusive(filters.goals, chipId)
                    filters.copy(goals = newSet) to state.filterChips.copy(
                        goals = state.filterChips.goals.map { chip ->
                            chip.copy(selected = chip.id == chipId && newSet.isNotEmpty())
                        }
                    )
                }
                FilterGroup.Level -> {
                    val newSet = toggleExclusive(filters.levels, chipId)
                    filters.copy(levels = newSet) to state.filterChips.copy(
                        levels = state.filterChips.levels.map { chip ->
                            chip.copy(selected = chip.id == chipId && newSet.isNotEmpty())
                        }
                    )
                }
                else -> filters to state.filterChips
            }
            state.copy(
                filters = updatedFilters,
                filterChips = updatedChips,
                allQuizzes = groupedWithFallback(updatedFilters),
                quizList = latestQuizzes
            )
        }
    }

    private fun toggleChipExclusive(
        chipId: String,
        rangeProvider: (String) -> IntRange?
    ) {
        val range = rangeProvider(chipId)
        _state.update { state ->
            val newRange = if (state.filters.duration == range) null else range
            val updatedChips = state.filterChips.durations.map {
                if (it.id == chipId) it.copy(selected = newRange != null) else it.copy(selected = false)
            }
            val updatedFilters = state.filters.copy(duration = newRange)
            state.copy(
                filters = updatedFilters,
                filterChips = state.filterChips.copy(durations = updatedChips),
                allQuizzes = groupedWithFallback(updatedFilters),
                quizList = latestQuizzes
            )
        }
    }

    private fun <T> toggleExclusive(current: Set<T>, item: T): Set<T> =
        if (current.contains(item)) emptySet() else setOf(item)

    private fun QuizTopic.toQuizUi(): QuizUi {
        val progress = quizProgress[id]
        val progressState = quizInProgressStates[id]
        val maxScore = questions.size
        val bestScore = progress?.bestScore
        val lastAttemptInstant = progress?.lastAttemptTimestamp
            ?.takeIf { it > 0L }
            ?.let { Instant.ofEpochMilli(it) }
            ?: progressState?.let { Instant.ofEpochMilli(it.updatedAt) }
        val lastScore = progress?.lastScore ?: 0
        val inProgress = progressState != null
        return QuizUi(
            id = id,
            title = title,
            category = category,
            format = format,
            level = level,
            durationMin = durationMinutes,
            questionsCount = maxScore,
            tags = (goalTags + tags).distinct(),
            bestScore = bestScore,
            passed = bestScore != null && bestScore >= maxScore,
            lastAttemptAt = lastAttemptInstant,
            inProgress = inProgress,
            isSaved = savedQuizzes.contains(id)
        )
    }

    private fun buildFilterChips(): QuizFilterUi = QuizFilterUi(
        goals = listOf(
            FilterChipState("goal_deal_ready", "Deal-ready"),
            FilterChipState("goal_source_funds", "Source of Funds"),
            FilterChipState("goal_compliance", "Compliance"),
            FilterChipState("goal_risk", "Risk Management"),
            FilterChipState("goal_legal", "Immigration Process")
        ),
        durations = listOf(
            FilterChipState("duration_short", "≤5 min"),
            FilterChipState("duration_medium", "6–10 min"),
            FilterChipState("duration_long", "10+ min")
        ),
        levels = listOf(
            FilterChipState("level_l", "Beginner"),
            FilterChipState("level_m", "Intermediate"),
            FilterChipState("level_h", "Advanced")
        )
    )

    private fun goalTagForChip(chipId: String): String? = when (chipId) {
        "goal_deal_ready" -> "deal_ready"
        "goal_source_funds" -> "source_funds"
        "goal_compliance" -> "compliance"
        "goal_risk" -> "risk"
        "goal_legal" -> "immigration"
        else -> null
    }

    private fun levelForChip(chipId: String): String? = when (chipId) {
        "level_l" -> "L"
        "level_m" -> "M"
        "level_h" -> "H"
        else -> null
    }

    private fun recommendedComparator(): Comparator<QuizUi> =
        compareByDescending<QuizUi> { it.isSaved }
            .thenByDescending { it.inProgress }
            .thenBy { it.passed }
            .thenByDescending { it.lastAttemptAt ?: Instant.MIN }

    private fun groupByCategory(quizzes: List<QuizUi>): Map<String, List<QuizUi>> {
        if (quizzes.isEmpty()) return emptyMap()
        return quizzes.groupBy { it.category }
            .toSortedMap(CASE_INSENSITIVE_ORDER)
    }

    private fun groupedWithFallback(filters: QuizFilters): Map<String, List<QuizUi>> {
        val filtered = applyFilters(latestQuizzes, filters)
        return if (filtered.isNotEmpty() || latestQuizzes.isEmpty()) {
            filtered
        } else {
            groupByCategory(latestQuizzes)
        }
    }

    private fun logEvent(name: String, params: Map<String, Any?> = emptyMap()) {
        // TODO hook analytics
    }

    private fun <T> Set<T>.toggle(item: T): Set<T> =
        if (contains(item)) minus(item) else plus(item)

    private fun List<FilterChipState>.updateSelection(
        chipId: String,
        selectedKeys: Set<String>
    ): List<FilterChipState> = map { chip ->
        chip.copy(selected = chip.id in selectedKeys)
    }
}

enum class FilterGroup { Goal, Duration, Level }
