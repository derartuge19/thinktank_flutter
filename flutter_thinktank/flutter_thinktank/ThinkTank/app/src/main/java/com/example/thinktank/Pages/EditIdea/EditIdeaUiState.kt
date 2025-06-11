package com.example.thinktank.Pages.EditIdea

data class EditIdeaUiState(
    val ideaId: String = "",
    val title: String = "",
    val description: String = "",
    val tags: String? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val error: String? = null
)
