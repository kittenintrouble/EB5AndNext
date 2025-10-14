package com.eb5.app.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: String,
    @SerialName("slug")
    @SerializedName("slug")
    val slug: String? = null,
    @SerialName("lang")
    @SerializedName("lang")
    val lang: String? = null,
    @SerialName("type")
    @SerializedName("type")
    val type: String? = null,
    @SerialName("location")
    @SerializedName("location")
    val location: String? = null,
    @SerialName("status")
    @SerializedName("status")
    val status: String? = null,
    @SerialName("developer")
    @SerializedName("developer")
    val developer: String? = null,
    @SerialName("expectedOpening")
    @SerializedName(value = "expectedOpening", alternate = ["expected_opening"])
    val expectedOpening: String? = null,
    @SerialName("title")
    @SerializedName("title")
    val title: String,
    @SerialName("shortDescription")
    @SerializedName(value = "shortDescription", alternate = ["short_description"])
    val shortDescription: String? = null,
    @SerialName("fullDescription")
    @SerializedName(value = "fullDescription", alternate = ["full_description"])
    val fullDescription: String? = null,
    @SerialName("images")
    @SerializedName("images")
    val images: List<ProjectImage>? = null,
    @SerialName("financials")
    @SerializedName("financials")
    val financials: Financials? = null,
    @SerialName("uscis")
    @SerializedName("uscis")
    val uscis: UscisInfo? = null,
    @SerialName("loanStructure")
    @SerializedName(value = "loanStructure", alternate = ["loan_structure"])
    val loanStructure: LoanStructure? = null,
    @SerialName("tea")
    @SerializedName("tea")
    val tea: TeaInfo? = null,
    @SerialName("jobs")
    @SerializedName("jobs")
    val jobs: JobCreation? = null,
    @SerialName("published")
    @SerializedName("published")
    val published: Boolean = false,
    @SerialName("publishedAt")
    @SerializedName(value = "publishedAt", alternate = ["published_at"])
    val publishedAt: String? = null,
    @SerialName("createdAt")
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String? = null,
    @SerialName("updatedAt")
    @SerializedName(value = "updatedAt", alternate = ["updated_at"])
    val updatedAt: String? = null,
    // Legacy fields (assets compatibility)
    @SerialName("image")
    @SerializedName("image")
    val image: String? = null,
    @SerialName("category")
    @SerializedName("category")
    val category: String? = null,
    @SerialName("teaStatus")
    @SerializedName(value = "teaStatus", alternate = ["tea_status"])
    val teaStatus: String? = null,
    @SerialName("minInvestmentUsd")
    @SerializedName(value = "minInvestmentUsd", alternate = ["min_investment_usd"])
    val minInvestmentUsd: Long? = null,
    @SerialName("jobCreationModel")
    @SerializedName(value = "jobCreationModel", alternate = ["job_creation_model"])
    val jobCreationModel: String? = null
) {
    val heroImageUrl: String?
        get() = images?.firstOrNull()?.url ?: image
}

@Serializable
data class ProjectImage(
    @SerialName("url")
    @SerializedName("url")
    val url: String,
    @SerialName("alt")
    @SerializedName("alt")
    val alt: String = ""
)

@Serializable
data class Financials(
    @SerialName("totalProject")
    @SerializedName(value = "totalProject", alternate = ["total_project"])
    val totalProject: Long? = null,
    @SerialName("eb5Offering")
    @SerializedName(value = "eb5Offering", alternate = ["eb5_offering"])
    val eb5Offering: Long? = null,
    @SerialName("minInvestment")
    @SerializedName(value = "minInvestment", alternate = ["min_investment"])
    val minInvestment: Long? = null,
    @SerialName("eb5Investors")
    @SerializedName(value = "eb5Investors", alternate = ["eb5_investors"])
    val eb5Investors: String? = null,
    @SerialName("term")
    @SerializedName("term")
    val term: Int? = null,
    @SerialName("interestRate")
    @SerializedName(value = "interestRate", alternate = ["interest_rate"])
    val interestRate: Double? = null
)

@Serializable
data class UscisInfo(
    @SerialName("i956fStatus")
    @SerializedName(value = "i956fStatus", alternate = ["i956f_status"])
    val i956fStatus: String? = null,
    @SerialName("i526eStatus")
    @SerializedName(value = "i526eStatus", alternate = ["i526e_status"])
    val i526eStatus: String? = null,
    @SerialName("approvalDate")
    @SerializedName(value = "approvalDate", alternate = ["approval_date"])
    val approvalDate: String? = null
)

@Serializable
data class LoanStructure(
    @SerialName("type")
    @SerializedName("type")
    val type: String? = null,
    @SerialName("annualReturn")
    @SerializedName(value = "annualReturn", alternate = ["annual_return"])
    val annualReturn: String? = null,
    @SerialName("termYears")
    @SerializedName(value = "termYears", alternate = ["term_years"])
    val termYears: Int? = null,
    @SerialName("escrow")
    @SerializedName("escrow")
    val escrow: Boolean? = null
)

@Serializable
data class TeaInfo(
    @SerialName("type")
    @SerializedName("type")
    val type: String? = null,
    @SerialName("designation")
    @SerializedName("designation")
    val designation: String? = null
)

@Serializable
data class JobCreation(
    @SerialName("total")
    @SerializedName("total")
    val total: Int? = null,
    @SerialName("perInvestor")
    @SerializedName(value = "perInvestor", alternate = ["per_investor"])
    val perInvestor: Double? = null,
    @SerialName("model")
    @SerializedName("model")
    val model: String? = null
)
