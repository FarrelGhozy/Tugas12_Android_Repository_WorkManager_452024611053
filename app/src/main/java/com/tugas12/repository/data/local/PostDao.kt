package com.tugas12.repository.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk tabel posts.
 * Menyediakan operasi baca (query) dan tulis (insert/delete).
 */
@Dao
interface PostDao {

    /**
     * Mengambil seluruh data posts dari database lokal.
     * Menggunakan Flow agar UI otomatis ter-update saat data berubah.
     */
    @Query("SELECT * FROM posts ORDER BY id ASC")
    fun getAllPosts(): Flow<List<PostEntity>>

    /**
     * Menyisipkan daftar posts ke database.
     * Menggunakan REPLACE jika terjadi konflik id.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)

    /**
     * Menghapus seluruh data posts dari database lokal.
     * Dipanggil sebelum insert ulang data terbaru.
     */
    @Query("DELETE FROM posts")
    suspend fun deleteAll()

    /**
     * Mengambil jumlah data posts di database.
     * Berguna untuk mengecek apakah cache lokal sudah terisi.
     */
    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getCount(): Int
}
