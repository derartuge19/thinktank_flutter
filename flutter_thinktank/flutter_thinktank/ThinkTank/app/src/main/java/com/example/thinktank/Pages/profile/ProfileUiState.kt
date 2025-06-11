package com.example.thinktank.Pages.Profile

import com.example.thinktank.data.models.Idea
import com.example.thinktank.data.models.User
import com.example.thinktank.Pages.Profile.Status

data class ProfileUiState(
    val userName: String = "",
    val email: String = "",
    val bio: String = "",
    val status: Status = Status.PENDING,
    val submittedIdeas: List<Idea> = emptyList(),
    val isFeedbackExpanded: Boolean = false,
    val feedback: String = "",
    val isDrawerOpen: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null
)

