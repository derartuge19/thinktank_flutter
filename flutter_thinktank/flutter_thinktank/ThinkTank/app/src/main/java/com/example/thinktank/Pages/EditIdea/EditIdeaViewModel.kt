package com.example.thinktank.Pages.EditIdea

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinktank.data.TokenManager
import com.example.thinktank.data.api.ApiClient
import com.example.thinktank.data.models.Idea
import com.example.thinktank.data.models.IdeaUpdateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response

class EditIdeaViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val _uiState = MutableStateFlow(EditIdeaUiState())
    val uiState: StateFlow<EditIdeaUiState> = _uiState

    var onUpdateSuccess: () -> Unit = {}

    override fun onCleared() {
        super.onCleared()
        // Clear any pending callbacks
        onUpdateSuccess = {}
    }

    fun loadIdea(ideaId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val token = tokenManager.getToken()
                if (token == null) {
                    _uiState.update { it.copy(error = "Not authenticated", isLoading = false) }
                    return@launch
                }

                val response = ApiClient.ideaApi.getUserIdeas("Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.find { it.id == ideaId }?.let { idea ->
                        _uiState.update {
                            it.copy(
                                ideaId = idea.id,
                                title = idea.title,
                                description = idea.description,
                                tags = idea.tags?.joinToString(","),
                                isLoading = false
                            )
                        }
                    } ?: run {
                        _uiState.update { it.copy(error = "Idea not found", isLoading = false) }
                    }
                } else {
                    _uiState.update { it.copy(error = "Failed to load idea", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "An error occurred", isLoading = false) }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, error = null) }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription, error = null) }
    }

    fun onTagsChange(newTags: String) {
        _uiState.update { it.copy(tags = newTags, error = null) }
    }

    fun onUpdateIdea() {
        if (!validateInput()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _uiState.update { it.copy(error = "Not authenticated") }
                    return@launch
                }

                _uiState.update { it.copy(isUpdating = true, error = null) }

                val request = IdeaUpdateRequest(
                    title = _uiState.value.title,
                    description = _uiState.value.description,
                    tags = _uiState.value.tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
                )

                val response = ApiClient.ideaApi.updateIdea("Bearer $token", _uiState.value.ideaId, request)

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isUpdating = false, updateSuccess = true) }
                    // Delay navigation to show success message
                    kotlinx.coroutines.delay(2000) // 2 seconds delay
                    onUpdateSuccess()
                } else {
                    val errorMessage = when (response.code()) {
                        403 -> "You don't have permission to edit this idea"
                        404 -> "Idea not found"
                        else -> "Failed to update idea"
                    }
                    _uiState.update { it.copy(error = errorMessage, isUpdating = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "An error occurred", isUpdating = false) }
            }
        }
    }

    private fun validateInput(): Boolean {
        val title = _uiState.value.title.trim()
        val description = _uiState.value.description.trim()
        val tags = _uiState.value.tags?.trim()

        return when {
            title.isEmpty() -> {
                _uiState.update { it.copy(error = "Title cannot be empty") }
                false
            }
            title.length < 3 -> {
                _uiState.update { it.copy(error = "Title must be at least 3 characters long") }
                false
            }
            description.isEmpty() -> {
                _uiState.update { it.copy(error = "Description cannot be empty") }
                false
            }
            description.length < 10 -> {
                _uiState.update { it.copy(error = "Description must be at least 10 characters long") }
                false
            }
            tags?.isNotEmpty() == true && tags.split(",").any { it.trim().length < 2 } -> {
                _uiState.update { it.copy(error = "Each tag must be at least 2 characters long") }
                false
            }
            else -> true
        }
    }
}
