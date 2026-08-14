package com.pranshulgg.watchmaster.feature.search

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pranshulgg.watchmaster.core.network.TvSeasonDto
import com.pranshulgg.watchmaster.core.ui.localization.AppLanguage
import com.pranshulgg.watchmaster.core.ui.snackbar.SnackbarManager
import com.pranshulgg.watchmaster.data.repository.SearchRepository
import com.pranshulgg.watchmaster.data.repository.WatchlistRepository
import com.pranshulgg.watchmaster.feature.shared.WatchlistViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: SearchRepository,
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {

    var query by mutableStateOf("")
        private set

    var results by mutableStateOf<List<SearchItem>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var hasSearched by mutableStateOf(false)
        private set

    var searchFailed by mutableStateOf(false)
        private set

    var seasonData by mutableStateOf<List<TvSeasonDto>>(emptyList())
        private set

    var seasonLoading by mutableStateOf(false)
        private set

    private var searchJob: Job? = null
    private val seasonCache = mutableMapOf<Long, List<TvSeasonDto>>()

    fun onQueryChange(value: String, type: SearchType) {
        query = value
        searchJob?.cancel()
        searchFailed = false

        if (value.isBlank()) {
            results = emptyList()
            loading = false
            hasSearched = false
            return
        }

        if (value.trim().length < 2) {
            results = emptyList()
            loading = false
            hasSearched = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(450)
            performSearch(value.trim(), type)
        }
    }

    fun search(type: SearchType = SearchType.MULTI) {
        val searchText = query.trim()
        if (searchText.isBlank()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            performSearch(searchText, type)
        }
    }

    private suspend fun performSearch(searchText: String, type: SearchType) {
        loading = true
        searchFailed = false
        try {
            val response = repo.search(searchText, type)
            if (query.trim() == searchText) {
                results = response
                hasSearched = true
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            if (query.trim() == searchText) {
                results = emptyList()
                hasSearched = true
                searchFailed = true
            }
        } finally {
            if (query.trim() == searchText) loading = false
        }
    }

    fun fetchSeasonData(tvId: Long) {
        seasonCache[tvId]?.let {
            seasonData = it
            return
        }

        viewModelScope.launch {
            seasonLoading = true
            val data = repo.getSeasonData(tvId)
            seasonCache[tvId] = data
            seasonData = data
            seasonLoading = false
        }
    }

    fun addToWatchlist(
        item: SearchItem,
        tvDetails: List<TvSeasonDto>? = null,
        watchlistViewModel: WatchlistViewModel,
    ) {
        viewModelScope.launch {
            val exists = watchlistViewModel.exists(item.id)
            if (!exists) watchlistRepository.addFromSearch(item, tvDetails)
            if (item.mediaType == "tv" && tvDetails != null) {
                addSeasonToWatchlist(item.id, tvDetails)
            } else if (item.mediaType == "tv") {
                SnackbarManager.show(
                    AppLanguage.text("No se encontró ninguna temporada", "No seasons were found")
                )
            }
        }
    }

    private fun addSeasonToWatchlist(id: Long, tvDetails: List<TvSeasonDto>) {
        viewModelScope.launch { watchlistRepository.insertSeason(id, tvDetails) }
    }

    suspend fun onSearchItemClick(
        item: SearchItem,
        watchlistViewModel: WatchlistViewModel,
    ) {
        if (watchlistViewModel.exists(item.id) && item.mediaType != "tv") {
            SnackbarManager.show(AppLanguage.text("Ya está en pendientes", "Already in your watchlist"))
        } else {
            updateSelectedItem(item)
            if (item.mediaType == "tv") fetchSeasonData(item.id)
            showSheet()
        }
    }

    private val _uiState = mutableStateOf(SearchUiState())
    val uiState: MutableState<SearchUiState> = _uiState

    private fun updateSelectedItem(item: SearchItem) {
        _uiState.value = _uiState.value.copy(selectedItem = item)
    }

    fun updateSelectedSeasonList(list: List<TvSeasonDto>?) {
        _uiState.value = _uiState.value.copy(selectedSeasonList = list)
    }

    private fun showSheet() {
        _uiState.value = _uiState.value.copy(isSheetOpen = true)
    }

    fun hideSheet() {
        _uiState.value = _uiState.value.copy(isSheetOpen = false)
    }
}
