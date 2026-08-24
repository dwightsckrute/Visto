package com.dwightsckrute.visto.feature.tv.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwightsckrute.visto.core.ui.snackbar.SnackbarManager
import com.dwightsckrute.visto.data.CountryWatchProviders
import com.dwightsckrute.visto.data.local.entity.SeasonEntity
import com.dwightsckrute.visto.data.local.entity.TvBundle
import com.dwightsckrute.visto.data.local.entity.TvEpisodeEntity
import com.dwightsckrute.visto.data.repository.TvRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import kotlin.collections.emptyList
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class TvDetailsViewModel @Inject constructor(
    private val repo: TvRepository
) : ViewModel() {

    var state by mutableStateOf<TvBundle?>(null)
        private set

    var loading by mutableStateOf(false)
        private set


    fun load(tvId: Long, onError: () -> Unit) {
        if (state != null) return

        viewModelScope.launch {
            loading = true

            val tv = try {
                repo.getWholeTvData(tvId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                onError()
                return@launch
            }

            state = tv

            loading = false
        }
    }

    private var isLoadingEps = false
    fun loadEpisodes(
        tvId: Long,
        seasonId: Long,
        seasonNumber: Int
    ) {
        if (isLoadingEps) return

        isLoadingEps = true

        viewModelScope.launch {
            try {
                repo.ensureEpisodesFetched(tvId, seasonId, seasonNumber)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                SnackbarManager.show(
                    "No se pudieron obtener los datos de la temporada"
                )
            } finally {
                isLoadingEps = false
            }
        }
    }

    fun refreshSeasonData(season: SeasonEntity) {
        if (loading) return
        SnackbarManager.show("Actualizando…")
        loading = true
        viewModelScope.launch {
            try {
                repo.refreshSeasonData(season)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                SnackbarManager.show(
                    "No se pudo actualizar",
                )
            } finally {
                loading = false
            }
            loadEpisodes(season.showId, season.seasonId, season.seasonNumber)
        }
    }

    fun seasonEpisodes(seasonId: Long): StateFlow<List<TvEpisodeEntity>> {

        return repo
            .getEpisodesForSeason(seasonId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Companion.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
    }

    private val _uiState = mutableStateOf(TvDetailsUiState())
    val uiState: State<TvDetailsUiState> = _uiState

    fun hideNoteDialog() {
        _uiState.value = _uiState.value.copy(showNoteDialog = false)
    }

    fun showNoteDialog(note: String) {
        _uiState.value = _uiState.value.copy(
            showNoteDialog = true,
            note = note
        )
    }

    fun updateNoteText(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun showRatingDialog(ratingOnly: Boolean = false) {
        _uiState.value = _uiState.value.copy(showRatingDialog = true, ratingOnly = ratingOnly)
    }

    fun hideRatingDialog() {
        _uiState.value = _uiState.value.copy(showRatingDialog = false)
    }

    fun showConfirmationDialog() {
        _uiState.value = _uiState.value.copy(showConfirmationDialog = true)
    }

    fun hideConfirmationDialog() {
        _uiState.value = _uiState.value.copy(showConfirmationDialog = false)
    }

    fun showWatchProviderSheet(providers: CountryWatchProviders? = null) {
        _uiState.value =
            _uiState.value.copy(isWatchProviderSheetOpen = true, currentWatchProviders = providers)
    }

    fun hideWatchProviderSheet() {
        _uiState.value =
            _uiState.value.copy(isWatchProviderSheetOpen = false)
    }

    fun markEpWatched(
        epId: Long,
        seasonId: Long,
        episodeNumber: Int
    ) {
        viewModelScope.launch {
            repo.markEpWatched(epId, seasonId, episodeNumber)
        }
    }

    fun markEpUnWatched(
        epId: Long,
        seasonId: Long,
        episodeNumber: Int
    ) {
        viewModelScope.launch {
            repo.markEpUnWatched(epId, seasonId, episodeNumber)
        }
    }

    fun markAllEpsWatched(seasonId: Long, lastEpNumber: Int) {
        viewModelScope.launch {
            repo.markAllEpWatched(seasonId, lastEpNumber)
        }
    }

    /** Situar un episodio en un día del diario, marcándolo visto si aún no lo estaba. */
    fun updateEpisodeWatchedDate(episode: TvEpisodeEntity, watchedAtMillis: Long) {
        viewModelScope.launch {
            repo.updateEpisodeWatchedDate(
                epId = episode.epId,
                seasonId = episode.seasonId,
                episodeNumber = episode.episode_number,
                watchedAt = Instant.ofEpochMilli(watchedAtMillis),
                wasWatched = episode.isWatched,
            )
        }
    }

    fun markEpWatchedFromCount(seasonId: Long, count: Int) {
        viewModelScope.launch {
            repo.markEpWatchedFromCount(seasonId, count)
        }
    }

}