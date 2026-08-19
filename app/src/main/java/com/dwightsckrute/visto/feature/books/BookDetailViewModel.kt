package com.dwightsckrute.visto.feature.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwightsckrute.visto.data.repository.AuthorInfo
import com.dwightsckrute.visto.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * El autor del libro que se está mirando.
 *
 * Va en su propio estado y se pide al abrir la ficha, no al guardar el libro: son tres peticiones
 * —obra, autor, sus obras— que solo importan si alguien llega a mirar, y hacerlas al añadir
 * habría alargado el guardado de cada libro por algo que casi nunca se ve.
 */
@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val repository: BookRepository,
) : ViewModel() {

    private val _author = MutableStateFlow<AuthorInfo?>(null)
    val author: StateFlow<AuthorInfo?> = _author.asStateFlow()

    private var loadedFor: Long? = null

    fun load(bookId: Long) {
        if (loadedFor == bookId) return
        loadedFor = bookId
        viewModelScope.launch {
            _author.value = runCatching { repository.authorOf(bookId) }.getOrNull()
        }
    }
}
