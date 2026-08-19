package com.dwightsckrute.visto.feature.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwightsckrute.visto.data.repository.BookRepository
import com.dwightsckrute.visto.data.repository.BookResult
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BooksUiState(
    val query: String = "",
    val results: List<BookResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchFailed: Boolean = false,
    val isSheetOpen: Boolean = false,
)

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val repository: BookRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BooksUiState())
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun openSearch() = _uiState.update { it.copy(isSheetOpen = true) }

    fun closeSearch() = _uiState.update {
        it.copy(isSheetOpen = false, query = "", results = emptyList(), searchFailed = false)
    }

    /**
     * Busca mientras se escribe, con una pausa.
     *
     * Sin ella, "El infinito en un junco" serían veinticuatro búsquedas contra Open Library para
     * una sola intención. El trabajo anterior se cancela en vez de dejarse correr: si no, la
     * respuesta de un texto a medio escribir puede llegar después de la del texto completo y
     * pisarla.
     */
    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _uiState.update { it.copy(isSearching = true, searchFailed = false) }
            val found = runCatching { repository.search(query) }
            _uiState.update {
                it.copy(
                    results = found.getOrDefault(emptyList()),
                    isSearching = false,
                    searchFailed = found.isFailure,
                )
            }
        }
    }

    fun add(book: BookResult) {
        viewModelScope.launch { repository.add(book) }
    }
}

/** Lo que se espera a que alguien deje de teclear antes de preguntar al catálogo. */
private const val SEARCH_DEBOUNCE_MS = 350L
