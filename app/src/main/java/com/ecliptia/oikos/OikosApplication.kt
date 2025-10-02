package com.ecliptia.oikos

import android.app.Application
import androidx.work.Configuration // New import
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject // New import
import androidx.hilt.work.HiltWorkerFactory // New import
import com.ecliptia.oikos.workers.RecurringTransactionWorker // New import

@HiltAndroidApp
class OikosApplication : Application(), Configuration.Provider { // Implements Configuration.Provider

    @Inject
    lateinit var workerFactory: HiltWorkerFactory // Inject HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        RecurringTransactionWorker.schedule(this) // Schedule the worker
    }

    override val workManagerConfiguration: Configuration // Changed from fun to val
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
