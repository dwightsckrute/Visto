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
/**
 * Lo que se sabe de algo que se está pidiendo.
 *
 * Tres estados y no dos, porque "todavía no ha llegado" y "no existe" se veían igual y decían
 * cosas contrarias: una ficha recién abierta afirmaba que el libro no tiene sinopsis mientras la
 * estaba pidiendo.
 */
sealed interface Loadable<out T> {
    data object Loading : Loadable<Nothing>
    data class Ready<T>(val value: T?) : Loadable<T>
}

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val repository: BookRepository,
) : ViewModel() {

    private val _author = MutableStateFlow<Loadable<AuthorInfo>>(Loadable.Loading)
    val author: StateFlow<Loadable<AuthorInfo>> = _author.asStateFlow()

    private val _overview = MutableStateFlow<Loadable<String>>(Loadable.Loading)
    val overview: StateFlow<Loadable<String>> = _overview.asStateFlow()

    private var loadedFor: Long? = null

    /**
     * Pide autor y sinopsis, y vuelve a intentarlo si la vez anterior no salió.
     *
     * Antes se marcaba como cargado antes de saber si había salido bien, así que un corte de red
     * al abrir la ficha dejaba al autor sin aparecer hasta cerrar la aplicación. Ahora solo cuenta
     * como cargado lo que llegó.
     *
     * La sinopsis se pide aquí y no solo al guardar el libro: guardada una vez, un fallo de red en
     * ese momento la dejaba vacía para siempre. Si ya está en la base no se vuelve a pedir — el
     * repositorio lo comprueba antes de salir a la red.
     */
    fun load(bookId: Long) {
        if (loadedFor == bookId) return

        viewModelScope.launch {
            _author.value = Loadable.Loading
            val resolved = runCatching { repository.authorOf(bookId) }.getOrNull()
            _author.value = Loadable.Ready(resolved)
            if (resolved != null) loadedFor = bookId
        }

        viewModelScope.launch {
            _overview.value = Loadable.Loading
            val text = runCatching { repository.refreshOverview(bookId) }.getOrNull()
            _overview.value = Loadable.Ready(text?.takeIf(String::isNotBlank))
        }
    }
}
