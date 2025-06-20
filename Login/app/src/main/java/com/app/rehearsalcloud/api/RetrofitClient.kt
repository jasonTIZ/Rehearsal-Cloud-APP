package com.app.rehearsalcloud.api

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.app.rehearsalcloud.common.Constants.BASE_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Setting up the logging interceptor to log HTTP request/response
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttp client with timeouts and logging
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Moshi converter for serializing and deserializing objects
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()) // Add support for Kotlin classes
        .build()

    // Retrofit instance creation
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL) // Ensure BASE_URL is correct
        .client(okHttpClient) // Using the custom OkHttp client
        .addConverterFactory(MoshiConverterFactory.create(moshi)) // Use Moshi converter
        .build()

    // API service creation
    val setlistApiService: SetlistApiService by lazy {
        retrofit.create(SetlistApiService::class.java)
    }

    val authApiService: AuthApiService by lazy{
        retrofit.create(AuthApiService::class.java)
    }
}
