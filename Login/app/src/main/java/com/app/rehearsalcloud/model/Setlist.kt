package com.app.rehearsalcloud.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Setlist(
    val id: Int? = null,  // Optional for creation
    val name: String,
    val date: String,  // You can change to Date if you're handling date parsing
    val setlistSongs: List<Int>? = emptyList() // List of song IDs that are associated with this setlist
)