package com.eb5.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eb5.app.R
import com.eb5.app.data.model.Project
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProjectDetailScreen(project: Project, onBack: () -> Unit) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = project.image,
            contentDescription = project.title,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
        Text(text = project.title, style = MaterialTheme.typography.headlineSmall)
        Text(text = project.fullDescription)
        Text(text = stringResource(R.string.project_location, project.location))
        Text(text = stringResource(R.string.project_category, project.category))
        Text(text = stringResource(R.string.project_tea_status, project.teaStatus))
        Text(text = stringResource(R.string.project_job_model, project.jobCreationModel))
        Text(text = stringResource(R.string.project_investment, currencyFormatter.format(project.minInvestmentUsd)))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.action_back))
        }
    }
}
