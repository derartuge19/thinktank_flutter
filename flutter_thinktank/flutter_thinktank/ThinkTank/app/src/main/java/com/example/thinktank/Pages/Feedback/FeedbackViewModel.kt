package com.example.thinktank.Pages.Feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinktank.data.api.ApiClient
import com.example.thinktank.data.models.Idea
import com.example.thinktank.data.models.IdeaStatus
import com.example.thinktank.data.models.Feedback
import com.example.thinktank.data.models.CreateFeedbackRequest
import com.example.thinktank.data.models.UpdateFeedbackRequest
import com.example.thinktank.data.models.FeedbackStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import android.util.Base64
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException

// UI state for Feedback screens
data class FeedbackUiState(
    val ideas: List<Idea> = emptyList(),
    val approvedIdeas: List<Idea> = emptyList(),
    val rejectedIdeas: List<Idea> = emptyList(),
    val selectedIdea: Idea? = null,
    val feedbacks: Map<Int, Feedback?> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FeedbackViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    fun selectIdea(idea: Idea) {
        _uiState.update { it.copy(selectedIdea = idea) }
    }

    fun loadIdeas(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val formattedToken = if (!token.startsWith("Bearer ")) "Bearer $token" else token
                // Use getAllIdeas endpoint for admin
                val response = ApiClient.ideaApi.getAllIdeas(formattedToken)
                
                if (response.isSuccessful && response.body() != null) {
                    val ideas = response.body()!!
                    println("\n=== Loading All Ideas for Idea Pool ===")
                    println("Total ideas received: ${ideas.size}")
                    
                    _uiState.update { it.copy(ideas = ideas, isLoading = false) }
                } else {
                    println("Failed to load ideas")
                    _uiState.update { it.copy(error = "Failed to load ideas", isLoading = false) }
                }
            } catch (e: Exception) {
                println("Error in loadIdeas: ${e.message}")
                e.printStackTrace()
                _uiState.update { it.copy(error = e.message ?: "An error occurred", isLoading = false) }
            }
        }
    }

    fun loadApprovedIdeas(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val formattedToken = if (!token.startsWith("Bearer ")) "Bearer $token" else token
                // Use getAllIdeas endpoint for admin
                val response = ApiClient.ideaApi.getAllIdeas(formattedToken)
                
                if (response.isSuccessful && response.body() != null) {
                    val ideas = response.body()!!
                    println("\n=== Loading All Ideas for Review ===")
                    println("Total ideas received: ${ideas.size}")
                    
                    // Get feedback for each idea
                    val ideasWithFeedback = ideas.map { idea ->
                        try {
                            println("\nProcessing idea: ${idea.id}")
                            println("Idea title: ${idea.title}")
                            
                            // Convert idea.id from String to Int
                            val ideaId = idea.id.toIntOrNull() ?: 0
                            println("Fetching feedback for idea ID: $ideaId")
                            
                            // Use getFeedbackByIdeaId to get feedback from feedback table
                            val feedbackResponse = ApiClient.feedbackApi.getFeedbackByIdeaId(
                                formattedToken,
                                ideaId
                            )
                            
                            if (feedbackResponse.isSuccessful && feedbackResponse.body() != null) {
                                val feedbacks = feedbackResponse.body()!!
                                println("Feedbacks received for idea $ideaId: ${feedbacks.size}")
                                
                                if (feedbacks.isNotEmpty()) {
                                    // Get the latest feedback
                                    val latestFeedback = feedbacks
                                        .sortedByDescending { it.createdAt ?: "" }
                                        .first()
                                    
                                    println("Latest feedback comment: ${latestFeedback.comment}")
                                    
                                    // Update the idea with the feedback list
                                    idea.copy(
                                        feedback = listOf(latestFeedback),
                                        tags = idea.tags ?: emptyList()
                                    )
                                } else {
                                    println("No feedbacks found for idea $ideaId")
                                    idea.copy(
                                        feedback = emptyList(),
                                        tags = idea.tags ?: emptyList()
                                    )
                                }
                            } else {
                                println("Failed to get feedback for idea $ideaId")
                                idea.copy(
                                    feedback = emptyList(),
                                    tags = idea.tags ?: emptyList()
                                )
                            }
                        } catch (e: Exception) {
                            println("Error processing feedback for idea ${idea.id}: ${e.message}")
                            e.printStackTrace()
                            idea.copy(
                                feedback = emptyList(),
                                tags = idea.tags ?: emptyList()
                            )
                        }
                    }
                    
                    println("\n=== Final Ideas with Feedback ===")
                    ideasWithFeedback.forEach { idea ->
                        println("Idea ${idea.id}:")
                        println("- Title: ${idea.title}")
                        println("- Feedback count: ${idea.feedback?.size ?: 0}")
                    }
                    
                    _uiState.update { it.copy(ideas = ideasWithFeedback, isLoading = false) }
                } else {
                    println("Failed to load ideas")
                    _uiState.update { it.copy(error = "Failed to load ideas", isLoading = false) }
                }
            } catch (e: Exception) {
                println("Error in loadApprovedIdeas: ${e.message}")
                e.printStackTrace()
                _uiState.update { it.copy(error = e.message ?: "An error occurred", isLoading = false) }
            }
        }
    }

    fun updateFeedback(token: String, ideaId: Int, feedback: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val formattedToken = if (!token.startsWith("Bearer ")) "Bearer $token" else token
                // First get the existing feedback for this idea
                val feedbackResponse = ApiClient.feedbackApi.getFeedbackByIdeaId(
                    formattedToken,
                    ideaId
                )
                
                if (feedbackResponse.isSuccessful && feedbackResponse.body() != null) {
                    val feedbacks = feedbackResponse.body()!!
                    val latestFeedback = feedbacks.maxByOrNull { it.createdAt ?: "" }
                    
                    if (latestFeedback != null) {
                        println("Updating feedback ID: ${latestFeedback.id}")
                        // Update existing feedback
                        val updateResponse = ApiClient.feedbackApi.updateFeedback(
                            formattedToken,
                            latestFeedback.id,
                            UpdateFeedbackRequest(
                                comment = feedback,
                                status = latestFeedback.status
                            )
                        )
                        
                        if (updateResponse.isSuccessful) {
                            println("Feedback updated successfully")
                            // Reload ideas to refresh the UI
                            loadApprovedIdeas(token)
                        } else {
                            println("Failed to update feedback: ${updateResponse.code()}")
                            _uiState.update { it.copy(error = "Failed to update feedback", isLoading = false) }
                        }
                    } else {
                        println("No existing feedback found, creating new feedback")
                        // Create new feedback if none exists
                        val createResponse = ApiClient.feedbackApi.createFeedback(
                            formattedToken,
                            CreateFeedbackRequest(
                                ideaId = ideaId,
                                comment = feedback,
                                status = FeedbackStatus.Approved
                            )
                        )
                        
                        if (createResponse.isSuccessful) {
                            println("Feedback created successfully")
                            // Reload ideas to refresh the UI
                            loadApprovedIdeas(token)
                        } else {
                            println("Failed to create feedback: ${createResponse.code()}")
                            _uiState.update { it.copy(error = "Failed to create feedback", isLoading = false) }
                        }
                    }
                } else {
                    println("Failed to get existing feedback")
                    _uiState.update { it.copy(error = "Failed to get existing feedback", isLoading = false) }
                }
            } catch (e: Exception) {
                println("Error in updateFeedback: ${e.message}")
                e.printStackTrace()
                _uiState.update { it.copy(error = e.message ?: "An error occurred", isLoading = false) }
            }
        }
    }

    fun deleteFeedback(token: String, ideaId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val formattedToken = if (!token.startsWith("Bearer ")) "Bearer $token" else token
                // First get the existing feedback for this idea
                val feedbackResponse = ApiClient.feedbackApi.getFeedbackByIdeaId(
                    formattedToken,
                    ideaId
                )
                
                if (feedbackResponse.isSuccessful && feedbackResponse.body() != null) {
                    val feedbacks = feedbackResponse.body()!!
                    val latestFeedback = feedbacks.maxByOrNull { it.createdAt ?: "" }
                    
                    if (latestFeedback != null) {
                        println("Deleting feedback ID: ${latestFeedback.id}")
                        // Delete the feedback
                        val deleteResponse = ApiClient.feedbackApi.deleteFeedback(
                            formattedToken,
                            latestFeedback.id
                        )
                        
                        if (deleteResponse.isSuccessful) {
                            println("Feedback deleted successfully")
                            // Reload ideas to refresh the UI
                            loadApprovedIdeas(token)
                        } else {
                            println("Failed to delete feedback: ${deleteResponse.code()}")
                            _uiState.update { it.copy(error = "Failed to delete feedback", isLoading = false) }
                        }
                    } else {
                        println("No feedback found to delete")
                        _uiState.update { it.copy(error = "No feedback found to delete", isLoading = false) }
                    }
                } else {
                    println("Failed to get existing feedback")
                    _uiState.update { it.copy(error = "Failed to get existing feedback", isLoading = false) }
                }
            } catch (e: Exception) {
                println("Error in deleteFeedback: ${e.message}")
                e.printStackTrace()
                _uiState.update { it.copy(error = e.message ?: "An error occurred", isLoading = false) }
            }
        }
    }

    fun submitFeedback(token: String, ideaId: Int, comment: String, isApproved: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val formattedToken = if (!token.startsWith("Bearer ")) "Bearer $token" else token
                val response = ApiClient.feedbackApi.createFeedback(
                    formattedToken,
                    CreateFeedbackRequest(
                        ideaId = ideaId,
                        comment = comment,
                        status = if (isApproved) FeedbackStatus.Approved else FeedbackStatus.Rejected
                    )
                )
                
                if (response.isSuccessful) {
                    println("Feedback submitted successfully")
                    // Reload ideas to refresh the UI
                    loadApprovedIdeas(token)
                } else {
                    println("Failed to submit feedback: ${response.code()}")
                    _uiState.update { it.copy(error = "Failed to submit feedback", isLoading = false) }
                }
            } catch (e: Exception) {
                println("Error in submitFeedback: ${e.message}")
                e.printStackTrace()
                _uiState.update { it.copy(error = e.message ?: "An error occurred", isLoading = false) }
            }
        }
    }
} 