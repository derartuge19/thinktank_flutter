package com.example.thinktank.data.api

import com.example.thinktank.data.models.CreateFeedbackRequest
import com.example.thinktank.data.models.Feedback
import com.example.thinktank.data.models.UpdateFeedbackRequest
import retrofit2.Response
import retrofit2.http.*

interface FeedbackApi {
    @POST("feedback/admin")
    @Headers("Content-Type: application/json")
    suspend fun createFeedback(
        @Header("Authorization") token: String,
        @Body request: CreateFeedbackRequest
    ): Response<Feedback>

    @GET("feedback/{ideaId}")
    @Headers("Content-Type: application/json")
    suspend fun getFeedbackByIdeaId(
        @Header("Authorization") token: String,
        @Path("ideaId") ideaId: Int
    ): Response<List<Feedback>>

    @GET("feedback/admin/all")
    @Headers("Content-Type: application/json")
    suspend fun getAllFeedback(
        @Header("Authorization") token: String
    ): Response<List<Feedback>>

    @GET("feedback/admin/{id}")
    @Headers("Content-Type: application/json")
    suspend fun getFeedbackById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Feedback>

    @PATCH("feedback/admin/{id}")
    @Headers("Content-Type: application/json")
    suspend fun updateFeedback(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateFeedbackRequest
    ): Response<Feedback>

    @DELETE("feedback/admin/{id}")
    @Headers("Content-Type: application/json")
    suspend fun deleteFeedback(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @GET("feedback/idea/{ideaId}")
    @Headers("Content-Type: application/json")
    suspend fun getFeedbackForIdea(
        @Header("Authorization") token: String,
        @Path("ideaId") ideaId: Int
    ): Response<List<Feedback>>
} 