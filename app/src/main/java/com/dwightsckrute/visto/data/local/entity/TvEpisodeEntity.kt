package com.dwightsckrute.visto.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "tv_episodes",
    foreignKeys = [
        ForeignKey(
            entity = SeasonEntity::class,
            parentColumns = ["seasonId"],
            childColumns = ["seasonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("seasonId")]
)
data class TvEpisodeEntity(

    @PrimaryKey(autoGenerate = true)
    val epId: Long = 0,
    val seasonId: Long,
    val showId: Long,
    val air_date: String?,
    val episode_number: Int,
    val name: String,
    val overview: String?,
    val still_path: String?,
    val runtime: Int?,
    val isWatched: Boolean = false,
    /**
     * Cuándo lo viste, si lo viste.
     *
     * `isWatched` decía que sí pero no cuándo, así que un episodio no podía aparecer en el diario:
     * el calendario solo sabía de películas y temporadas terminadas. Con la fecha, cada episodio
     * es una entrada suya, que es el detalle que faltaba para llevar la cuenta de verdad.
     *
     * Nulo cuando está sin ver. Marcar y desmarcar la pone y la quita.
     */
    val watchedDate: Instant? = null
)
