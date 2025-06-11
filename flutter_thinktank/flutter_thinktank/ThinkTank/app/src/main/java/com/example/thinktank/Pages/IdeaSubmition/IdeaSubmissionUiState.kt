package com.example.thinktank.Pages.IdeaSubmition

data class IdeaSubmissionUiState(
    val title: String = "",
    val description: String = "",
    val tags: String? = null,
    val isSubmitting: Boolean = false,
    val submissionSuccess: Boolean = false,
    val error: String? = null
)
