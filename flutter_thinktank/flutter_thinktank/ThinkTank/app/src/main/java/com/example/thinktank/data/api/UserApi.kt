package com.example.thinktank.data.api

import com.example.thinktank.data.models.Profile
import com.example.thinktank.data.models.UpdateUserRequest
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("users/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<Profile>

    @PATCH("users/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateUserRequest
    ): Response<Profile>
} 
