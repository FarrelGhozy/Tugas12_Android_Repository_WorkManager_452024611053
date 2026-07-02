package com.tugas12.repository

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.tugas12.repository.worker.RefreshDataWorker

/**
 * Application class untuk inisialisasi global.
 * WorkManager di-set supaya bisa logging dan debugging.
 */
class MyApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}
