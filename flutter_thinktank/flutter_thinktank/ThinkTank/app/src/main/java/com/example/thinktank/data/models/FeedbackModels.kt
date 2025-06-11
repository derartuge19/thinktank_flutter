package com.example.thinktank.data.models

enum class FeedbackStatus {
    Reviewed,
    Approved,
    Rejected
}

data class Feedback(
    val id: Int,
    val comment: String,
    val admin: User,
    val idea: Idea,
    val status: FeedbackStatus,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deletedAt: String? = null
)

data class CreateFeedbackRequest(
    val ideaId: Int,
    val comment: String,
    val status: FeedbackStatus = FeedbackStatus.Reviewed
)

data class UpdateFeedbackRequest(
    val comment: String?,
    val status: FeedbackStatus?
) 