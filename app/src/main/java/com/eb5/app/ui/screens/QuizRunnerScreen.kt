package com.eb5.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eb5.app.ui.localization.stringResource
import com.eb5.app.R
import com.eb5.app.data.model.QuizTopic
import com.eb5.app.data.model.QuizInProgressState
import androidx.compose.ui.graphics.Color

private val CorrectAnswerBackground = Color(0xFFDDF6E3)
private val CorrectAnswerContent = Color(0xFF216E3A)

@Composable
fun QuizRunnerScreen(
    topic: QuizTopic,
    initialState: QuizInProgressState? = null,
    onProgress: (QuizInProgressState?) -> Unit,
    onCompleted: (score: Int) -> Unit,
    onExit: () -> Unit
) {
    val totalQuestions = topic.questions.size
    var currentIndex by remember {
        mutableIntStateOf(initialState?.currentIndex?.coerceIn(0, (totalQuestions - 1).coerceAtLeast(0)) ?: 0)
    }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(initialState?.score?.coerceIn(0, totalQuestions) ?: 0) }
    val question = topic.questions[currentIndex]
    val answeredCorrectly = selectedOption == question.correctAnswerIndex && selectedOption != null
    val startedAt = remember { initialState?.startedAt ?: System.currentTimeMillis() }

    fun moveToNext(correct: Boolean) {
        val updatedScore = if (correct) score + 1 else score
        score = updatedScore
        if (currentIndex == totalQuestions - 1) {
            onProgress(null)
            onCompleted(updatedScore)
        } else {
            currentIndex += 1
            selectedOption = null
            onProgress(
                QuizInProgressState(
                    quizId = topic.id,
                    currentIndex = currentIndex,
                    score = updatedScore,
                    startedAt = startedAt,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = topic.title, style = MaterialTheme.typography.titleLarge)
        LinearProgressIndicator(progress = { (currentIndex + 1f) / totalQuestions })
        Text(text = stringResource(R.string.quiz_question_progress, currentIndex + 1, totalQuestions))
        Text(text = question.question, style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(question.options) { index, optionText ->
                val isSelected = selectedOption == index
                val showFeedback = selectedOption != null
                val isCorrectOption = index == question.correctAnswerIndex
                val backgroundColor = when {
                    showFeedback && isCorrectOption -> CorrectAnswerBackground
                    isSelected && !answeredCorrectly -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val contentColor = when {
                    showFeedback && isCorrectOption -> CorrectAnswerContent
                    isSelected && !answeredCorrectly -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedOption = index }
                        .padding(horizontal = 4.dp),
                    color = backgroundColor,
                    contentColor = contentColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = optionText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                    )
                }
            }
        }
        if (selectedOption != null) {
            if (answeredCorrectly) {
                Button(
                    onClick = { moveToNext(true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (currentIndex == totalQuestions - 1) stringResource(R.string.quiz_finish) else stringResource(
                            R.string.quiz_next
                        )
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { selectedOption = null }, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.quiz_change_answer))
                    }
                    Button(onClick = { moveToNext(false) }, modifier = Modifier.weight(1f)) {
                        Text(text = if (currentIndex == totalQuestions - 1) stringResource(R.string.quiz_finish) else stringResource(R.string.quiz_continue))
                    }
                }
            }
        }
        OutlinedButton(
            onClick = {
                onProgress(
                    QuizInProgressState(
                        quizId = topic.id,
                        currentIndex = currentIndex,
                        score = score,
                        startedAt = startedAt,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                onExit()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.quiz_exit))
        }
    }
}
