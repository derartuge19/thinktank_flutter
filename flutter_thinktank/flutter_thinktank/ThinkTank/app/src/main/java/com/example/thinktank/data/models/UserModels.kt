package com.example.thinktank.data.models

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    @SerializedName("role")
    val role: String = "user",
    @SerializedName("profile")
    val profile: Profile? = null
)

data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val email: String
) 