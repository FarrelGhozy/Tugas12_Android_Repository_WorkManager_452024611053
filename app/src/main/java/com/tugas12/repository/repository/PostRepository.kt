package com.tugas12.repository.repository

import com.tugas12.repository.data.local.PostDao
import com.tugas12.repository.data.local.PostEntity
import com.tugas12.repository.data.remote.ApiService
import kotlinx.coroutines.flow.Flow

/**
 * Repository adalah single source of truth untuk data posts.
 *
 * Keuntungan Repository Pattern:
 * 1. ViewModel tidak perlu tahu dari mana data berasal (local DB atau network).
 * 2. Memisahkan logika bisnis (caching strategy) dari UI layer.
 * 3. Memudahkan pengujian (testing) karena bisa mock repository saja.
 * 4. Konsisten — semua operasi data melalui satu pintu.
 */
class PostRepository(
    private val postDao: PostDao,
    private val apiService: ApiService
) {

    /**
     * Mengambil data posts dari database lokal (Room).
     * Karena menggunakan Flow, UI akan otomatis terupdate
     * saat data di database berubah (reactive).
     */
    fun getAllPosts(): Flow<List<PostEntity>> {
        return postDao.getAllPosts()
    }

    /**
     * Menyegarkan data dari API dan menyimpannya ke database lokal.
     *
     * Strategi: Network-first dengan local fallback.
     * - Jika berhasil fetch dari API → hapus data lama → simpan data baru.
     * - Jika gagal (offline/error) → data lama tetap tersedia di Room.
     *
     * @return Result.success() jika berhasil, Result.failure() jika gagal.
     */
    suspend fun refreshPosts(): Result<Unit> {
        return try {
            val posts = apiService.getPosts()
            val entities = posts.map { it.toEntity() }
            postDao.deleteAll()
            postDao.insertAll(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            // Log error — data lokal tetap bisa diakses via Flow
            android.util.Log.e("PostRepository", "Gagal refresh data: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Mengecek apakah database sudah berisi data.
     * Berguna untuk menentukan apakah perlu fetch dari API saat pertama buka.
     */
    suspend fun isCacheEmpty(): Boolean {
        return postDao.getCount() == 0
    }
}
