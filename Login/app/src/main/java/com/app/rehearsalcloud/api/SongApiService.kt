package com.app.rehearsalcloud.api

import com.app.rehearsalcloud.dtos.SongDto
import com.app.rehearsalcloud.model.setlist.Setlist
import okhttp3.MultipartBody
import okio.ByteString
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface SongApiService {
    @GET("Song")
    suspend fun getSongs(): List<SongDto>

    @GET("Song/{id}")
    suspend fun getSongById(@Path("id") id: Int): SongDto

    @GET("Song/{id}/audio/{audioId}")
    suspend fun downloadAudioFile(@Path("id") songId: Int, @Path("audioId") audioId: Int): Response<ByteString>

    @Multipart
    @POST("Song/create-song")
    suspend fun createSong(
        @Part("SongName") songName: String,
        @Part("Artist") artist: String,
        @Part("BPM") bpm: Int,
        @Part("Tone") tone: String,
        @Part coverImage: MultipartBody.Part,
        @Part zipFile: MultipartBody.Part
    ): SongDto

    @Multipart
    @PUT("Song/{id}")
    suspend fun updateSong(
        @Path("id") id: Int,
        @Part("SongName") songName: String,
        @Part("Artist") artist: String,
        @Part("BPM") bpm: Int,
        @Part("Tone") tone: String,
        @Part coverImage: MultipartBody.Part?,
        @Part zipFile: MultipartBody.Part?
    ): SongDto

    @DELETE("Song/{id}")
    suspend fun deleteSong(@Path("id") id: Int)
}