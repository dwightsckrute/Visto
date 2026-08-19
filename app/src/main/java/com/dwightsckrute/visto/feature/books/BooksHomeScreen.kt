package com.dwightsckrute.visto.feature.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.components.AppPill
import com.dwightsckrute.visto.core.ui.components.EmptyContainerPlaceholder
import com.dwightsckrute.visto.core.ui.components.MediaTabRow
import com.dwightsckrute.visto.core.ui.components.PillSize
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.components.media.PosterBox
import com.dwightsckrute.visto.core.ui.components.media.PosterPlaceholder
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.theme.Spacing
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.data.repository.MEDIA_TYPE_BOOK
import com.dwightsckrute.visto.feature.movie.components.MovieItems
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import kotlinx.coroutines.launch

/**
 * Los libros.
 *
 * Comparten tabla, estados, fechas, notas y valoraciones con las películas y las series: un libro
 * es una fila más de la biblioteca con `mediaType = "book"`, así que el diario y las listas lo
 * recogen sin tocarlos. Lo único suyo es de dónde salen los datos, que es Open Library.
 *
 * Esta primera versión lista lo guardado y deja añadir. La ficha del libro y el progreso por
 * páginas no están todavía; ninguna de las dos cosas hace falta para empezar a usarlo, y
 * construirlas antes de tener libros dentro habría sido decidir a ciegas cómo se ven.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BooksHomeScreen(
    navController: NavController,
    scrollBehavior: FloatingToolbarScrollBehavior,
    scrollBehaviorTopBar: TopAppBarScrollBehavior,
) {
    val watchlistViewModel: WatchlistViewModel = hiltViewModel()
    val booksViewModel: BooksViewModel = hiltViewModel()
    val items by watchlistViewModel.watchlist.collectAsState()
    val state by booksViewModel.uiState.collectAsState()

    val books = items.filter { it.mediaType == MEDIA_TYPE_BOOK }
    val tabs = BookTab.entries
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            MediaTabRow(
                titles = tabs.map { it.title },
                pagerState = pagerState,
                onTabClick = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
            )
        },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize(),
            verticalAlignment = Alignment.Top,
        ) { page ->
            val tab = tabs[page]
            val shown = books.filter { tab.matches(it.status) }
            if (shown.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyContainerPlaceholder(
                        icon = R.drawable.book_24px,
                        text = tab.emptyTitle(),
                        description = localized(
                            "Añade libros desde la búsqueda, junto a las películas y las series.",
                            "Add books from search, alongside films and shows.",
                        ),
                    )
                }
            } else {
                // La misma lista que películas y series: mismas filas, mismas esquinas de grupo,
                // misma sección de fijados. Escribir una propia era lo que hacía que los libros
                // se vieran distintos sin motivo.
                MovieItems(
                    scrollBehavior = scrollBehavior,
                    scrollBehaviorTopBar = scrollBehaviorTopBar,
                    navController = navController,
                    onLongActionMovieRequest = {},
                    pinnedItems = shown.filter { it.isPinned },
                    normalItems = shown.filterNot { it.isPinned },
                )
            }
        }
    }
}

/**
 * Las tres pestañas, con el vocabulario de los libros.
 *
 * Los estados son los mismos que los de una película —la biblioteca es una sola tabla— y solo
 * cambia cómo se dicen. El título va en una función y no en el constructor porque `localized` es
 * @Composable: como constante se fijaría en español al cargar la clase.
 */
private enum class BookTab {
    TO_READ, READING, READ;

    val title: String
        @Composable get() = when (this) {
            TO_READ -> localized("Pendientes", "To read")
            READING -> localized("Leyendo", "Reading")
            READ -> localized("Leídos", "Read")
        }

    @Composable
    fun emptyTitle(): String = when (this) {
        TO_READ -> localized("Nada pendiente", "Nothing to read")
        READING -> localized("No estás leyendo nada", "Not reading anything")
        READ -> localized("Aún no has terminado ninguno", "Nothing finished yet")
    }

    fun matches(status: WatchStatus): Boolean = when (this) {
        TO_READ -> status == WatchStatus.WANT_TO_WATCH
        // Un libro aparcado sigue siendo un libro empezado, y esconderlo en una cuarta pestaña
        // por sí solo sería darle más peso del que tiene.
        READING -> status == WatchStatus.WATCHING || status == WatchStatus.INTERRUPTED
        READ -> status == WatchStatus.FINISHED
    }
}

/**
 * El estado, dicho en el idioma de los libros.
 *
 * "Visto" y "viendo" no valen aquí, y el resto de la aplicación los usa en todas partes. De
 * momento se traduce solo en la superficie; si los libros se quedan, el vocabulario habrá que
 * resolverlo en `WatchStatus` y no fila a fila.
 */
@Composable
internal fun WatchStatus.readingLabel(): String = when (this) {
    WatchStatus.WATCHING -> localized("Leyendo", "Reading")
    WatchStatus.FINISHED -> localized("Leído", "Read")
    WatchStatus.INTERRUPTED -> localized("Aparcado", "Paused")
    else -> localized("Pendiente", "To read")
}
