package com.tugas12.repository.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tugas12.repository.data.local.AppDatabase
import com.tugas12.repository.data.remote.RetrofitClient
import com.tugas12.repository.repository.PostRepository

/**
 * CoroutineWorker untuk refresh data secara periodik di background.
 *
 * Menggunakan CoroutineWorker (bukan Worker biasa) agar operasi
 * database dan network bisa berjalan secara asynchronous tanpa
 * memblokir thread utama.
 *
 * WorkManager menjamin bahwa tugas ini tetap berjalan meskipun:
 * - Aplikasi ditutup
 * - Perangkat di-restart
 * - Terjadi perubahan konfigurasi (rotasi, dll)
 */
class RefreshDataWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "RefreshDataWorker"
    }

    /**
     * doWork() dijalankan secara asynchronous di background thread.
     * 
     * @return Result.success() jika berhasil
     *         Result.retry() jika gagal dan ingin diulang (max 3x)
     *         Result.failure() jika gagal total
     */
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, ">>> RefreshDataWorker dimulai...")

            // Inisialisasi dependencies
            val database = AppDatabase.getInstance(applicationContext)
            val apiService = RetrofitClient.apiService
            val repository = PostRepository(database.postDao(), apiService)

            // Eksekusi refresh data
            val result = repository.refreshPosts()

            if (result.isSuccess) {
                Log.d(TAG, ">>> RefreshDataWorker: SUCCESS — Data berhasil di-refresh dari API ke lokal DB")
                Result.success()
            } else {
                Log.e(TAG, ">>> RefreshDataWorker: GAGAL — ${result.exceptionOrNull()?.message}")
                if (runAttemptCount < 3) {
                    Log.d(TAG, ">>> RefreshDataWorker: Percobaan ke-$runAttemptCount, akan di-retry...")
                    Result.retry()
                } else {
                    Log.e(TAG, ">>> RefreshDataWorker: Gagal setelah 3 percobaan")
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, ">>> RefreshDataWorker: CRASH — ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
