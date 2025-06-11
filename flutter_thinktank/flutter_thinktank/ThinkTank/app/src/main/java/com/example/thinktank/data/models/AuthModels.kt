package com.example.thinktank.data.models

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String = "user"  // Default role is "user"
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String
)

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String? = null,
    val id: Int? = null,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: String? = null
) 