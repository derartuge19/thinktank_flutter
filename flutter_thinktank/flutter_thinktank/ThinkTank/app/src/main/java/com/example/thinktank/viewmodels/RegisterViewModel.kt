package com.example.thinktank.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinktank.data.TokenManager
import com.example.thinktank.data.api.ApiClient
import com.example.thinktank.data.models.AuthResponse
import com.example.thinktank.data.models.LoginRequest
import com.example.thinktank.data.models.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val _firstName = MutableStateFlow("")
    val firstName: StateFlow<String> = _firstName

    private val _lastName = MutableStateFlow("")
    val lastName: StateFlow<String> = _lastName

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _registrationError = MutableStateFlow<String?>(null)
    val registrationError: StateFlow<String?> = _registrationError

    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess

    fun updateFirstName(firstName: String) {
        _firstName.value = firstName
    }

    fun updateLastName(lastName: String) {
        _lastName.value = lastName
    }

    fun updateEmail(email: String) {
        _email.value = email
    }

    fun updatePassword(password: String) {
        _password.value = password
    }

    fun register() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Validate input
                if (_firstName.value.isBlank() || _lastName.value.isBlank() || 
                    _email.value.isBlank() || _password.value.isBlank()) {
                    _registrationError.value = "Please fill in all fields"
                    return@launch
                }

                _isRegistering.value = true
                _registrationError.value = null

                val registerRequest = RegisterRequest(
                    email = _email.value,
                    password = _password.value,
                    firstName = _firstName.value,
                    lastName = _lastName.value,
                    role = "user"
                )

                Log.d("Registration", "Attempting registration with email: ${_email.value}")

                val response = ApiClient.authApi.registerUser(registerRequest)

                launch(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        if (authResponse != null) {
                            Log.d("Registration", "Registration successful")
                            // Save the token if it's included in the registration response
                            authResponse.accessToken?.let { token ->
                                // Ensure token is properly formatted
                                val formattedToken = if (!token.startsWith("Bearer ")) "Bearer $token" else token
                                tokenManager.saveToken(formattedToken)
                                Log.d("Registration", "Token saved after registration: ${formattedToken.take(20)}...")
                            }
                            _registrationSuccess.value = true
                            // Since registration was successful, we can now try to login
                            loginAfterRegistration()
                        } else {
                            Log.e("Registration", "No response body")
                            _registrationError.value = "Registration successful but no response received"
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("Registration", "Registration failed. Code: ${response.code()}, Body: $errorBody")
                        
                        when (response.code()) {
                            409 -> _registrationError.value = "Email already in use"
                            400 -> _registrationError.value = "Invalid registration data"
                            else -> _registrationError.value = "Registration failed: ${errorBody ?: "Unknown error"}"
                        }
                    }
                }
            } catch (e: ConnectException) {
                Log.e("Registration", "Connection error: ${e.message}")
                _registrationError.value = "Cannot connect to server. Please check your connection"
            } catch (e: SocketTimeoutException) {
                Log.e("Registration", "Timeout error: ${e.message}")
                _registrationError.value = "Connection timed out. Please try again"
            } catch (e: UnknownHostException) {
                Log.e("Registration", "Unknown host error: ${e.message}")
                _registrationError.value = "Cannot reach server. Please check your connection"
            } catch (e: Exception) {
                Log.e("Registration", "Registration error: ${e.message}", e)
                _registrationError.value = "Registration failed: ${e.message}"
            } finally {
                _isRegistering.value = false
            }
        }
    }

    private suspend fun loginAfterRegistration() {
        try {
            val loginRequest = LoginRequest(
                email = _email.value,
                password = _password.value
            )
            
            val response = ApiClient.authApi.loginUser(loginRequest)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.accessToken != null) {
                    Log.d("Registration", "Login after registration successful")
                    // Ensure token is properly formatted
                    val formattedToken = if (!authResponse.accessToken.startsWith("Bearer ")) "Bearer ${authResponse.accessToken}" else authResponse.accessToken
                    tokenManager.saveToken(formattedToken)
                    Log.d("Registration", "Token saved after login: ${formattedToken.take(20)}...")
                    _registrationSuccess.value = true
                } else {
                    Log.e("Registration", "No token in login response")
                    _registrationError.value = "Registration successful but login failed"
                }
            } else {
                Log.e("Registration", "Login after registration failed: ${response.code()}")
                _registrationError.value = "Registration successful but login failed"
            }
        } catch (e: Exception) {
            Log.e("Registration", "Login after registration error: ${e.message}")
            _registrationError.value = "Registration successful but login failed"
        }
    }
} 