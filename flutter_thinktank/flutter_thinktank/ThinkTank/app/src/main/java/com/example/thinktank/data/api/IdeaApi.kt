package com.example.thinktank.data.api

import com.example.thinktank.data.models.Idea
import com.example.thinktank.data.models.IdeaSubmissionRequest
import com.example.thinktank.data.models.IdeaUpdateRequest
import retrofit2.Response
import retrofit2.http.*

interface IdeaApi {
    @GET("ideas/user")
    @Headers("Content-Type: application/json")
    suspend fun getUserIdeas(
        @Header("Authorization") token: String
    ): Response<List<Idea>>

    @GET("ideas/admin/all")
    @Headers("Content-Type: application/json")
    suspend fun getAllIdeas(
        @Header("Authorization") token: String
    ): Response<List<Idea>>

    @GET("ideas/public")
    @Headers("Content-Type: application/json")
    suspend fun getPublicIdeas(): Response<List<Idea>>

    @POST("ideas")
    @Headers("Content-Type: application/json")
    suspend fun submitIdea(
        @Header("Authorization") token: String,
        @Body request: IdeaSubmissionRequest
    ): Response<Unit>

    @PATCH("ideas/{id}")
    @Headers("Content-Type: application/json")
    suspend fun updateIdea(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: IdeaUpdateRequest
    ): Response<Unit>

    @DELETE("ideas/{id}")
    @Headers("Content-Type: application/json")
    suspend fun deleteIdea(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @PATCH("ideas/{id}/status")
    suspend fun updateIdeaStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body status: String
    ): Response<Idea>
} 
