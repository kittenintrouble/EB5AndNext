package com.eb5.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eb5.app.R
import com.eb5.app.data.model.Project
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProjectsScreen(
    projects: List<Project>,
    onProjectSelected: (Project) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(projects, key = { it.id }) { project ->
            ProjectCard(project = project, onClick = { onProjectSelected(project) })
        }
    }
}

@Composable
fun ProjectCard(project: Project, onClick: () -> Unit) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.US)
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AsyncImage(
                model = project.image,
                contentDescription = project.title,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Text(text = project.title, style = MaterialTheme.typography.titleMedium)
            Text(text = project.shortDescription)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(R.string.project_location, project.location))
                Text(text = stringResource(R.string.project_category, project.category))
            }
            Text(
                text = stringResource(R.string.project_investment, numberFormat.format(project.minInvestmentUsd)),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
