package com.example.thinktank.data.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Idea(
    val id: String,
    val title: String,
    val description: String,
    val status: String,
    val tags: List<String> = emptyList(),
    @SerializedName("createdAt")
    val createdAt: Date = Date(),
    @SerializedName("user")
    val user: User? = null,
    @SerializedName("feedback")
    val feedback: List<Feedback>? = null
)

enum class IdeaStatus {
    @SerializedName("Pending")
    PENDING,
    @SerializedName("Reviewed")
    REVIEWED,
    @SerializedName("Approved")
    APPROVED,
    @SerializedName("Rejected")
    REJECTED;

    companion object {
        fun fromString(value: String): IdeaStatus {
            println("Converting status string: '$value'")
            return when (value) {
                "Pending" -> PENDING
                "Reviewed" -> REVIEWED
                "Approved" -> APPROVED
                "Rejected" -> REJECTED
                else -> {
                    println("Unknown status value: '$value', defaulting to PENDING")
                    PENDING
                }
            }
        }
    }
}

data class IdeaSubmissionRequest(
    val title: String,
    val description: String,
    val tags: List<String>? = null
)

data class IdeaUpdateRequest(
    val title: String,
    val description: String,
    val tags: List<String>? = null
) 