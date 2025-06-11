package com.example.thinktank.utils

import android.util.Base64
import android.util.Log
import org.json.JSONObject

object JwtUtils {
    fun getUserIdFromToken(token: String): Int? {
        return try {
            // Remove "Bearer " prefix if present
            val actualToken = if (token.startsWith("Bearer ")) {
                token.substring(7)
            } else {
                token
            }

            // Split the token into parts
            val parts = actualToken.split(".")
            if (parts.size != 3) {
                Log.e("JwtUtils", "Invalid token format")
                return null
            }

            // Decode the payload (second part)
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val jsonObject = JSONObject(payload)
            
            // Get the user ID from the payload
            val userId = jsonObject.optInt("sub")
            if (userId == 0) {
                Log.e("JwtUtils", "No user ID found in token")
                null
            } else {
                userId
            }
        } catch (e: Exception) {
            Log.e("JwtUtils", "Error decoding token", e)
            null
        }
    }
} 