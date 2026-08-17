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
) {
    val entriesForSelectedDate: List<WatchedEntry>
        get() = selectedDate?.let { entriesByDate[it] }.orEmpty()

    /** Total del mes visible, para el encabezado. */
    val visibleMonthCount: Int
        get() = entriesByDate.entries
            .filter { YearMonth.from(it.key) == visibleMonth }
            .sumOf { it.value.size }

    val hasAnyEntry: Boolean
        get() = entriesByDate.isNotEmpty()
}
