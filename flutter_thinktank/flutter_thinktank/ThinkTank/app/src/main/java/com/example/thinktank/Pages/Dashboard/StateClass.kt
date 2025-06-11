package com.example.thinktank.Pages.Dashboard

import com.example.thinktank.data.models.Idea

data class DashboardUiState(
    val approvedIdeas: List<Idea> = emptyList(),
    val isDrawerOpen: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class Project(
    val title: String,
    val subtitle: String,
    val description: String
)
