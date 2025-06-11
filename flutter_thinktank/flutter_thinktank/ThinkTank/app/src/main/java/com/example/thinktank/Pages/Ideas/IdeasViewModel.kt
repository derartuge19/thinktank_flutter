package com.example.thinktank.Pages.Ideas

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinktank.data.TokenManager
import com.example.thinktank.data.api.ApiClient
import com.example.thinktank.data.models.Idea
import com.example.thinktank.data.models.IdeaStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class IdeasViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val _uiState = MutableStateFlow(IdeasUiState())
    val uiState: StateFlow<IdeasUiState> = _uiState

    fun loadIdeas() {
        val token = tokenManager.getToken()
        if (token == null) {
            Log.e("IdeasViewModel", "Token is null")
            _uiState.update { it.copy(error = "Please login to view ideas") }
            return
        }

        Log.d("IdeasViewModel", "Loading ideas with token: ${token.take(10)}...")
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val formattedToken = "Bearer $token"
                Log.d("IdeasViewModel", "Making API call to getUserIdeas")
                val response = ApiClient.ideaApi.getUserIdeas(formattedToken)

                launch(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val ideas = response.body() ?: emptyList()
                        Log.d("IdeasViewModel", "Successfully loaded ${ideas.size} ideas")
                        _uiState.update { it.copy(ideas = ideas, isLoading = false) }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("IdeasViewModel", "Failed to load ideas: ${response.code()} - $errorBody")
                        _uiState.update { 
                            it.copy(
                                error = errorBody ?: "Failed to load ideas",
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("IdeasViewModel", "Error loading ideas", e)
                launch(Dispatchers.Main) {
                    _uiState.update { 
                        it.copy(
                            error = "Error loading ideas: ${e.localizedMessage}",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun deleteIdea(ideaId: String) {
        val token = tokenManager.getToken()
        if (token == null) {
            _uiState.update { it.copy(error = "Please login to delete ideas") }
            return
        }

        _uiState.update { it.copy(isDeleting = true, deletingIdeaId = ideaId) }
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val formattedToken = "Bearer $token"
                val response = ApiClient.ideaApi.deleteIdea(formattedToken, ideaId)

                launch(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Remove the deleted idea from the list
                        _uiState.update { currentState ->
                            currentState.copy(
                                ideas = currentState.ideas.filter { it.id != ideaId },
                                isDeleting = false,
                                deletingIdeaId = null
                            )
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        _uiState.update { 
                            it.copy(
                                error = errorBody ?: "Failed to delete idea",
                                isDeleting = false,
                                deletingIdeaId = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    _uiState.update { 
                        it.copy(
                            error = "Error deleting idea: ${e.localizedMessage}",
                            isDeleting = false,
                            deletingIdeaId = null
                        )
                    }
                }
            }
        }
    }
} 
