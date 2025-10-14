package com.eb5.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.eb5.app.R
import com.eb5.app.ui.projects.projectStatusLabel
import com.eb5.app.ui.projects.projectTypeLabel
import com.eb5.app.data.model.Project
import com.eb5.app.data.model.ProjectImage
import com.eb5.app.ui.projects.ProjectDetailUiState
import java.text.NumberFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectDetailScreen(
    state: ProjectDetailUiState,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.project != null -> {
            ProjectDetailContent(
                project = state.project,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                onBack = onBack
            )
        }

        else -> {
            ProjectDetailError(
                message = state.error ?: stringResource(R.string.project_detail_error),
                onRetry = onRetry,
                onBack = onBack
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProjectDetailContent(
    project: Project,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val heroImageUrl = project.heroImageUrl
    val gallery = remember(project.images, heroImageUrl) {
        val remoteImages = project.images?.takeIf { it.isNotEmpty() } ?: emptyList()
        when {
            remoteImages.isNotEmpty() -> remoteImages
            !heroImageUrl.isNullOrBlank() -> listOf(ProjectImage(heroImageUrl, project.title))
            else -> emptyList()
        }
    }
    val pagerState = rememberPagerState { if (gallery.isNotEmpty()) gallery.size else 1 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable { onBack() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = stringResource(R.string.action_back),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onToggleFavorite) {
                val icon = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder
                val cd = if (isFavorite) R.string.project_remove_favorite else R.string.project_add_favorite
                Icon(imageVector = icon, contentDescription = stringResource(cd))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            if (gallery.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val image = gallery[page]
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(image.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = image.alt,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        },
                        error = {
                            Image(
                                painter = painterResource(id = R.drawable.bg_us_flag),
                                contentDescription = image.alt,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                if (gallery.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(gallery.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .height(6.dp)
                                    .width(if (isSelected) 18.dp else 6.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.bg_us_flag),
                    contentDescription = project.title,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val typeLabel = remember(project.type) {
                        projectTypeLabel(context, project.type)
                    }
                    val statusLabel = remember(project.status) {
                        projectStatusLabel(context, project.status)
                    }
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        typeLabel?.let { DetailMetaPill(label = stringResource(R.string.project_category_label), value = it) }
                        statusLabel?.let { DetailMetaPill(label = stringResource(R.string.project_status_label), value = it) }
                    }


                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        project.location?.takeIf { it.isNotBlank() }?.let {
                            InfoRow(icon = Icons.Outlined.Place, text = stringResource(R.string.project_location, it))
                        }
                        project.expectedOpening?.takeIf { it.isNotBlank() }?.let {
                            InfoRow(icon = Icons.Outlined.Event, text = stringResource(R.string.project_expected_opening, it))
                        }
                        project.developer?.takeIf { it.isNotBlank() }?.let {
                            InfoRow(icon = Icons.Outlined.Badge, text = stringResource(R.string.project_developer, it))
                        }
                    }
                }
            }

            val minInvestment = project.financials?.minInvestment ?: project.minInvestmentUsd
            val financialItems = buildList {
                project.financials?.totalProject?.let { add(stringResource(R.string.project_financial_total, currencyFormatter.format(it))) }
                project.financials?.eb5Offering?.let { add(stringResource(R.string.project_financial_offering, currencyFormatter.format(it))) }
                minInvestment?.let { add(stringResource(R.string.project_financial_minimum, currencyFormatter.format(it))) }
                project.financials?.eb5Investors?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_financial_investors, it))
                }
                project.financials?.term?.let {
                    add(stringResource(R.string.project_financial_term, it.toString()))
                }
                project.financials?.interestRate?.let {
                    val rate = String.format(Locale.US, "%.1f%%", it)
                    add(stringResource(R.string.project_financial_interest, rate))
                }
            }

            val loanItems = buildList {
                project.loanStructure?.type?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_loan_type, it))
                }
                project.loanStructure?.annualReturn?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_loan_return, it))
                }
                project.loanStructure?.termYears?.let {
                    add(stringResource(R.string.project_loan_term, it.toString()))
                }
                project.loanStructure?.escrow?.let {
                    val value = if (it) stringResource(R.string.common_yes) else stringResource(R.string.common_no)
                    add(stringResource(R.string.project_loan_escrow, value))
                }
            }

            val teaItems = buildList {
                project.tea?.type?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_tea_type, it))
                }
                project.tea?.designation?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_tea_designation, it))
                }
            }

            val uscisItems = buildList {
                project.uscis?.i956fStatus?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_uscis_i956f, it))
                }
                project.uscis?.i526eStatus?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_uscis_i526e, it))
                }
                formatDate(project.uscis?.approvalDate)?.let {
                    add(stringResource(R.string.project_uscis_approval_date, it))
                }
            }

            val jobsItems = buildList {
                project.jobs?.total?.let {
                    add(stringResource(R.string.project_jobs_total, it.toString()))
                }
                project.jobs?.perInvestor?.let {
                    add(stringResource(R.string.project_jobs_per_investor, String.format(Locale.US, "%.1f", it)))
                }
                (project.jobs?.model ?: project.jobCreationModel)?.takeIf { it.isNotBlank() }?.let {
                    add(stringResource(R.string.project_jobs_model_label, it))
                }
            }

            ProjectDetailSection(
                icon = Icons.Outlined.AttachMoney,
                title = stringResource(R.string.project_financials_title),
                items = financialItems,
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProjectDetailSection(
                    icon = Icons.Outlined.Gavel,
                    title = stringResource(R.string.project_uscis_title),
                    items = uscisItems,
                    modifier = Modifier.weight(1f)
                )
                ProjectDetailSection(
                    icon = Icons.Outlined.Map,
                    title = stringResource(R.string.project_tea_title),
                    items = teaItems,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProjectDetailSection(
                    icon = Icons.Outlined.Badge,
                    title = stringResource(R.string.project_loan_title),
                    items = loanItems,
                    modifier = Modifier.weight(1f)
                )
                ProjectDetailSection(
                    icon = Icons.Outlined.Work,
                    title = stringResource(R.string.project_jobs_title),
                    items = jobsItems,
                    modifier = Modifier.weight(1f)
                )
            }

            project.fullDescription?.takeIf { it.isNotBlank() }?.let {
                HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val visaOptions = remember {
                listOf(
                    "H-1B",
                    "F-1",
                    "B-1/B-2",
                    "O-1",
                    "L-1",
                    "J-1",
                    "E-2",
                    "TN",
                    "U.S. Green Card",
                    "U.S. Citizen",
                    "Other"
                )
            }
            var firstName by rememberSaveable { mutableStateOf("") }
            var lastName by rememberSaveable { mutableStateOf("") }
            var email by rememberSaveable { mutableStateOf("") }
            var phone by rememberSaveable { mutableStateOf("") }
            var countryBirth by rememberSaveable { mutableStateOf("") }
            var countryLiving by rememberSaveable { mutableStateOf("") }
            var selectedVisa by rememberSaveable { mutableStateOf("") }
            var visaExpanded by remember { mutableStateOf(false) }
            var accreditedInvestor by rememberSaveable { mutableStateOf(false) }

            Text(
                text = stringResource(R.string.project_more_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.project_more_first_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.project_more_last_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.project_more_email)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.project_more_phone)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = countryBirth,
                    onValueChange = { countryBirth = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.project_more_country_birth)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = countryLiving,
                    onValueChange = { countryLiving = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.project_more_country_living)) },
                    modifier = Modifier.fillMaxWidth()
                )

                val focusManager = LocalFocusManager.current
                val density = LocalDensity.current
                var visaFieldWidth by remember { mutableStateOf(0) }
                val visaInteraction = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(interactionSource = visaInteraction, indication = null) {
                            focusManager.clearFocus()
                            visaExpanded = true
                        }
                ) {
                    OutlinedTextField(
                        value = selectedVisa,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.project_more_current_visa_status)) },
                        placeholder = { Text(stringResource(R.string.project_more_current_visa_status_hint)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.rotate(if (visaExpanded) 180f else 0f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { layoutCoordinates ->
                                visaFieldWidth = layoutCoordinates.size.width
                            },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedTextColor = if (selectedVisa.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        ),
                        interactionSource = visaInteraction
                    )
                    DropdownMenu(
                        expanded = visaExpanded,
                        onDismissRequest = { visaExpanded = false },
                        modifier = Modifier.width(with(density) { visaFieldWidth.toDp() })
                    ) {
                        visaOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedVisa = option
                                    visaExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = accreditedInvestor,
                        onCheckedChange = { accreditedInvestor = it }
                    )
                    Text(
                        text = stringResource(R.string.project_more_accredited),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { accreditedInvestor = !accreditedInvestor }
                    )
                }

                Button(
                    onClick = { /* TODO handle form submission */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Text(
                        text = stringResource(R.string.project_more_submit).uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProjectDetailSection(
    icon: ImageVector,
    title: String,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    Surface(modifier = modifier, tonalElevation = 2.dp, shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { raw ->
                    Text(text = boldAfterColon(raw), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ProjectDetailError(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.project_detail_retry))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.action_back))
        }
    }
}

private fun formatDate(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    return runCatching {
        OffsetDateTime.parse(raw).atZoneSameInstant(ZoneId.systemDefault()).format(formatter)
    }.getOrElse {
        runCatching { Instant.parse(raw).atZone(ZoneId.systemDefault()).format(formatter) }.getOrNull()
    }
}

private fun boldAfterColon(text: String): AnnotatedString {
    val idx = text.indexOf(":")
    return if (idx >= 0 && idx + 1 < text.length) {
        buildAnnotatedString {
            append(text.substring(0, idx + 2))
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                append(text.substring(idx + 2).trimStart())
            }
        }
    } else {
        AnnotatedString(text)
    }
}


@Composable
private fun DetailMetaPill(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
