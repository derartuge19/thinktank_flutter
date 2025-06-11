package com.example.thinktank.data.remote

import com.example.thinktank.data.models.Idea
import com.example.thinktank.data.models.User
import com.example.thinktank.Pages.Profile.Status
import com.example.thinktank.data.models.Profile
import com.example.thinktank.data.models.CreateProfileRequest
import com.example.thinktank.data.models.UpdateProfileRequest
import com.example.thinktank.data.models.UpdateUserRequest
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody

interface ApiService {
    @GET("users/{id}")
    suspend fun getUserProfile(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<User>

    @GET("ideas/user")
    suspend fun getUserIdeas(@Header("Authorization") token: String): Response<List<Idea>>

    @DELETE("ideas/{ideaId}")
    suspend fun deleteIdea(
        @Header("Authorization") token: String,
        @Path("ideaId") ideaId: String
    ): Response<Unit>

    @PUT("users/status")
    suspend fun updateUserStatus(
        @Header("Authorization") token: String,
        @Body status: Status
    ): Response<Unit>

    @GET("profiles/{id}")
    suspend fun getProfile(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Profile>

    @POST("profiles")
    suspend fun createProfile(
        @Header("Authorization") token: String,
        @Body createProfileRequest: CreateProfileRequest
    ): Response<Profile>

    @PATCH("profiles/{id}")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body updateProfileRequest: UpdateProfileRequest
    ): Response<Profile>

    @DELETE("profiles/{id}")
    suspend fun deleteProfile(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @Multipart
    @POST("profiles/upload")
    suspend fun uploadProfilePicture(
        @Header("Authorization") token: String,
        @Part("file") file: MultipartBody.Part
    ): Response<String>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body updateUserRequest: UpdateUserRequest
    ): Response<User>
} 