package com.dwightsckrute.visto.feature.calendar

import java.time.LocalDate
import java.time.YearMonth

/**
 * Una cosa terminada un día concreto.
 *
 * No es una entidad nueva: se deriva de `WatchlistItemEntity.finishedDate`, que Visto ya venía
 * guardando al marcar algo como visto.
 */
data class WatchedEntry(
    val id: Long,
    val title: String,
    val mediaType: String,
    val posterPath: String?,
    val userRating: Double?,
    /** El episodio, cuando la entrada es uno: "T2E5 · Grilled". */
    val subtitle: String? = null,
    /**
     * A dónde lleva tocarla. Un episodio abre su temporada, no la serie.
     *
     * `id` es el del episodio, que no sirve para navegar, así que la serie viaja aparte.
     */
    val showId: Long? = null,
    val seasonNumber: Int? = null,
    val seasonId: Long? = null,
)

/** Los tres tipos que puede tener una entrada del diario. */
object EntryType {
    const val MOVIE = "movie"
    const val TV = "tv"
    const val EPISODE = "episode"
}

data class CalendarUiState(
    val isLoading: Boolean = true,
    val visibleMonth: YearMonth,
    val selectedDate: LocalDate?,
    /** Solo los días con algo visto; los días vacíos no ocupan memoria. */
    val entriesByDate: Map<LocalDate, List<WatchedEntry>> = emptyMap(),
    /** El selector de mes y año, desplegado bajo la cabecera. */
    val isPickingMonth: Boolean = false,
) {
    val entriesForSelectedDate: List<WatchedEntry>
        get() = selectedDate?.let { entriesByDate[it] }.orEmpty()

    private val visibleMonthEntries: List<WatchedEntry>
        get() = entriesByDate.entries
            .filter { YearMonth.from(it.key) == visibleMonth }
            .flatMap { it.value }

    /** Total del mes visible, para el encabezado. */
    val visibleMonthCount: Int
        get() = visibleMonthEntries.size

    /**
     * El desglose del mes, separado por tipo.
     *
     * Un total suelto dice cuánto, pero no qué. Cinco películas y cinco temporadas son el mismo
     * número y no se parecen en nada, y distinguirlo no cuesta nada porque el tipo ya viene en
     * cada entrada.
     */
    val visibleMonthMovies: Int
        get() = visibleMonthEntries.count { it.mediaType == EntryType.MOVIE }

    val visibleMonthShows: Int
        get() = visibleMonthEntries.count { it.mediaType == EntryType.TV }

    val visibleMonthEpisodes: Int
        get() = visibleMonthEntries.count { it.mediaType == EntryType.EPISODE }

    /** Los meses con algo visto, para marcarlos en el selector y no navegar a ciegas. */
    val monthsWithEntries: Set<YearMonth>
        get() = entriesByDate.keys.mapTo(mutableSetOf()) { YearMonth.from(it) }

    val hasAnyEntry: Boolean
        get() = entriesByDate.isNotEmpty()
}
