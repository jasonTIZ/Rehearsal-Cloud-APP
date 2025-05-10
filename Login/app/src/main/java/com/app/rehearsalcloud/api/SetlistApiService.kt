package com.app.rehearsalcloud.api

import com.app.rehearsalcloud.model.Setlist
import retrofit2.Response
import retrofit2.http.*

interface SetlistApiService {

    // Fetch setlists
    @GET("Setlist")
    suspend fun getSetlists(): Response<List<Setlist>>

    // Create a setlist
    @POST("Setlist")
    suspend fun createSetlist(@Body createSetlistRequest: Setlist): Response<Setlist>

    @PUT("Setlist/{id}")
    suspend fun updateSetlist(@Path("id") id: Int, @Body setlist: Setlist): Response<Setlist>

    @GET("Setlist/{id}")
    suspend fun getSetlistById(@Path("id") id: Int): Response<Setlist>

    // Delete a setlist
    @DELETE("Setlist/{id}")
    suspend fun deleteSetlist(@Path("id") id: Int): Response<Unit>
}
