package com.tugas12.repository.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tugas12.repository.data.local.AppDatabase
import com.tugas12.repository.data.local.PostEntity
import com.tugas12.repository.data.remote.RetrofitClient
import com.tugas12.repository.repository.PostRepository
import com.tugas12.repository.worker.RefreshDataWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel untuk MainActivity.
 *
 * Sesuai Repository Pattern, ViewModel TIDAK boleh menyentuh
 * DAO atau Retrofit secara langsung. Semua operasi data
 * dilakukan melalui PostRepository.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository

    /** Data posts yang siap ditampilkan di UI (dari cache lokal Room) */
    private val _posts = MutableStateFlow<List<PostEntity>>(emptyList())
    val posts: StateFlow<List<PostEntity>> = _posts.asStateFlow()

    /** Status loading/refreshing */
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /** Status pesan untuk user */
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        // Inisialisasi Repository — ViewModel hanya butuh ini!
        val database = AppDatabase.getInstance(application)
        val apiService = RetrofitClient.apiService
        repository = PostRepository(database.postDao(), apiService)

        // Observe data dari Room secara realtime
        viewModelScope.launch {
            repository.getAllPosts().collect { postList ->
                _posts.value = postList
            }
        }

        // Cek apakah cache kosong, jika iya auto-refresh
        viewModelScope.launch {
            if (repository.isCacheEmpty()) {
                refreshData()
            }
        }

        // Jadwalkan WorkManager untuk periodic refresh
        schedulePeriodicRefresh()

        // Trigger immediate one-time worker untuk bukti logcat
        triggerImmediateWorker()
    }

    /**
     * One-time worker tanpa constraints untuk keperluan demonstrasi logcat.
     */
    private fun triggerImmediateWorker() {
        val immediateRequest = androidx.work.OneTimeWorkRequestBuilder<com.tugas12.repository.worker.RefreshDataWorker>()
            .addTag("immediate_refresh")
            .build()

        androidx.work.WorkManager.getInstance(getApplication())
            .enqueue(immediateRequest)
    }

    /**
     * Refresh data dari API → disimpan ke Room.
     * ViewModel cuma panggil repository.refreshPosts() — 
     * gak perlu tahu detail DAO atau Retrofit!
     */
    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = repository.refreshPosts()
            if (result.isSuccess) {
                _snackbarMessage.value = "Data berhasil di-refresh! (${posts.value.size} posts)"
            } else {
                _snackbarMessage.value = "Gagal refresh: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
            }
            _isRefreshing.value = false
        }
    }

    /**
     * Menjadwalkan periodic WorkManager untuk refresh data otomatis.
     * 
     * Constraints:
     * - NetworkType.UNMETERED → Hanya jalan saat pakai WiFi (gak boros kuota)
     * - RequiresCharging → Hanya jalan saat perangkat di-charge (irit baterai)
     * 
     * PeriodicWorkRequest: 15 menit (minimal interval Android)
     */
    private fun schedulePeriodicRefresh() {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val refreshRequest = androidx.work.PeriodicWorkRequestBuilder<RefreshDataWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("periodic_refresh")
            .build()

        androidx.work.WorkManager.getInstance(getApplication())
            .enqueueUniquePeriodicWork(
                "refresh_data_worker",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                refreshRequest
            )
    }

    /** Membersihkan pesan snackbar setelah ditampilkan */
    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
