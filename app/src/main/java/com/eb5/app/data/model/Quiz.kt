package com.eb5.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QuizTopic(
    val termId: Int,
    val title: String,
    val questions: List<QuizQuestion>
) {
    val id: String = termId.toString()
}

@Serializable
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)