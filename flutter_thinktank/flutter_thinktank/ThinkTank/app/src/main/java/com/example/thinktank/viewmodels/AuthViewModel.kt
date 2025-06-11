package com.example.thinktank.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinktank.data.api.ApiClient
import com.example.thinktank.data.models.AuthResponse
import com.example.thinktank.data.models.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class AuthViewModel : ViewModel() {
    private val _registrationError = MutableStateFlow<String?>(null)
    val registrationError: StateFlow<String?> = _registrationError

    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering

    fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: String = "user",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _registrationError.value = null
                _isRegistering.value = true

                val registerRequest = RegisterRequest(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName
                )

                val response = ApiClient.authApi.registerUser(registerRequest)

                launch(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        if (authResponse != null) {
                            Log.d("Registration", "Success: $authResponse")
                            onSuccess()
                        } else {
                            _registrationError.value = "Empty response from server"
                            Log.e("Registration", "Empty response: ${response.raw()}")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        _registrationError.value = errorBody ?: "Unknown error"

                        Log.e("Registration", "Failed with code: ${response.code()}")
                        Log.e("Registration", "Message: ${response.message()}")
                        Log.e("Registration", "Error body: $errorBody")
                        Log.e("Registration", "Raw: ${response.raw()}")
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    _registrationError.value = "Registration failed: ${e.localizedMessage}"
                    Log.e("Registration", "Exception: ${e.localizedMessage}")
                }
            } finally {
                launch(Dispatchers.Main) {
                    _isRegistering.value = false
                }
            }
        }
    }
} 