package com.example.thinktank.Pages.LoginPage

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinktank.data.TokenManager
import com.example.thinktank.data.api.ApiClient
import com.example.thinktank.data.models.AuthResponse
import com.example.thinktank.data.models.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import com.example.thinktank.data.models.IdeaStatus

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun updateEmail(email: String) {
        _email.value = email
    }

    fun updatePassword(password: String) {
        _password.value = password
    }

    fun login(onSuccess: () -> Unit) {
        if (_email.value.isEmpty() || _password.value.isEmpty()) {
            _loginError.value = "Please enter both email and password"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _loginError.value = null

                val loginRequest = LoginRequest(
                    email = _email.value,
                    password = _password.value
                )

                Log.d("Login", "Attempting login with email: ${_email.value}")
                val response = ApiClient.authApi.loginUser(loginRequest)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.accessToken != null) {
                        Log.d("Login", "Login successful")
                        tokenManager.saveToken(authResponse.accessToken)
                        Log.d("Login", "Token saved after login")
                        
                        // Verify token was saved
                        val savedToken = tokenManager.getFormattedToken()
                        if (savedToken == null) {
                            Log.e("Login", "Token was not saved properly")
                            _loginError.value = "Login failed: Could not save authentication token"
                            return@launch
                        }
                        
                        Log.d("Login", "Token verified and formatted: ${savedToken.take(20)}...")
                        onSuccess()
                    } else {
                        Log.e("Login", "No token in response")
                        _loginError.value = "Login failed: No token received"
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("Login", "Login failed. Code: ${response.code()}, Body: $errorBody")
                    
                    when (response.code()) {
                        401 -> _loginError.value = "Invalid email or password"
                        400 -> _loginError.value = "Invalid login data"
                        else -> _loginError.value = "Login failed: ${errorBody ?: "Unknown error"}"
                    }
                }
            } catch (e: SocketTimeoutException) {
                Log.e("Login", "Timeout error: ${e.message}")
                _loginError.value = "Connection timed out. Please try again"
            } catch (e: UnknownHostException) {
                Log.e("Login", "Unknown host error: ${e.message}")
                _loginError.value = "Cannot reach server. Please check your connection"
            } catch (e: Exception) {
                Log.e("Login", "Login error: ${e.message}", e)
                _loginError.value = "Login failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun stringToIdeaStatus(status: String): IdeaStatus {
        return IdeaStatus.fromString(status)
    }
}
