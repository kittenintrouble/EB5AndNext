package com.eb5.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: String,
    val title: String,
    val shortDescription: String,
    val image: String,
    val category: String,
    val location: String,
    val teaStatus: String,
    val minInvestmentUsd: Long,
    val jobCreationModel: String,
    val fullDescription: String
)
