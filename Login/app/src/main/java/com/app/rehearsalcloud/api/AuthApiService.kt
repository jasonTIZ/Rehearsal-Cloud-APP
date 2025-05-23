package com.app.rehearsalcloud.api

import com.app.rehearsalcloud.model.User
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    // Endpoint for register
    @POST("Auth/register")
    suspend fun registerUser (
        @Body createUserRequest: User
    ): Response<Map<String, Any>>

    // Endpoint for log in
    @POST("Auth/login")
    suspend fun loginUser(
        @Body loginRequest: User
    ): Response<Map<String, Any>>

    // get uses
    @GET("Auth/users")
    suspend fun getUsers(): Response<List<User>>

    // Endpoint delete user
    @DELETE("Auth/users/{id}")
    suspend fun deleteUser(
        @Path("id") id: Int
    ): Response<Unit>
}








