package com.pranshulgg.watchmaster.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pranshulgg.watchmaster.core.model.WatchStatus
import com.pranshulgg.watchmaster.data.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
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
        viewModelScope.launch {
            repository.getWatchlist().collect { items ->
                val byDate = items
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
                    .groupBy({ it.first }, { it.second })

                _uiState.update { state ->
                    state.copy(isLoading = false, entriesByDate = byDate)
                }
            }
        }
    }

    fun showMonth(month: YearMonth) = _uiState.update { it.copy(visibleMonth = month) }

    fun showPreviousMonth() = showMonth(_uiState.value.visibleMonth.minusMonths(1))

    fun showNextMonth() = showMonth(_uiState.value.visibleMonth.plusMonths(1))

    /** Volver a pulsar el día seleccionado lo deselecciona, para poder ver el mes entero. */
    fun selectDate(date: LocalDate) = _uiState.update { state ->
        state.copy(selectedDate = if (state.selectedDate == date) null else date)
    }

    fun goToToday() {
        val today = LocalDate.now(zone)
        _uiState.update { it.copy(visibleMonth = YearMonth.from(today), selectedDate = today) }
    }
}
