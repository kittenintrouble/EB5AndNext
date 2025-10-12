package com.eb5.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eb5.app.R
import com.eb5.app.data.model.QuizProgress
import com.eb5.app.data.model.QuizTopic

@Composable
fun QuizzesScreen(
    quizzes: List<QuizTopic>,
    quizProgress: Map<String, QuizProgress>,
    onQuizSelected: (QuizTopic) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(quizzes, key = { it.id }) { quiz ->
            val progress = quizProgress[quiz.id]
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = quiz.title, style = MaterialTheme.typography.titleMedium)
                    Text(text = stringResource(R.string.quiz_question_count, quiz.questions.size))
                    Text(text = stringResource(R.string.quiz_best_score, progress?.bestScore ?: 0))
                    Button(onClick = { onQuizSelected(quiz) }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(R.string.quiz_start))
                    }
                }
            }
        }
    }
}
