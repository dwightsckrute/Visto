package com.pranshulgg.watchmaster.feature.calendar

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
)

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
        get() = visibleMonthEntries.count { it.mediaType != "tv" }

    val visibleMonthShows: Int
        get() = visibleMonthEntries.count { it.mediaType == "tv" }

    /** Los meses con algo visto, para marcarlos en el selector y no navegar a ciegas. */
    val monthsWithEntries: Set<YearMonth>
        get() = entriesByDate.keys.mapTo(mutableSetOf()) { YearMonth.from(it) }

    /**
     * Los años a los que se puede ir, del más reciente al más antiguo.
     *
     * Solo los que tienen algo, más el que se está mirando. Un par de flechas dejaría recorrer
     * también 1998, donde no hay nada que ver; una lista de los años que existen de verdad ocupa
     * menos, se toca de una vez y no promete lo que no hay.
     */
    val selectableYears: List<Int>
        get() = (monthsWithEntries.map { it.year } + visibleMonth.year)
            .distinct()
            .sortedDescending()

    val hasAnyEntry: Boolean
        get() = entriesByDate.isNotEmpty()
}
