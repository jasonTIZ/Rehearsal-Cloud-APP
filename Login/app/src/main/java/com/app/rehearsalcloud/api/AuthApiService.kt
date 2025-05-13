package com.app.rehearsalcloud.api

import com.app.rehearsalcloud.model.User
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    // Endpoint for register
    @POST("api/Auth/register")
    suspend fun registerUser(
        @Body createUserRequest: User
    ): Response<User>

    // Endpoint for log in
    @POST("api/Auth/login")
    suspend fun loginUser(
        @Body loginRequest: User
    ): Response<User>

    // get uses
    @GET("api/Auth/users")
    suspend fun getUsers(): Response<List<User>>

    // Endpoint delete user
    @DELETE("api/Auth/users/{id}")
    suspend fun deleteUser(
        @Path("id") id: Int
    ): Response<Unit>
}








