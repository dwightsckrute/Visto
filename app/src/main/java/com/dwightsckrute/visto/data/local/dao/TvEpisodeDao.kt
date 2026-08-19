package com.dwightsckrute.visto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dwightsckrute.visto.data.local.entity.TvEpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TvEpisodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<TvEpisodeEntity>)


    // La fecha viaja con el estado en la misma sentencia: si fueran dos, un fallo entre medias
    // dejaría un episodio visto sin fecha, invisible para el diario y sin forma de notarlo.
    @Query("UPDATE tv_episodes SET isWatched = :watched, watchedDate = :watchedAt WHERE epId = :epId")
    suspend fun updateEpisodeStatus(epId: Long, watched: Boolean, watchedAt: Long?)

    // COALESCE: terminar una temporada no debe reescribir la fecha de los episodios que ya
    // tenían la suya. Lo que viste el martes se vio el martes.
    @Query("UPDATE tv_episodes SET isWatched = 1, watchedDate = COALESCE(watchedDate, :watchedAt) WHERE seasonId = :seasonId")
    suspend fun markAllEpWatched(seasonId: Long, watchedAt: Long)

    @Query("UPDATE tv_episodes SET isWatched = 0, watchedDate = NULL WHERE seasonId = :seasonId")
    suspend fun markAllEpUnWatched(seasonId: Long)

    @Query("SELECT * FROM tv_episodes WHERE seasonId = :seasonId ORDER BY episode_number")
    fun getEpisodesForSeason(seasonId: Long): Flow<List<TvEpisodeEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM tv_episodes WHERE seasonId = :seasonId)")
    suspend fun hasEpisodes(seasonId: Long): Boolean

    @Query("SELECT * FROM tv_episodes")
    suspend fun getAllEpisodes(): List<TvEpisodeEntity>

    /** Los episodios con fecha, que son los que el diario puede situar en un día. */
    @Query("SELECT * FROM tv_episodes WHERE watchedDate IS NOT NULL")
    fun getWatchedEpisodes(): Flow<List<TvEpisodeEntity>>

    /** Mover un episodio a otro día, para corregir una fecha o registrar algo visto ayer. */
    @Query("UPDATE tv_episodes SET isWatched = 1, watchedDate = :watchedAt WHERE epId = :epId")
    suspend fun updateEpisodeWatchedDate(epId: Long, watchedAt: Long)

    @Query(
        """
            UPDATE tv_episodes 
            SET isWatched = 1,
                watchedDate = COALESCE(watchedDate, :watchedAt)
            WHERE seasonId = :seasonId 
            AND episode_number BETWEEN 1 AND :count
            """
    )
    suspend fun markEpWatchedFromCount(seasonId: Long, count: Int, watchedAt: Long)


    @Query("DELETE FROM tv_episodes")
    suspend fun clearAll()

    @Query(
        """
        UPDATE tv_episodes
        SET name = :name,
            overview = :overview,
            air_date = :airDate,
            still_path = COALESCE(:stillPath, still_path),
            runtime = COALESCE(:runtime, runtime)
        WHERE seasonId = :seasonId AND episode_number = :episodeNumber
        """
    )
    suspend fun updateLocalizedMetadata(
        seasonId: Long,
        episodeNumber: Int,
        name: String,
        overview: String?,
        airDate: String?,
        stillPath: String?,
        runtime: Int?,
    )

}
