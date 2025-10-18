package com.eb5.app.ui.screens

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.onFocusChanged
import com.eb5.app.BuildConfig
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.Toast

import android.util.Patterns
import android.util.Log

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.TextField
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
import com.eb5.app.ui.localization.stringResource
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
import com.eb5.app.ui.components.DetailTopBar
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
        DetailTopBar(
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            onBack = onBack,
            favoriteOnContentDescription = R.string.project_remove_favorite,
            favoriteOffContentDescription = R.string.project_add_favorite
        )

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
                    val imageUrl = if (image.url.contains("unsplash.com")) {
                        "${image.url}?w=800&q=80&fm=jpg&fit=crop"
                    } else {
                        image.url
                    }
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        project.developer?.takeIf { it.isNotBlank() }?.let {
                            InfoRow(icon = Icons.Outlined.Badge, text = stringResource(R.string.project_developer, it))
                        }
                        project.location?.takeIf { it.isNotBlank() }?.let {
                            InfoRow(icon = Icons.Outlined.Place, text = stringResource(R.string.project_location, it))
                        }
                        project.expectedOpening?.takeIf { it.isNotBlank() }?.let {
                            InfoRow(icon = Icons.Outlined.Event, text = stringResource(R.string.project_expected_opening, it))
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
                HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(0.dp))

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
            var firstName by remember { mutableStateOf("") }
            var lastName by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }
            var countryBirth by remember { mutableStateOf("") }
            var countryLiving by remember { mutableStateOf("") }
            var selectedVisa by remember { mutableStateOf("") }
            var visaExpanded by remember { mutableStateOf(false) }
            var accreditedInvestor by remember { mutableStateOf(false) }
            var attemptedSubmit by remember { mutableStateOf(false) }
            var showErrors by remember { mutableStateOf(false) }
            fun isEmailValid(value: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(value).matches()
            fun phoneDigits(value: String): String = value.filter { it.isDigit() }
            val isFormValid by remember(firstName, lastName, email, phone, countryBirth, countryLiving, selectedVisa, accreditedInvestor) {
                mutableStateOf(
                    firstName.isNotBlank() &&
                            lastName.isNotBlank() &&
                            email.isNotBlank() && isEmailValid(email) &&
                            phoneDigits(phone).length >= 7 &&
                            countryBirth.isNotBlank() &&
                            countryLiving.isNotBlank() &&
                            selectedVisa.isNotBlank() &&
                            accreditedInvestor
                )
            }

            var submitting by remember { mutableStateOf(false) }
            var submittedSuccessfully by remember { mutableStateOf(false) }

            val scope = rememberCoroutineScope()
            val reqFirstName = remember { BringIntoViewRequester() }
            val reqLastName = remember { BringIntoViewRequester() }
            val reqEmail = remember { BringIntoViewRequester() }
            val reqPhone = remember { BringIntoViewRequester() }
            val reqCountryBirth = remember { BringIntoViewRequester() }
            val reqCountryLiving = remember { BringIntoViewRequester() }
            val reqVisa = remember { BringIntoViewRequester() }
            val reqAccredited = remember { BringIntoViewRequester() }

            fun bringFirstErrorIntoView() {
                val requester = when {
                    firstName.isBlank() -> reqFirstName
                    lastName.isBlank() -> reqLastName
                    !isEmailValid(email) -> reqEmail
                    phoneDigits(phone).length < 7 -> reqPhone
                    countryBirth.isBlank() -> reqCountryBirth
                    countryLiving.isBlank() -> reqCountryLiving
                    selectedVisa.isBlank() -> reqVisa
                    !accreditedInvestor -> reqAccredited
                    else -> null
                }
                requester?.let { scope.launch { it.bringIntoView() } }
            }
            if (!submittedSuccessfully) {
                Text(
                    text = stringResource(R.string.project_more_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "All fields are required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!submittedSuccessfully) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    singleLine = true,
                    enabled = !submitting,
                    isError = showErrors && firstName.isBlank(),
                    label = { RequiredLabel(stringResource(R.string.project_more_first_name)) },
                    placeholder = { Text("John") },
                    supportingText = {
                        if (showErrors && firstName.isBlank()) {
                            Text(text = stringResource(R.string.common_required))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(reqFirstName)
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    singleLine = true,
                    enabled = !submitting,
                    isError = showErrors && lastName.isBlank(),
                    label = { RequiredLabel(stringResource(R.string.project_more_last_name)) },
                    placeholder = { Text("Smith") },
                    supportingText = {
                        if (showErrors && lastName.isBlank()) {
                            Text(text = stringResource(R.string.common_required))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(reqLastName)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    enabled = !submitting,
                    isError = showErrors && (email.isBlank() || !isEmailValid(email)),
                    label = { RequiredLabel(stringResource(R.string.project_more_email)) },
                    placeholder = { Text("john.smith@example.com") },
                    supportingText = {
                        if (showErrors && (email.isBlank() || !isEmailValid(email))) {
                            Text(text = if (email.isBlank()) stringResource(R.string.common_required) else stringResource(R.string.common_invalid_email))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(reqEmail)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    singleLine = true,
                    enabled = !submitting,
                    isError = showErrors && phoneDigits(phone).length < 7,
                    label = { RequiredLabel(stringResource(R.string.project_more_phone)) },
                    placeholder = { Text("+1 (555) 123-4567") },
                    supportingText = {
                        if (showErrors && phoneDigits(phone).length < 7) {
                            Text(text = stringResource(R.string.common_invalid_phone))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(reqPhone)
                )
                OutlinedTextField(
                    value = countryBirth,
                    onValueChange = { countryBirth = it },
                    singleLine = true,
                    enabled = !submitting,
                    isError = showErrors && countryBirth.isBlank(),
                    label = { RequiredLabel(stringResource(R.string.project_more_country_birth)) },
                    placeholder = { Text("China") },
                    supportingText = {
                        if (showErrors && countryBirth.isBlank()) {
                            Text(text = stringResource(R.string.common_required))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(reqCountryBirth)
                )
                OutlinedTextField(
                    value = countryLiving,
                    onValueChange = { countryLiving = it },
                    singleLine = true,
                    enabled = !submitting,
                    isError = showErrors && countryLiving.isBlank(),
                    label = { RequiredLabel(stringResource(R.string.project_more_country_living)) },
                    placeholder = { Text("United States") },
                    supportingText = {
                        if (showErrors && countryLiving.isBlank()) {
                            Text(text = stringResource(R.string.common_required))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(reqCountryLiving)
                )

                val focusManager = LocalFocusManager.current

                ExposedDropdownMenuBox(
                    expanded = visaExpanded,
                    onExpandedChange = {
                        if (!submitting) {
                            visaExpanded = !visaExpanded
                            if (visaExpanded) focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = selectedVisa,
                        onValueChange = {},
                        readOnly = true,
                        enabled = !submitting,
                        isError = showErrors && selectedVisa.isBlank(),
                        label = { RequiredLabel(stringResource(R.string.project_more_current_visa_status)) },
                        placeholder = { Text(stringResource(R.string.project_more_current_visa_status_hint)) },
                        supportingText = {
                            if (showErrors && selectedVisa.isBlank()) {
                                Text(text = stringResource(R.string.common_required))
                            }
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = visaExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = !submitting)
                            .fillMaxWidth()
                            .bringIntoViewRequester(reqVisa),
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = visaExpanded,
                        onDismissRequest = { visaExpanded = false }
                    ) {
                        visaOptions.forEach { option ->
                            androidx.compose.material3.DropdownMenuItem(
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.bringIntoViewRequester(reqAccredited)
                ) {
                    Checkbox(
                        checked = accreditedInvestor,
                        onCheckedChange = { if (!submitting) accreditedInvestor = it },
                        enabled = !submitting
                    )
                    Text(
                        text = stringResource(R.string.project_more_accredited),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { if (!submitting) accreditedInvestor = !accreditedInvestor }
                    )
                }
                if (showErrors && !accreditedInvestor) {
                    Text(
                        text = stringResource(R.string.common_required),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                }
                // Form submission helper
                suspend fun submitForm(): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
                    try {
                        val endpoint = BuildConfig.PROJECT_FORM_ENDPOINT
                        Log.d("ProjectForm", "=== FORM SUBMISSION START ===")
                        Log.d("ProjectForm", "URL: $endpoint")

                        val url = java.net.URL(endpoint)
                        val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
                            requestMethod = "POST"
                            connectTimeout = 15000
                            readTimeout = 20000
                            doOutput = true
                            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                            Log.d("ProjectForm", "Content-Type: application/json; charset=UTF-8")
                            if (BuildConfig.PROJECT_FORM_API_KEY.isNotEmpty()) {
                                setRequestProperty("x-api-key", BuildConfig.PROJECT_FORM_API_KEY)
                                Log.d("ProjectForm", "API Key: ${BuildConfig.PROJECT_FORM_API_KEY}")
                            } else {
                                Log.d("ProjectForm", "API Key: NOT SET (public endpoint)")
                            }
                        }

                        val payload = JSONObject().apply {
                            put("project_id", project.id ?: JSONObject.NULL)
                            put("project_title", project.title ?: JSONObject.NULL)
                            put("first_name", firstName)
                            put("last_name", lastName)
                            put("email", email)
                            put("phone", phone)
                            put("country_of_birth", countryBirth)
                            put("country_of_living", countryLiving)
                            put("current_visa_status", selectedVisa)
                            put("accredited_investor", accreditedInvestor)
                            put("timestamp", System.currentTimeMillis())
                        }

                        val jsonString = payload.toString()
                        Log.d("ProjectForm", "JSON Payload: $jsonString")

                        conn.outputStream.use { os ->
                            os.write(jsonString.toByteArray(Charsets.UTF_8))
                        }

                        Log.d("ProjectForm", "Request sent, waiting for response...")

                        val code = conn.responseCode
                        Log.d("ProjectForm", "HTTP Status Code: $code")

                        val body = try {
                            (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() }
                        } catch (e: Exception) {
                            Log.e("ProjectForm", "Error reading response body", e)
                            null
                        }

                        Log.d("ProjectForm", "Response Body: $body")
                        conn.disconnect()

                        Log.d("ProjectForm", "=== FORM SUBMISSION END ===")

                        if (code in 200..299) true to (body ?: "")
                        else false to (body ?: "Server error ($code)")
                    } catch (e: Exception) {
                        Log.e("ProjectForm", "Exception during form submission", e)
                        Log.e("ProjectForm", "Exception message: ${e.message}")
                        Log.e("ProjectForm", "Exception type: ${e.javaClass.name}")
                        false to (e.message ?: "Network error")
                    }
                }

                if (submittedSuccessfully) {
                    // Success message UI
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Thank You!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Your inquiry has been submitted successfully. We will contact you within 24 hours.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            showErrors = true
                            if (isFormValid && !submitting) {
                                scope.launch {
                                    submitting = true
                                    val (ok, msg) = submitForm()
                                    submitting = false
                                    if (ok) {
                                        // Clear form on success
                                        firstName = ""
                                        lastName = ""
                                        email = ""
                                        phone = ""
                                        countryBirth = ""
                                        countryLiving = ""
                                        selectedVisa = ""
                                        accreditedInvestor = false
                                        visaExpanded = false
                                        showErrors = false
                                        submittedSuccessfully = true
                                    } else {
                                        val message = if (!msg.isNullOrBlank()) msg else context.getString(R.string.common_submitted_fail)
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        bringFirstErrorIntoView()
                                    }
                                }
                            } else if (!isFormValid) {
                                bringFirstErrorIntoView()
                            }
                        },
                        enabled = !submitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        if (submitting) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(
                                text = stringResource(R.string.project_more_submit).uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Composable
private fun RequiredLabel(text: String) {
    Text(
        buildAnnotatedString {
            append(text)
            append(" ")
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                append("*")
            }
        }
    )
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
