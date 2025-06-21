package com.app.rehearsalcloud.api

import com.app.rehearsalcloud.dtos.SetlistDto
import com.app.rehearsalcloud.dtos.SetlistWithoutSongsDto
import com.app.rehearsalcloud.dtos.UpdateSetlistRequestDto
import com.app.rehearsalcloud.model.setlist.Setlist
import com.app.rehearsalcloud.model.setlist.SetlistWithSongs
import retrofit2.Response
import retrofit2.http.*

interface SetlistApiService {

    // Create a setlist
    @POST("Setlist")
    suspend fun createSetlist(@Body createDto: UpdateSetlistRequestDto): SetlistDto

    // Delete a setlist
    @DELETE("Setlist/{id}")
    suspend fun deleteSetlist(@Path("id") id: Int): Response<Unit>

    @GET("Setlist")
    suspend fun getSetlistsWithoutSongs(): List<SetlistWithoutSongsDto>

    @PUT("Setlist/{id}")
    suspend fun updateSetlist(
        @Path("id") id: Int,
        @Body updateSetlistDto: UpdateSetlistRequestDto
    ): SetlistDto // Assuming SetlistDto includes songs and audio files
}
