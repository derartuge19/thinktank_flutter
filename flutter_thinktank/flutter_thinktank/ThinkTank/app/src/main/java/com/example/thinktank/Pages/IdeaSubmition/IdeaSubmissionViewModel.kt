package com.example.thinktank.Pages.IdeaSubmition

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinktank.data.TokenManager
import com.example.thinktank.data.api.ApiClient
import com.example.thinktank.data.models.IdeaSubmissionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.withContext

open class IdeaSubmissionViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    open val _uiState = MutableStateFlow(IdeaSubmissionUiState())
    val uiState: StateFlow<IdeaSubmissionUiState> = _uiState

    var onSubmissionSuccess: () -> Unit = {}

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, error = null) }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription, error = null) }
    }

    fun onTagsChange(newTags: String) {
        _uiState.update { it.copy(tags = newTags, error = null) }
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

    fun onSubmitIdea() {
        val token = tokenManager.getToken()
        if (token == null) {
            _uiState.update { it.copy(error = "Please login to submit ideas") }
            return
        }

        if (!validateInput()) {
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null) }
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = IdeaSubmissionRequest(
                    title = _uiState.value.title.trim(),
                    description = _uiState.value.description.trim(),
                    tags = _uiState.value.tags?.let { tags ->
                        tags.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                    }
                )
                
                // Add Bearer prefix to the token
                val formattedToken = "Bearer $token"
                Log.d("IdeaSubmission", "Using token: $formattedToken")
                
                val response = ApiClient.ideaApi.submitIdea(formattedToken, request)

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isSubmitting = false, submissionSuccess = true) }
                        // Delay navigation to show success message
                        kotlinx.coroutines.delay(2000) // 2 seconds delay
                        onSubmissionSuccess()
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        403 -> "You don't have permission to submit ideas"
                        400 -> "Invalid idea data"
                        else -> "Failed to submit idea"
                    }
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(error = errorMessage, isSubmitting = false) }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "Connection timed out. Please try again."
                    is UnknownHostException -> "No internet connection. Please check your network."
                    else -> e.message ?: "An error occurred while submitting the idea"
                }
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(error = errorMessage, isSubmitting = false) }
                }
            }
        }
    }
}
