package com.example.thinktank.data.models

import com.google.gson.annotations.SerializedName

data class Profile(
    val id: Int,
    val fullName: String,
    val email: String,
    val profilePicture: String? = null,
    val bio: String? = null,
    @SerializedName("ideas")
    val ideas: List<Idea> = emptyList()
)

data class CreateProfileRequest(
    val fullName: String,
    val email: String,
    val profilePicture: String? = null,
    val bio: String? = null
)

data class UpdateProfileRequest(
    val fullName: String,
    val email: String,
    val profilePicture: String? = null,
    val bio: String? = null
) 