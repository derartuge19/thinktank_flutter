package com.example.thinktank.data.api

import com.example.thinktank.data.models.Idea
import com.example.thinktank.data.models.User
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("ideas/user")
    fun getUserIdeas(@Header("Authorization") token: String): Call<List<Idea>>

    @GET("ideas/public")
    fun getPublicIdeas(): Call<List<Idea>>

    @GET("ideas/{id}")
    fun getIdeaById(@Path("id") id: String): Call<Idea>

    @POST("ideas")
    fun createIdea(
        @Header("Authorization") token: String,
        @Body idea: Idea
    ): Call<Idea>

    @DELETE("ideas/{id}")
    fun deleteIdea(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Call<Unit>

    @PATCH("ideas/{id}")
    fun updateIdea(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body idea: Idea
    ): Call<Idea>

    @GET("users/{id}")
    fun getUserProfile(@Header("Authorization") token: String, @Path("id") id: String): Call<User>
} 
