package com.pranshulgg.watchmaster.feature.setting

import android.content.Context
import androidx.room.withTransaction
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.pranshulgg.watchmaster.core.network.TmdbApi
import com.pranshulgg.watchmaster.core.ui.localization.AppLanguage
import com.pranshulgg.watchmaster.core.utils.PreferencesHelper
import com.pranshulgg.watchmaster.data.local.WatchMasterDatabase
import java.time.Instant
import kotlinx.coroutines.flow.first

object MetadataSync {
    const val LAST_SYNC_KEY = "metadata_last_sync"
    const val SYNCED_LANGUAGE_KEY = "metadata_synced_language"
    private const val WORK_NAME = "visto_metadata_language_sync"

    fun enqueue(context: Context) {
        val request = OneTimeWorkRequestBuilder<MetadataSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun ensureCurrentLanguage(context: Context) {
        if (PreferencesHelper.getString(SYNCED_LANGUAGE_KEY) != AppLanguage.code(context)) {
            enqueue(context)
        }
    }
}

class MetadataSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        PreferencesHelper.init(applicationContext)
        AppLanguage.initialize(applicationContext)
        val db = WatchMasterDatabase.getInstance(applicationContext)
        val api = TmdbApi.create()
        val watchlist = db.watchlistDao().getAll().first()
        val seasons = db.seasonDao().getAllSeasons().first()
        var failures = 0

        watchlist.forEach { item ->
            val updated = runCatching {
                if (item.mediaType == "movie") {
                    val movie = api.getWholeMovieData(item.id).body()
                        ?: error("Movie metadata unavailable")
                    db.withTransaction {
                        db.watchlistDao().updateLocalizedMetadata(
                            id = item.id,
                            title = movie.title,
                            overview = movie.overview,
                            posterPath = movie.poster_path,
                            backdropPath = movie.backdrop_path,
                        )
                        db.movieBundleDao().deleteById(item.id)
                    }
                } else {
                    val show = api.getWholeTvData(item.id).body()
                        ?: error("TV metadata unavailable")
                    val remoteSeasons = api.getTvSeasons(item.id).body()?.seasons.orEmpty()
                    db.withTransaction {
                        db.watchlistDao().updateLocalizedMetadata(
                            id = item.id,
                            title = show.name,
                            overview = show.overview,
                            posterPath = show.poster_path,
                            backdropPath = show.backdrop_path,
                        )
                        remoteSeasons.forEach { season ->
                            db.seasonDao().updateLocalizedMetadata(
                                showId = item.id,
                                seasonNumber = season.season_number,
                                name = season.name,
                                episodeCount = season.episode_count,
                                airDate = season.air_date,
                                posterPath = season.poster_path,
                            )
                        }
                        db.tvBundleDao().deleteById(item.id)
                    }
                }
            }.isSuccess
            if (!updated) failures++
        }

        seasons.filter { it.seasonNumber > 0 }.forEach { season ->
                if (!db.tvEpisodeDao().hasEpisodes(season.seasonId)) return@forEach
                val updated = runCatching {
                    val episodes = api.getTvSeasonEpisodes(
                        season.showId,
                        season.seasonNumber,
                    ).body()?.episodes ?: error("Episode metadata unavailable")
                    db.withTransaction {
                        episodes.forEach { episode ->
                            db.tvEpisodeDao().updateLocalizedMetadata(
                                seasonId = season.seasonId,
                                episodeNumber = episode.episode_number,
                                name = episode.name,
                                overview = episode.overview,
                                airDate = episode.air_date,
                                stillPath = episode.still_path,
                                runtime = episode.runtime,
                            )
                        }
                    }
                }.isSuccess
                if (!updated) failures++
        }

        if (failures == 0) {
            PreferencesHelper.setString(MetadataSync.LAST_SYNC_KEY, Instant.now().toString())
            PreferencesHelper.setString(
                MetadataSync.SYNCED_LANGUAGE_KEY,
                AppLanguage.code(applicationContext),
            )
            return Result.success()
        }
        return if (runAttemptCount < 2) Result.retry() else Result.failure()
    }
}
