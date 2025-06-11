package com.example.thinktank.Pages.Ideas

import com.example.thinktank.data.models.Idea

data class IdeasUiState(
    val ideas: List<Idea> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val deletingIdeaId: String? = null
)
