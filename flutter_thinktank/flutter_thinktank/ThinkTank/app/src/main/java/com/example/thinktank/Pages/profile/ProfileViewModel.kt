package com.example.thinktank.Pages.Profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinktank.data.models.Idea
import com.example.thinktank.data.models.User
import com.example.thinktank.data.remote.ApiService
import com.example.thinktank.data.TokenManager
import com.example.thinktank.utils.JwtUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import com.example.thinktank.Pages.Profile.Status

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val apiService: ApiService

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Create logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Create OkHttpClient with logging and timeout
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Create Retrofit instance with the client
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3444/") // Update this to match your backend URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(ApiService::class.java)
        loadProfile()
    }

    fun toggleFeedbackExpanded() {
        _uiState.update { currentState ->
            currentState.copy(isFeedbackExpanded = !currentState.isFeedbackExpanded)
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                tokenManager.clearToken()
                _uiState.value = ProfileUiState()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleDrawer() {
        _uiState.update { it.copy(isDrawerOpen = !it.isDrawerOpen) }
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val formattedToken = tokenManager.getFormattedToken()
                Log.d("ProfileViewModel", "Attempting to load profile. Token present: ${formattedToken != null}")
                
                if (formattedToken == null) {
                    Log.e("ProfileViewModel", "No token found")
                    _uiState.update { it.copy(
                        error = "No authentication token found. Please login again.",
                        isLoading = false
                    ) }
                    return@launch
                }

                Log.d("ProfileViewModel", "Using formatted token: ${formattedToken.take(20)}...")
                
                // Get user ID from token
                val userId = JwtUtils.getUserIdFromToken(formattedToken)
                if (userId == null) {
                    Log.e("ProfileViewModel", "Could not get user ID from token")
                    _uiState.update { it.copy(
                        error = "Could not get user ID from token",
                        isLoading = false
                    ) }
                    return@launch
                }

                Log.d("ProfileViewModel", "Got user ID from token: $userId")
                
                // Get user profile
                val response = apiService.getUserProfile(formattedToken, userId)
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        Log.d("ProfileViewModel", "Successfully loaded user profile")
                        _uiState.update { it.copy(
                            user = user,
                            userName = "${user.firstName} ${user.lastName}",
                            email = user.email,
                            bio = user.profile?.bio ?: "",
                            isLoading = false,
                            error = null
                        ) }
                    } else {
                        Log.e("ProfileViewModel", "User profile is null")
                        _uiState.update { it.copy(
                            error = "Failed to load user profile",
                            isLoading = false
                        ) }
                    }
                } else {
                    Log.e("ProfileViewModel", "Failed to load user profile: ${response.code()}")
                    _uiState.update { it.copy(
                        error = "Failed to load user profile: ${response.code()}",
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile", e)
                _uiState.update { it.copy(
                    error = "Error loading profile: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun loadIdeas() {
        viewModelScope.launch {
            try {
                // Ensure we have a valid state before updating
                if (_uiState.value == null) {
                    _uiState.value = ProfileUiState()
                }
                
                _uiState.update { it.copy(isLoading = true, error = null) }
                val token = tokenManager.getToken()
                if (token != null) {
                    val response = apiService.getUserIdeas("Bearer $token")
                    if (response.isSuccessful) {
                        response.body()?.let { ideas ->
                            _uiState.update { currentState ->
                                currentState.copy(
                                    submittedIdeas = ideas,
                                    isLoading = false
                                )
                            }
                        }
                    } else {
                        _uiState.update { it.copy(
                            error = "Failed to load ideas: ${response.message()}",
                            isLoading = false
                        ) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message,
                    isLoading = false
                ) }
            }
        }
    }

    fun deleteIdea(ideaId: String) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token != null) {
                    val response = apiService.deleteIdea("Bearer $token", ideaId)
                    if (response.isSuccessful) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                submittedIdeas = currentState.submittedIdeas.filter { it.id != ideaId }
                            )
                        }
                    } else {
                        _uiState.update { it.copy(error = "Failed to delete idea: ${response.message()}") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateStatus(status: Status) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token != null) {
                    val response = apiService.updateUserStatus("Bearer $token", status)
                    if (response.isSuccessful) {
                        _uiState.update { it.copy(status = status) }
                    } else {
                        _uiState.update { it.copy(error = "Failed to update status: ${response.message()}") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun reloadUserData() {
        loadProfile()
    }

    fun refreshProfile() {
        loadProfile()
    }

    fun deleteUser() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val formattedToken = tokenManager.getFormattedToken()
                Log.d("ProfileViewModel", "Attempting to delete user account")
                
                if (formattedToken == null) {
                    Log.e("ProfileViewModel", "No token found")
                    _uiState.update { it.copy(
                        error = "No authentication token found",
                        isLoading = false
                    ) }
                    return@launch
                }

                // Get user ID from token
                val userId = JwtUtils.getUserIdFromToken(formattedToken)
                if (userId == null) {
                    Log.e("ProfileViewModel", "Could not get user ID from token")
                    _uiState.update { it.copy(
                        error = "Could not get user ID from token",
                        isLoading = false
                    ) }
                    return@launch
                }

                Log.d("ProfileViewModel", "Got user ID from token: $userId")
                
                // First verify the user exists
                val userResponse = apiService.getUserProfile(formattedToken, userId)
                if (!userResponse.isSuccessful) {
                    Log.e("ProfileViewModel", "Failed to verify user: ${userResponse.code()}")
                    _uiState.update { it.copy(
                        error = "Failed to verify user account",
                        isLoading = false
                    ) }
                    return@launch
                }

                // Delete the user (backend will handle cascade deletion)
                val deleteUserResponse = apiService.deleteUser(formattedToken, userId)
                if (deleteUserResponse.isSuccessful) {
                    Log.d("ProfileViewModel", "User and profile deleted successfully")
                    // Clear the token and reset the UI state
                    tokenManager.clearToken()
                    _uiState.value = ProfileUiState()
                } else {
                    val errorBody = deleteUserResponse.errorBody()?.string()
                    Log.e("ProfileViewModel", "Failed to delete user: ${deleteUserResponse.code()}, Error: $errorBody")
                    _uiState.update { it.copy(
                        error = "Failed to delete account: ${errorBody ?: deleteUserResponse.message()}",
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error deleting account", e)
                _uiState.update { it.copy(
                    error = "Error deleting account: ${e.message}",
                    isLoading = false
                ) }
            } finally {
                // Ensure loading state is reset
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
