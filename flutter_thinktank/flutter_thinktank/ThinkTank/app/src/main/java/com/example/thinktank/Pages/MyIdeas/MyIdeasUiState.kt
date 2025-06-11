package com.example.thinktank.Pages.MyIdeas

import com.example.thinktank.data.models.Idea

data class MyIdeasUiState(
    val ideas: List<Idea> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigationEvent: NavigationEvent? = null
)
