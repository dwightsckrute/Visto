package com.pranshulgg.watchmaster.feature.setting

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.pranshulgg.watchmaster.core.utils.PreferencesHelper
import java.time.Instant
import java.util.concurrent.TimeUnit

enum class BackupFrequency(val days: Long) {
    OFF(0),
    DAILY(1),
    WEEKLY(7),
}

object AutomaticBackup {
    const val URI_KEY = "automatic_backup_uri"
    const val FREQUENCY_KEY = "automatic_backup_frequency"
    const val LAST_BACKUP_KEY = "automatic_backup_last_success"
    private const val WORK_NAME = "visto_automatic_backup"

    fun currentFrequency(): BackupFrequency = PreferencesHelper
        .getString(FREQUENCY_KEY)
        ?.let { runCatching { BackupFrequency.valueOf(it) }.getOrNull() }
        ?: BackupFrequency.OFF

    fun configure(context: Context, uri: Uri, frequency: BackupFrequency) {
        PreferencesHelper.setString(URI_KEY, uri.toString())
        setFrequency(context, frequency)
    }

    fun setFrequency(context: Context, frequency: BackupFrequency) {
        PreferencesHelper.setString(FREQUENCY_KEY, frequency.name)
        val workManager = WorkManager.getInstance(context)
        if (frequency == BackupFrequency.OFF) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        if (PreferencesHelper.getString(URI_KEY).isNullOrBlank()) return

        val request = PeriodicWorkRequestBuilder<AutomaticBackupWorker>(
            frequency.days,
            TimeUnit.DAYS,
        ).build()
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun lastBackup(): Instant? = PreferencesHelper
        .getString(LAST_BACKUP_KEY)
        ?.let { runCatching { Instant.parse(it) }.getOrNull() }
}

class AutomaticBackupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        PreferencesHelper.init(applicationContext)
        val uri = PreferencesHelper.getString(AutomaticBackup.URI_KEY)
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::parse)
            ?: return Result.failure()

        return if (exportData(applicationContext, uri, true, true)) {
            PreferencesHelper.setString(
                AutomaticBackup.LAST_BACKUP_KEY,
                Instant.now().toString(),
            )
            Result.success()
        } else {
            Result.retry()
        }
    }
}
