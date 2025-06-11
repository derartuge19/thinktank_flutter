package com.example.thinktank.data.api

import com.example.thinktank.data.models.AuthResponse
import com.example.thinktank.data.models.LoginRequest
import com.example.thinktank.data.models.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>
} 
