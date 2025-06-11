package com.example.thinktank.Pages.EditProfile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinktank.data.TokenManager
import com.example.thinktank.data.models.CreateProfileRequest
import com.example.thinktank.data.models.Profile
import com.example.thinktank.data.models.UpdateProfileRequest
import com.example.thinktank.data.models.UpdateUserRequest
import com.example.thinktank.data.remote.ApiService
import com.example.thinktank.utils.JwtUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val apiService: ApiService

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var initialProfile: Profile? = null
    private var currentUserId: Int? = null

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

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val token = tokenManager.getToken()
                if (token != null) {
                    Log.d("EditProfileViewModel", "Loading profile with token: ${token.take(10)}...")
                    
                    // Get user ID from token
                    val userId = JwtUtils.getUserIdFromToken(token)
                    if (userId == null) {
                        Log.e("EditProfileViewModel", "Could not get user ID from token")
                        _uiState.update { it.copy(
                            error = "Could not get user ID from token",
                            isLoading = false
                        ) }
                        return@launch
                    }
                    
                    Log.d("EditProfileViewModel", "Got user ID from token: $userId")
                    
                    // First get the user profile to get the ID
                    val userResponse = apiService.getUserProfile("Bearer $token", userId)
                    if (userResponse.isSuccessful) {
                        userResponse.body()?.let { user ->
                            Log.d("EditProfileViewModel", "User profile loaded successfully: id=${user.id}, name=${user.firstName} ${user.lastName}")
                            
                            // Get the profile ID from the user's profile object
                            val profileId = user.profile?.id
                            if (profileId == null) {
                                Log.e("EditProfileViewModel", "No profile ID found in user data")
                                _uiState.update { it.copy(
                                    error = "No profile found for user",
                                    isLoading = false
                                ) }
                                return@launch
                            }
                            
                            // Store the user ID
                            currentUserId = user.id
                            Log.d("EditProfileViewModel", "Successfully set currentUserId to: ${currentUserId}")
                            
                            // Try to get the profile details using the profile ID
                            val profileResponse = apiService.getProfile("Bearer $token", profileId)
                            if (profileResponse.isSuccessful) {
                                profileResponse.body()?.let { profile ->
                                    initialProfile = profile
                                    _uiState.update { currentState ->
                                        currentState.copy(
                                            firstName = user.firstName,
                                            lastName = user.lastName,
                                            email = user.email,
                                            profilePicture = profile.profilePicture,
                                            isLoading = false
                                        )
                                    }
                                }
                            } else if (profileResponse.code() == 404) {
                                // Profile doesn't exist, create one
                                Log.d("EditProfileViewModel", "Profile not found, creating new profile")
                                
                                // Create new profile
                                Log.d("EditProfileViewModel", "Creating new profile")
                                val createRequest = CreateProfileRequest(
                                    fullName = user.firstName + " " + user.lastName,
                                    email = user.email,
                                    profilePicture = null
                                )
                                Log.d("EditProfileViewModel", "Creating profile with request: $createRequest")
                                val createResponse = apiService.createProfile("Bearer $token", createRequest)
                                if (createResponse.isSuccessful) {
                                    createResponse.body()?.let { profile ->
                                        initialProfile = profile
                                        _uiState.update { currentState ->
                                            currentState.copy(
                                                firstName = user.firstName,
                                                lastName = user.lastName,
                                                email = user.email,
                                                profilePicture = profile.profilePicture,
                                                isLoading = false
                                            )
                                        }
                                    }
                                } else {
                                    val errorBody = createResponse.errorBody()?.string()
                                    Log.e("EditProfileViewModel", "Error creating profile: $errorBody")
                                    _uiState.update { it.copy(
                                        error = "Failed to create profile: ${createResponse.message()}",
                                        isLoading = false
                                    ) }
                                }
                            } else {
                                val errorBody = profileResponse.errorBody()?.string()
                                Log.e("EditProfileViewModel", "Error loading profile: $errorBody")
                                _uiState.update { it.copy(
                                    error = "Failed to load profile: ${profileResponse.message()}",
                                    isLoading = false
                                ) }
                            }
                        }
                    } else {
                        val errorBody = userResponse.errorBody()?.string()
                        Log.e("EditProfileViewModel", "Error loading user profile: $errorBody")
                        _uiState.update { it.copy(
                            error = "Failed to load user profile: ${userResponse.message()}",
                            isLoading = false
                        ) }
                    }
                } else {
                    Log.e("EditProfileViewModel", "No token found")
                }
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Exception loading profile", e)
                _uiState.update { it.copy(
                    error = "Failed to connect to server: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun onFieldChange(updater: (EditProfileUiState) -> EditProfileUiState) {
        _uiState.update { currentState -> updater(currentState) }
    }

    fun onSaveChanges() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val token = tokenManager.getToken()
                if (token != null) {
                    val userId = currentUserId
                    Log.d("EditProfileViewModel", "Attempting to update user for ID: $userId")
                    if (userId == null) {
                        Log.e("EditProfileViewModel", "User ID is null")
                        _uiState.update { it.copy(
                            error = "User ID not found. Please try again.",
                            isLoading = false
                        ) }
                        return@launch
                    }

                    val currentState = _uiState.value
                    Log.d("EditProfileViewModel", "Current state: firstName=${currentState.firstName}, lastName=${currentState.lastName}, email=${currentState.email}")

                    // Create update user request with only the fields that match the backend DTO
                    val updateUserRequest = UpdateUserRequest(
                        firstName = currentState.firstName,
                        lastName = currentState.lastName,
                        email = currentState.email
                    )

                    // Update user
                    val userResponse = apiService.updateUser("Bearer $token", userId, updateUserRequest)
                    if (!userResponse.isSuccessful) {
                        val errorBody = userResponse.errorBody()?.string()
                        Log.e("EditProfileViewModel", "Error updating user: $errorBody")
                        _uiState.update { it.copy(
                            error = "Failed to update user: ${userResponse.message()}",
                            isLoading = false
                        ) }
                        return@launch
                    }

                    Log.d("EditProfileViewModel", "User updated successfully")
                    
                    // Refresh the user profile to get updated data
                    val refreshResponse = apiService.getUserProfile("Bearer $token", userId)
                    if (refreshResponse.isSuccessful) {
                        refreshResponse.body()?.let { user ->
                            Log.d("EditProfileViewModel", "Profile refreshed successfully: firstName=${user.firstName}, lastName=${user.lastName}, email=${user.email}")
                            _uiState.update { it.copy(
                                firstName = user.firstName,
                                lastName = user.lastName,
                                email = user.email,
                                isLoading = false,
                                saveSuccess = true,
                                error = null
                            ) }
                        }
                    } else {
                        Log.e("EditProfileViewModel", "Error refreshing profile after update")
                        _uiState.update { it.copy(
                            isLoading = false,
                            saveSuccess = true,
                            error = null
                        ) }
                    }
                } else {
                    Log.e("EditProfileViewModel", "No token found for update")
                    _uiState.update { it.copy(
                        error = "No token found. Please login again.",
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Exception updating user and profile", e)
                _uiState.update { it.copy(
                    error = "Failed to connect to server: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun uploadProfilePicture(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val token = tokenManager.getToken()
                if (token != null) {
                    val file = File(uri.path!!)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    
                    val response = apiService.uploadProfilePicture("Bearer $token", body)
                    if (response.isSuccessful) {
                        response.body()?.let { imageUrl ->
                            _uiState.update { it.copy(
                                profilePicture = imageUrl,
                                isLoading = false
                            ) }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("EditProfileViewModel", "Error uploading image: $errorBody")
                        _uiState.update { it.copy(
                            error = "Failed to upload image: ${response.message()}",
                            isLoading = false
                        ) }
                    }
                }
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Exception uploading image", e)
                _uiState.update { it.copy(
                    error = "Failed to connect to server: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
