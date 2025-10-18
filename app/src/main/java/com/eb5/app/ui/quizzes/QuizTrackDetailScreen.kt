package com.eb5.app.ui.quizzes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.eb5.app.ui.localization.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eb5.app.R
import com.eb5.app.data.model.QuizProgress
import com.eb5.app.data.model.QuizTopic
import com.eb5.app.data.model.QuizTrack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTrackDetailScreen(
    track: QuizTrack,
    quizzes: List<QuizTopic>,
    progress: Map<String, QuizProgress>,
    onStartQuiz: (String) -> Unit,
    onBack: () -> Unit
) {
    val completedCount = track.quizIds.count { id ->
        val total = quizzes.firstOrNull { it.id == id }?.questions?.size ?: 0
        val best = progress[id]?.bestScore ?: 0
        total > 0 && best >= total
    }
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.clickable(onClick = onBack, role = Role.Button),
                title = { Text(track.title, maxLines = 1, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = track.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(
                            R.string.quizzes_track_duration,
                            track.estimatedDurationMinutes,
                            track.quizIds.size
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(
                            R.string.quiz_track_progress,
                            completedCount,
                            track.quizIds.size
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (quizzes.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.quiz_track_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(quizzes, key = { it.id }) { topic ->
                    TrackQuizCard(
                        topic = topic,
                        progress = progress[topic.id],
                        onStart = { onStartQuiz(topic.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun TrackQuizCard(
    topic: QuizTopic,
    progress: QuizProgress?,
    onStart: () -> Unit
) {
    val totalQuestions = topic.questions.size
    val bestScore = progress?.bestScore ?: 0
    val lastScore = progress?.lastScore ?: 0
    val completed = bestScore >= totalQuestions && totalQuestions > 0
    val inProgress = !completed && lastScore in 1 until totalQuestions
    val buttonLabel = when {
        inProgress -> stringResource(R.string.quizzes_cta_resume)
        completed -> stringResource(R.string.quizzes_cta_retake)
        else -> stringResource(R.string.quizzes_cta_start)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(topic.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            topic.summary?.takeIf { it.isNotBlank() }?.let { summary ->
                Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = stringResource(
                    R.string.quiz_track_quiz_meta,
                    topic.questions.size,
                    topic.durationMinutes
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (progress != null && totalQuestions > 0) {
                val label = stringResource(R.string.quizzes_best_score_label, bestScore, totalQuestions)
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                Text(buttonLabel)
            }
        }
    }
}
