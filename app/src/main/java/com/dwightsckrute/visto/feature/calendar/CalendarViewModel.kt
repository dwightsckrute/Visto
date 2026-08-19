package com.dwightsckrute.visto.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.data.repository.TvRepository
import com.dwightsckrute.visto.data.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@HiltViewModel
class CalendarViewModel @Inject constructor(
    repository: WatchlistRepository,
    private val tvRepository: TvRepository,
) : ViewModel() {

    private val zone: ZoneId = ZoneId.systemDefault()

    private val _uiState = MutableStateFlow(
        CalendarUiState(
            visibleMonth = YearMonth.now(zone),
            selectedDate = LocalDate.now(zone),
        )
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        // Tres fuentes para un mismo diario: lo terminado de la lista, y ahora cada episodio con
        // su fecha. Los episodios necesitan además la lista y las temporadas para saber de qué
        // serie son y a qué temporada volver, así que las tres se combinan en vez de leerse por
        // separado; si no, un episodio aparecería sin título hasta que cargara lo demás.
        viewModelScope.launch {
            combine(
                repository.getWatchlist(),
                repository.getAllSeasons(),
                tvRepository.watchedEpisodes(),
            ) { items, seasons, episodes ->
                val fromLibrary = items
                    .asSequence()
                    .filter { it.status == WatchStatus.FINISHED }
                    .mapNotNull { item ->
                        val finished: Instant = item.finishedDate ?: return@mapNotNull null
                        finished.atZone(zone).toLocalDate() to WatchedEntry(
                            id = item.id,
                            title = item.title,
                            mediaType = item.mediaType,
                            posterPath = item.posterPath,
                            userRating = item.userRating,
                        )
                    }

                val showsById = items.associateBy { it.id }
                val seasonsById = seasons.associateBy { it.seasonId }

                val fromEpisodes = episodes
                    .asSequence()
                    .mapNotNull { episode ->
                        val watched: Instant = episode.watchedDate ?: return@mapNotNull null
                        val show = showsById[episode.showId]
                        val season = seasonsById[episode.seasonId]
                        watched.atZone(zone).toLocalDate() to WatchedEntry(
                            id = episode.epId,
                            // El título es el de la serie: en un día con cuatro episodios de dos
                            // series distintas, lo primero que hay que distinguir es cuál es cuál.
                            title = show?.title ?: episode.name,
                            mediaType = EntryType.EPISODE,
                            posterPath = show?.posterPath,
                            userRating = null,
                            subtitle = buildString {
                                season?.seasonNumber?.let { append("T").append(it).append("E") }
                                append(episode.episode_number)
                                append(" · ")
                                append(episode.name)
                            },
                            showId = episode.showId,
                            seasonNumber = season?.seasonNumber,
                            seasonId = episode.seasonId,
                        )
                    }

                (fromLibrary + fromEpisodes).groupBy({ it.first }, { it.second })
            }
                // El diario agrupa la biblioteca entera y todos los episodios fechados por día;
                // hacerlo en Main significaba que abrirlo con muchos episodios se notara.
                .flowOn(Dispatchers.Default)
                .collect { byDate ->
                _uiState.update { state ->
                    state.copy(isLoading = false, entriesByDate = byDate)
                }
            }
        }
    }

    fun showMonth(month: YearMonth) =
        _uiState.update { it.copy(visibleMonth = month, isPickingMonth = false) }

    fun showPreviousMonth() = showMonth(_uiState.value.visibleMonth.minusMonths(1))

    fun showNextMonth() = showMonth(_uiState.value.visibleMonth.plusMonths(1))

    fun toggleMonthPicker() = _uiState.update { it.copy(isPickingMonth = !it.isPickingMonth) }

    /** Volver a pulsar el día seleccionado lo deselecciona, para poder ver el mes entero. */
    fun selectDate(date: LocalDate) = _uiState.update { state ->
        state.copy(selectedDate = if (state.selectedDate == date) null else date)
    }

    fun goToToday() {
        val today = LocalDate.now(zone)
        _uiState.update {
            it.copy(
                visibleMonth = YearMonth.from(today),
                selectedDate = today,
                isPickingMonth = false,
            )
        }
    }
}
