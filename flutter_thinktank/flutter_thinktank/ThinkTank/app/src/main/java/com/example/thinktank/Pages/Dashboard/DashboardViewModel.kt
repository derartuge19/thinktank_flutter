package com.example.thinktank.Pages.Dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinktank.data.api.ApiClient
import com.example.thinktank.data.models.Idea
import com.example.thinktank.data.models.FeedbackStatus
import com.example.thinktank.data.models.Feedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // For preview purposes
    fun setPreviewState(approvedIdeas: List<Idea>) {
        println("\n=== Setting Preview State ===")
        println("Setting ${approvedIdeas.size} preview ideas")
        _uiState.value = _uiState.value.copy(
            approvedIdeas = approvedIdeas,
            isDrawerOpen = false
        )
    }

    fun loadApprovedIdeas(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val formattedToken = if (!token.startsWith("Bearer ")) "Bearer $token" else token
                println("\n=== Loading Ideas for Dashboard ===")
                println("Token format check: ${if (token.startsWith("Bearer ")) "Already has Bearer" else "Adding Bearer"}")
                
                // Try admin endpoint first
                println("Trying admin endpoint first")
                val feedbackResponse = ApiClient.feedbackApi.getAllFeedback(formattedToken)
                println("Feedback API Response code: ${feedbackResponse.code()}")
                
                if (feedbackResponse.isSuccessful && feedbackResponse.body() != null) {
                    // Admin flow
                    println("Admin flow: Processing feedback")
                    val allFeedback = feedbackResponse.body()!!
                    println("Total feedback received: ${allFeedback.size}")
                    
                    
                    val approvedFeedback = allFeedback.filter { feedback -> 
                        feedback.status == FeedbackStatus.Approved && feedback.idea != null 
                    }
                    println("Approved feedback: ${approvedFeedback.size}")
                    

                    val approvedIdeaIds = approvedFeedback.mapNotNull { it.idea?.id }.distinct()
                    println("Unique approved idea IDs: ${approvedIdeaIds.size}")
                    

                    val ideasResponse = ApiClient.ideaApi.getAllIdeas(formattedToken)
                    if (ideasResponse.isSuccessful && ideasResponse.body() != null) {
                        val allIdeas = ideasResponse.body()!!
                        val approvedIdeas = allIdeas.filter { idea -> 
                            approvedIdeaIds.contains(idea.id)
                        }
                        
                        println("Admin: Approved ideas loaded: ${approvedIdeas.size}")
                        approvedIdeas.forEach { idea ->
                            println("Admin: Approved idea: ${idea.id} - ${idea.title}")
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            approvedIdeas = approvedIdeas,
                            isLoading = false
                        )
                    } else {
                        val errorBody = ideasResponse.errorBody()?.string()
                        println("Admin: Failed to load ideas: ${ideasResponse.code()} - $errorBody")
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to load ideas: ${errorBody ?: "Unknown error"}",
                            isLoading = false
                        )
                    }
                } else {
                   
                    println("Regular user flow: Getting public ideas")
                    try {
                        val publicResponse = ApiClient.ideaApi.getPublicIdeas()
                        println("Public ideas API Response code: ${publicResponse.code()}")
                        println("Public ideas API Response body: ${publicResponse.body()}")
                        
                        if (publicResponse.isSuccessful && publicResponse.body() != null) {
                            val publicIdeas = publicResponse.body()!!
                            println("Public ideas loaded: ${publicIdeas.size}")
                            publicIdeas.forEach { idea ->
                                println("Public idea: ${idea.id} - ${idea.title}")
                            }
                            
                            _uiState.value = _uiState.value.copy(
                                approvedIdeas = publicIdeas,
                                isLoading = false
                            )
                        } else {
                            val errorBody = publicResponse.errorBody()?.string()
                            println("Failed to load public ideas: ${publicResponse.code()} - $errorBody")
                            _uiState.value = _uiState.value.copy(
                                error = "Failed to load ideas: ${errorBody ?: "Unknown error"}",
                                isLoading = false
                            )
                        }
                    } catch (e: Exception) {
                        println("Error in public ideas flow: ${e.message}")
                        e.printStackTrace()
                        _uiState.value = _uiState.value.copy(
                            error = "Error loading ideas: ${e.message}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                println("Error loading ideas: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "An error occurred",
                    isLoading = false
                )
            }
        }
    }

    fun onDrawerOpen() {
        _uiState.value = _uiState.value.copy(isDrawerOpen = true)
    }

    fun onDrawerClose() {
        _uiState.value = _uiState.value.copy(isDrawerOpen = false)
    }
}
