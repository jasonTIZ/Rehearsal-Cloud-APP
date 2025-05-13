package com.app.rehearsalcloud.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: Int? = null,
    val username: String,
    val email: String,
    val password: String
)
