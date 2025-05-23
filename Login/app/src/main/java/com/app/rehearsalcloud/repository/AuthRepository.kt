package com.app.rehearsalcloud.repository

import android.util.Log
import com.app.rehearsalcloud.api.RetrofitClient
import com.app.rehearsalcloud.model.User
import org.json.JSONObject


class AuthRepository {

    private val apiService = RetrofitClient.authApiService


    suspend fun registerUser(user: User): User {
        val response = apiService.registerUser(user)

        if (response.isSuccessful) {
            val responseBody = response.body()
            Log.d("API_RESPONSE", "Raw registration response: $responseBody")


            if (responseBody != null && responseBody["message"] != null) {
                return user.copy(id = 0) // ID temporal
            }
            throw Exception("Unexpected response: $responseBody")
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e("API_ERROR", "Registration failed: $errorBody")
            throw Exception(errorBody ?: "Registration failed with status ${response.code()}")
        }
    }


    suspend fun loginUser(user: User): Boolean {
        val response = apiService.loginUser(user)

        if (response.isSuccessful) {
            val responseBody = response.body()
            val message = responseBody?.get("message") as? String

            return when {
                message?.contains("exitoso") == true -> true
                message?.contains("incorrecta") == true -> throw Exception("Credenciales incorrectas, intente de nuevo")
                message != null -> throw Exception(message)
                else -> throw Exception("Respuesta inválida del servidor")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = try {
                val errorJson = JSONObject(errorBody ?: "")
                when (errorJson.getString("message")) {
                    "Usuario no encontrado" -> "Credenciales incorrectas, intente de nuevo"
                    "Contraseña incorrecta" -> "Credenciales incorrectas, intente de nuevo"
                    else -> errorJson.getString("message")
                }
            } catch (e: Exception) {
                "Error en el inicio de sesión"
            }
            throw Exception(errorMessage)
        }
    }


    suspend fun getUsers(): List<User> {
        val response = apiService.getUsers()
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception("Failed to load user")
        }
    }


    suspend fun deleteUser(id: Int) {
        val response = apiService.deleteUser(id)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete user")
        }
    }
}

