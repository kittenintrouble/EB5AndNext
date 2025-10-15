package com.eb5.app.ui.quizzes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.eb5.app.ui.quizzes.QuizzesTab.All
import com.eb5.app.ui.quizzes.QuizzesTab.Results
import com.eb5.app.ui.quizzes.QuizzesTab.Tracks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant

private const val KEY_TAB = "quizzes_tab"

class QuizzesViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sampleQuizzes = buildSampleQuizzes()
    private val sampleTracks = buildSampleTracks()
    private val sampleResults = buildSampleResults()
    private val sampleCertificates = buildSampleCertificates()

    private val _state = MutableStateFlow(
        QuizzesUiState(
            tab = savedStateHandle.get<String>(KEY_TAB)?.let { runCatching { QuizzesTab.valueOf(it) }.getOrNull() }
                ?: Tracks,
            resumeQuizzes = sampleQuizzes.filter { it.inProgress },
            tracks = sampleTracks,
            allQuizzes = sampleQuizzes.groupBy { it.category },
            filters = QuizFilters(),
            filterChips = buildFilterChips(),
            results = sampleResults,
            certificates = sampleCertificates
        )
    )
    val state: StateFlow<QuizzesUiState> = _state.asStateFlow()

    fun onTabSelected(tab: QuizzesTab) {
        if (tab == _state.value.tab) return
        savedStateHandle[KEY_TAB] = tab.name
        _state.update { it.copy(tab = tab) }
        logEvent("quizzes_tab_view", mapOf("tab" to tab.name))
    }

    fun onToggleFilterChip(group: FilterGroup, chipId: String) {
        when (group) {
            FilterGroup.Goal -> toggleSetChip(group, chipId)
            FilterGroup.Duration -> toggleChipExclusive(chipId) { id ->
                when (id) {
                    "duration_short" -> 0..5
                    "duration_medium" -> 6..10
                    "duration_long" -> 11..Int.MAX_VALUE
                    else -> null
                }
            }
            FilterGroup.Format -> toggleSetChip(group, chipId)
            FilterGroup.Level -> toggleSetChip(group, chipId)
            FilterGroup.Sort -> toggleSort(chipId)
        }
        logEvent("quizzes_filter_apply", mapOf("filters" to _state.value.filters))
    }

    fun onResetFilters() {
        _state.update {
            it.copy(
                filters = QuizFilters(),
                filterChips = buildFilterChips()
            )
        }
    }

    fun onSavedToggle(quizId: String, isSaved: Boolean) {
        val updated = sampleQuizzes.map {
            if (it.id == quizId) it.copy(isSaved = isSaved) else it
        }
        _state.update {
            it.copy(
                resumeQuizzes = updated.filter { quiz -> quiz.inProgress },
                allQuizzes = updated.groupBy { quiz -> quiz.category }
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

    fun onCertificateDownload(trackId: String) {
        logEvent("results_certificate_download", mapOf("trackId" to trackId))
    }

    fun onCertificateShare(trackId: String) {
        logEvent("results_certificate_share", mapOf("trackId" to trackId))
    }

    private fun toggleSetChip(group: FilterGroup, chipId: String) {
        _state.update { state ->
            val filters = state.filters
            val updatedFilters = when (group) {
                FilterGroup.Goal -> filters.copy(goals = filters.goals.toggle(chipId))
                FilterGroup.Format -> filters.copy(formats = filters.formats.toggle(chipId))
                FilterGroup.Level -> filters.copy(levels = filters.levels.toggle(chipId))
                else -> filters
            }
            val updatedChips = when (group) {
                FilterGroup.Goal -> state.filterChips.copy(
                    goals = state.filterChips.goals.updateSelection(chipId, updatedFilters.goals)
                )
                FilterGroup.Format -> state.filterChips.copy(
                    formats = state.filterChips.formats.updateSelection(chipId, updatedFilters.formats)
                )
                FilterGroup.Level -> state.filterChips.copy(
                    levels = state.filterChips.levels.updateSelection(chipId, updatedFilters.levels)
                )
                else -> state.filterChips
            }
            state.copy(filters = updatedFilters, filterChips = updatedChips)
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
            state.copy(
                filters = state.filters.copy(duration = newRange),
                filterChips = state.filterChips.copy(durations = updatedChips)
            )
        }
    }

    private fun toggleSort(chipId: String) {
        val sort = when (chipId) {
            "sort_recommended" -> QuizSortOption.Recommended
            "sort_inprogress" -> QuizSortOption.InProgressFirst
            "sort_newest" -> QuizSortOption.Newest
            "sort_difficulty" -> QuizSortOption.Difficulty
            else -> QuizSortOption.Recommended
        }
        _state.update { state ->
            state.copy(
                filters = state.filters.copy(sort = sort),
                filterChips = state.filterChips.copy(
                    sorts = state.filterChips.sorts.map { chip ->
                        chip.copy(selected = chip.id == chipId)
                    }
                )
            )
        }
    }

    private fun buildFilterChips(): QuizFilterUi = QuizFilterUi(
        goals = listOf(
            FilterChipState("goal_deal_ready", "Deal-ready"),
            FilterChipState("goal_source_funds", "Source of Funds"),
            FilterChipState("goal_compliance", "Compliance")
        ),
        durations = listOf(
            FilterChipState("duration_short", "≤5 min"),
            FilterChipState("duration_medium", "6–10 min"),
            FilterChipState("duration_long", "10+ min")
        ),
        formats = listOf(
            FilterChipState("format_scenario", "Scenario"),
            FilterChipState("format_calculator", "Calculator"),
            FilterChipState("format_timeline", "Timeline"),
            FilterChipState("format_multi", "Multi")
        ),
        levels = listOf(
            FilterChipState("level_l", "Beginner"),
            FilterChipState("level_m", "Intermediate"),
            FilterChipState("level_h", "Advanced")
        ),
        sorts = listOf(
            FilterChipState("sort_recommended", "Recommended", selected = true),
            FilterChipState("sort_inprogress", "In-progress first"),
            FilterChipState("sort_newest", "Newest"),
            FilterChipState("sort_difficulty", "Difficulty")
        )
    )

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

enum class FilterGroup { Goal, Duration, Format, Level, Sort }

private fun buildSampleQuizzes(): List<QuizUi> = listOf(
    QuizUi(
        id = "quiz_eb5_basics_v1",
        title = "EB-5 Basics and Program Timeline",
        category = "EB-5 Basics",
        format = "Multi",
        level = "L",
        durationMin = 5,
        questionsCount = 5,
        tags = listOf("basics", "timeline"),
        bestScore = 4,
        passed = true,
        lastAttemptAt = Instant.now().minusSeconds(3600 * 24 * 3),
        inProgress = false,
        isSaved = true
    ),
    QuizUi(
        id = "quiz_invest_models_v1",
        title = "Investment Models: Debt vs Equity",
        category = "Investment",
        format = "Scenario",
        level = "M",
        durationMin = 6,
        questionsCount = 5,
        tags = listOf("deal-structure", "coupon"),
        bestScore = 3,
        passed = false,
        lastAttemptAt = Instant.now().minusSeconds(3600 * 12),
        inProgress = true,
        isSaved = false
    ),
    QuizUi(
        id = "quiz_risk_return_v1",
        title = "Risk and Return Trade-offs",
        category = "Risk & Compliance",
        format = "Scenario",
        level = "H",
        durationMin = 8,
        questionsCount = 6,
        tags = listOf("risk", "portfolio"),
        bestScore = null,
        passed = false,
        lastAttemptAt = null,
        inProgress = false,
        isSaved = false
    )
)

private fun buildSampleTracks(): List<TrackUi> = listOf(
    TrackUi(
        id = "track_deal_ready_20m",
        title = "Deal-Ready in 20 minutes",
        description = "A focused set of quizzes to become deal-ready fast.",
        quizIds = listOf("quiz_eb5_basics_v1", "quiz_invest_models_v1", "quiz_risk_return_v1"),
        estimatedDurationMin = 20,
        completed = 1,
        total = 3,
        certificateAvailable = false
    ),
    TrackUi(
        id = "track_source_of_funds",
        title = "Source of Funds Mastery",
        description = "Master lawful source of funds documentation with targeted scenarios.",
        quizIds = listOf("quiz_sof_intro", "quiz_sof_paths", "quiz_sof_review"),
        estimatedDurationMin = 18,
        completed = 3,
        total = 3,
        certificateAvailable = true
    )
)

private fun buildSampleResults(): List<AttemptSummary> = listOf(
    AttemptSummary(
        id = "attempt_1",
        title = "Investment Models: Debt vs Equity",
        quizId = "quiz_invest_models_v1",
        trackId = null,
        bestScore = 3,
        totalQuestions = 5,
        level = "M",
        durationMin = 6,
        completedAt = Instant.now().minusSeconds(3600 * 24 * 2)
    ),
    AttemptSummary(
        id = "attempt_2",
        title = "Deal-Ready in 20 minutes",
        quizId = null,
        trackId = "track_deal_ready_20m",
        bestScore = 12,
        totalQuestions = 16,
        level = "H",
        durationMin = 20,
        completedAt = Instant.now().minusSeconds(3600 * 24 * 10)
    )
)

private fun buildSampleCertificates(): List<CertificateUi> = listOf(
    CertificateUi(
        trackId = "track_source_of_funds",
        title = "Source of Funds Mastery",
        completedAt = Instant.now().minusSeconds(3600 * 24 * 12)
    )
)
