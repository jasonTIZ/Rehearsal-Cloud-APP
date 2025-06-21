package com.app.rehearsalcloud.model.setlist

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)
// Setlist Entity
@Entity()
data class Setlist(
    @PrimaryKey
    val id: Int,
    val name: String,
    val date: Long
)