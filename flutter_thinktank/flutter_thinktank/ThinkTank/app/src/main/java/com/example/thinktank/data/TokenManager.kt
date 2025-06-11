package com.example.thinktank.data

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import android.util.Base64
import org.json.JSONObject

class TokenManager(application: Application) {
    private val prefs: SharedPreferences = application.getSharedPreferences("auth_prefs", Application.MODE_PRIVATE)
    private val TOKEN_KEY = "auth_token"

    fun saveToken(token: String?) {
        if (token == null || token.isEmpty()) {
            Log.e("TokenManager", "Attempted to save null or empty token")
            return
        }
        try {
            // Remove any existing "Bearer " prefix before saving
            val cleanToken = if (token.startsWith("Bearer ")) token.substring(7) else token
            prefs.edit().putString(TOKEN_KEY, cleanToken).apply()
            Log.d("TokenManager", "Token saved successfully: ${cleanToken.take(20)}...")
            
            // Verify token was saved
            val savedToken = getToken()
            if (savedToken == null) {
                Log.e("TokenManager", "Token verification failed - token not found after saving")
            } else {
                Log.d("TokenManager", "Token verification successful")
            }
        } catch (e: Exception) {
            Log.e("TokenManager", "Error saving token: ${e.localizedMessage}")
        }
    }

    fun getToken(): String? {
        return try {
            val token = prefs.getString(TOKEN_KEY, null)
            if (token == null) {
                Log.d("TokenManager", "No token found")
            } else {
                Log.d("TokenManager", "Token retrieved successfully: ${token.take(20)}...")
            }
            token
        } catch (e: Exception) {
            Log.e("TokenManager", "Error retrieving token: ${e.localizedMessage}")
            null
        }
    }

    fun getFormattedToken(): String? {
        val token = getToken() ?: return null
        return "Bearer $token"
    }

    fun getUserRole(): String? {
        val token = getToken() ?: return null
        return try {
            // Split the token into parts
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e("TokenManager", "Invalid token format")
                return null
            }

            // Decode the payload (second part)
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            Log.d("TokenManager", "Decoded JWT payload: $payload")
            
            val jsonObject = JSONObject(payload)
            Log.d("TokenManager", "JWT payload keys: ${jsonObject.keys().asSequence().toList()}")
            
            // Extract the role from the payload
            val role = jsonObject.optString("role")
            Log.d("TokenManager", "Extracted role from token: $role")
            
            if (role.isEmpty()) {
                Log.e("TokenManager", "No role found in token payload")
                // Try alternative role field names
                val altRole = jsonObject.optString("userRole") ?: jsonObject.optString("user_role")
                if (altRole.isNotEmpty()) {
                    Log.d("TokenManager", "Found role in alternative field: $altRole")
                    return altRole
                }
                return null
            }
            
            role
        } catch (e: Exception) {
            Log.e("TokenManager", "Error extracting role from token: ${e.localizedMessage}")
            e.printStackTrace()
            null
        }
    }

    fun clearToken() {
        try {
            prefs.edit().remove(TOKEN_KEY).apply()
            Log.d("TokenManager", "Token cleared")
            
            // Verify token was cleared
            val token = getToken()
            if (token != null) {
                Log.e("TokenManager", "Token verification failed - token still present after clearing")
            } else {
                Log.d("TokenManager", "Token verification successful - token cleared")
            }
        } catch (e: Exception) {
            Log.e("TokenManager", "Error clearing token: ${e.localizedMessage}")
        }
    }
} 
