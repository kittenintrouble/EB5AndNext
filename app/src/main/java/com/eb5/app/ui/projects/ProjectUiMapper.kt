package com.eb5.app.ui.projects

import android.content.Context
import com.eb5.app.R
import java.util.Locale

private enum class KnownProjectType(val key: String, val labelRes: Int) {
    LODGING("lodging", R.string.project_type_lodging),
    HOTEL("hotel", R.string.project_type_hotel),
    RETAIL("retail", R.string.project_type_retail),
    MIXED_USE("mixed-use", R.string.project_type_mixed_use),
    MIXED_USE_ALT("mixed_use", R.string.project_type_mixed_use),
    INFRASTRUCTURE("infrastructure", R.string.project_type_infrastructure)
}

private enum class KnownProjectStatus(val key: String, val labelRes: Int) {
    PLANNING("planning", R.string.project_status_planning_value),
    CONSTRUCTION("construction", R.string.project_status_construction_value),
    COMPLETED("completed", R.string.project_status_completed_value)
}

fun projectTypeLabel(context: Context, raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    val normalized = value.lowercase(Locale.US)
    val match = KnownProjectType.values().firstOrNull { it.key == normalized }
    return match?.let { context.getString(it.labelRes) } ?: raw
}

fun projectStatusLabel(context: Context, raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    val normalized = value.lowercase(Locale.US)
    val match = KnownProjectStatus.values().firstOrNull { it.key == normalized }
    return match?.let { context.getString(it.labelRes) } ?: raw
}
