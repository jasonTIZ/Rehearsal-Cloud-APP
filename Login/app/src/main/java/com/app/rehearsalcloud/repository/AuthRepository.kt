package com.app.rehearsalcloud.repository

import com.app.rehearsalcloud.api.RetrofitClient
import com.app.rehearsalcloud.model.User


class AuthRepository {

    private val apiService = RetrofitClient.authApiService


    suspend fun registerUser(User: User): User {
        val response = apiService.registerUser(User)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Failed to create setlist")
        }
    }


    suspend fun loginUser(User: User): User {
        val response = apiService.loginUser(User)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Failed to create setlist")
        }
    }


    suspend fun getUsers(): List<User> {
        val response = apiService.getUsers()
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception("Failed to load setlists")
        }
    }


    suspend fun deleteUser(id: Int) {
        val response = apiService.deleteUser(id)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete setlist")
        }
    }
}

