package com.tugas12.repository.data.remote

import retrofit2.http.GET

/**
 * Retrofit service interface untuk API JSONPlaceholder.
 */
interface ApiService {

    /**
     * Mendapatkan seluruh posts dari API.
     * @return List<PostResponse> daftar posts dari server.
     */
    @GET("posts")
    suspend fun getPosts(): List<PostResponse>
}
