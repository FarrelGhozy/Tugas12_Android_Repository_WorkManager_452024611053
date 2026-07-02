package com.tugas12.repository.data.remote

import com.tugas12.repository.data.local.PostEntity

/**
 * Model data dari API JSONPlaceholder.
 * Field-nya sesuai dengan response JSON dari endpoint /posts.
 */
data class PostResponse(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
) {
    /**
     * Konversi dari response API ke entity database Room.
     */
    fun toEntity(): PostEntity {
        return PostEntity(
            id = id,
            userId = userId,
            title = title,
            body = body
        )
    }
}
