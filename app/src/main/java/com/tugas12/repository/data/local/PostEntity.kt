package com.tugas12.repository.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity untuk menyimpan data posts dari API ke database lokal.
 * Setiap post memiliki id, userId, title, dan body.
 */
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)
