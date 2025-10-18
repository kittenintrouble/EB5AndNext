package com.eb5.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eb5.app.ui.localization.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eb5.app.R
import com.eb5.app.data.model.QuizTopic

@Composable
fun QuizResultScreen(
    topic: QuizTopic,
    score: Int,
    onRetake: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(R.string.quiz_result_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = topic.title, style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(R.string.quiz_result_score, score, topic.questions.size))
        Button(onClick = onRetake, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.quiz_retake))
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.action_back))
        }
    }
}
