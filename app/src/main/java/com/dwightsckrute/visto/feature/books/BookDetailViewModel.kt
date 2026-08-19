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

    /**
     * Pide el autor, y vuelve a intentarlo si la vez anterior no salió.
     *
     * Antes se marcaba como cargado antes de saber si había salido bien, así que un corte de red
     * al abrir la ficha dejaba al autor sin aparecer hasta cerrar la aplicación. Ahora solo cuenta
     * como cargado lo que llegó.
     */
    fun load(bookId: Long) {
        if (loadedFor == bookId && _author.value != null) return
        viewModelScope.launch {
            val resolved = runCatching { repository.authorOf(bookId) }.getOrNull()
            _author.value = resolved
            if (resolved != null) loadedFor = bookId
        }
    }
}
