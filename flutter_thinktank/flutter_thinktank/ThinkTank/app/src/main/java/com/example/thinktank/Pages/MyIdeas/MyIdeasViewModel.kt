package com.example.thinktank.Pages.MyIdeas

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinktank.data.TokenManager
import com.example.thinktank.data.api.ApiClient
import com.example.thinktank.data.models.Idea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class MyIdeasViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val _uiState = MutableStateFlow(MyIdeasUiState())
    val uiState: StateFlow<MyIdeasUiState> = _uiState

    init {
        loadIdeas()
    }

    private fun getUserIdFromToken(token: String): String? {
        return try {
            // Remove "Bearer " prefix if present
            val cleanToken = if (token.startsWith("Bearer ")) token.substring(7) else token
            
            // Split the token into parts
            val parts = cleanToken.split(".")
            if (parts.size != 3) {
                Log.e("MyIdeasViewModel", "Invalid token format: wrong number of parts")
                return null
            }
            
            // Decode the payload
            val payload = parts[1]
            val decodedPayload = String(Base64.decode(payload, Base64.URL_SAFE))
            Log.d("MyIdeasViewModel", "Decoded payload: $decodedPayload")
            
            // Extract user ID from the payload
            val userId = decodedPayload.split("\"id\":")[1].split(",")[0].trim()
            Log.d("MyIdeasViewModel", "Extracted user ID: $userId")
            userId
        } catch (e: Exception) {
            Log.e("MyIdeasViewModel", "Error extracting user ID from token", e)
            null
        }
    }

    fun loadIdeas() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _uiState.update { it.copy(error = "Not authenticated", isLoading = false) }
                    return@launch
                }

                val response = ApiClient.ideaApi.getUserIdeas("Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { ideas ->
                        _uiState.update { it.copy(ideas = ideas, isLoading = false) }
                    }
                } else {
                    _uiState.update { it.copy(error = "Failed to load ideas", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "An error occurred", isLoading = false) }
            }
        }
    }

    fun deleteIdea(ideaId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _uiState.update { it.copy(error = "Not authenticated") }
                    return@launch
                }

                val response = ApiClient.ideaApi.deleteIdea("Bearer $token", ideaId)

                if (response.isSuccessful) {
                    // Remove the deleted idea from the list
                    _uiState.update { currentState ->
                        currentState.copy(
                            ideas = currentState.ideas.filter { it.id != ideaId }
                        )
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        403 -> "You don't have permission to delete this idea"
                        404 -> "Idea not found"
                        else -> "Failed to delete idea"
                    }
                    _uiState.update { it.copy(error = errorMessage) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "An error occurred") }
            }
        }
    }

    fun onEditIdea(idea: Idea) {
        _uiState.update { it.copy(navigationEvent = NavigationEvent.EditIdea(idea)) }
    }

    fun clearNavigationEvent() {
        _uiState.update { it.copy(navigationEvent = null) }
    }
}

sealed class NavigationEvent {
    data class EditIdea(val idea: Idea) : NavigationEvent()
}
