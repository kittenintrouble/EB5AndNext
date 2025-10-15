package com.eb5.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eb5.app.R
import com.eb5.app.data.model.QuizTopic

@Composable
fun QuizRunnerScreen(
    topic: QuizTopic,
    onCompleted: (score: Int) -> Unit,
    onExit: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    val totalQuestions = topic.questions.size
    val question = topic.questions[currentIndex]
    val answeredCorrectly = selectedOption == question.correctAnswerIndex && selectedOption != null

    fun moveToNext(correct: Boolean) {
        val updatedScore = if (correct) score + 1 else score
        if (currentIndex == totalQuestions - 1) {
            onCompleted(updatedScore)
        } else {
            if (correct) {
                score += 1
            }
            currentIndex += 1
            selectedOption = null
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
                val isCorrectOption = index == question.correctAnswerIndex
                val containerColor = when {
                    !isSelected && selectedOption != null && isCorrectOption -> MaterialTheme.colorScheme.secondaryContainer
                    isSelected && answeredCorrectly -> MaterialTheme.colorScheme.primaryContainer
                    isSelected && !answeredCorrectly -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor)
                ) {
                    Button(
                        onClick = { selectedOption = index },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
                    ) {
                        Text(text = optionText)
                    }
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
        OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.quiz_exit))
        }
    }
}
