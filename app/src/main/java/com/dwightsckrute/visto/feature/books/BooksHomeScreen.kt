package com.dwightsckrute.visto.feature.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.dwightsckrute.visto.core.ui.components.PillSize
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.components.media.PosterBox
import com.dwightsckrute.visto.core.ui.components.media.PosterPlaceholder
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.theme.Spacing
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.data.repository.BookResult
import com.dwightsckrute.visto.data.repository.MEDIA_TYPE_BOOK
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel

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
    scrollBehaviorTopBar: TopAppBarScrollBehavior,
) {
    val watchlistViewModel: WatchlistViewModel = hiltViewModel()
    val booksViewModel: BooksViewModel = hiltViewModel()
    val items by watchlistViewModel.watchlist.collectAsState()
    val state by booksViewModel.uiState.collectAsState()

    val books = items.filter { it.mediaType == MEDIA_TYPE_BOOK }

    Column(modifier = Modifier.fillMaxSize()) {
        BookSearchField(
            query = state.query,
            onQueryChange = booksViewModel::onQueryChange,
        )

        when {
            state.query.isNotBlank() -> BookSearchResults(
                state = state,
                savedIds = books.map { it.id }.toSet(),
                onAdd = booksViewModel::add,
            )

            books.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyContainerPlaceholder(
                    icon = R.drawable.book_24px,
                    text = localized("Aún no hay libros", "No books yet"),
                    description = localized(
                        "Busca arriba por título o autor para añadir el primero.",
                        "Search above by title or author to add your first one.",
                    ),
                )
            }

            else -> SavedBooks(books)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookSearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        singleLine = true,
        shape = RoundedCornerShape(ShapeRadius.ExtraLarge),
        placeholder = { Text(localized("Buscar un libro", "Search for a book")) },
        leadingIcon = { Symbol(R.drawable.search_24px, desc = null) },
    )
}

@Composable
private fun BookSearchResults(
    state: BooksUiState,
    savedIds: Set<Long>,
    onAdd: (BookResult) -> Unit,
) {
    if (state.searchFailed) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = localized(
                    "No se pudo consultar el catálogo",
                    "Could not reach the catalogue",
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = Spacing.lg,
            end = Spacing.lg,
            bottom = Spacing.xxl,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(state.results, key = { it.id }) { book ->
            BookRow(
                title = book.title,
                subtitle = listOfNotNull(
                    book.authors.firstOrNull(),
                    book.year?.toString(),
                ).joinToString(" · "),
                coverUrl = book.coverUrl,
                // Ya guardado: se enseña igualmente para que no parezca que la búsqueda falla,
                // pero sin ofrecer añadirlo otra vez.
                trailing = if (book.id in savedIds) {
                    { AppPill(label = localized("Guardado", "Saved"), size = PillSize.Small) }
                } else {
                    null
                },
                onClick = { if (book.id !in savedIds) onAdd(book) },
            )
        }
    }
}

@Composable
private fun SavedBooks(books: List<WatchlistItemEntity>) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = Spacing.lg,
            end = Spacing.lg,
            bottom = Spacing.xxl,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(books, key = { it.id }) { book ->
            BookRow(
                title = book.title,
                subtitle = book.releaseDate.orEmpty(),
                coverUrl = book.posterPath,
                trailing = {
                    AppPill(
                        label = book.status.readingLabel(),
                        size = PillSize.Small,
                    )
                },
                onClick = {},
            )
        }
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
private fun WatchStatus.readingLabel(): String = when (this) {
    WatchStatus.WATCHING -> localized("Leyendo", "Reading")
    WatchStatus.FINISHED -> localized("Leído", "Read")
    WatchStatus.INTERRUPTED -> localized("Aparcado", "Paused")
    else -> localized("Pendiente", "To read")
}

@Composable
private fun BookRow(
    title: String,
    subtitle: String,
    coverUrl: String?,
    trailing: (@Composable () -> Unit)?,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(ShapeRadius.ExtraLarge),
        color = MaterialTheme.colorScheme.surfaceBright,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterBox(
                posterUrl = coverUrl,
                // Open Library entrega la portada como URL completa, no como una ruta que haya
                // que componer, así que el mismo valor sirve de comprobación y de origen.
                apiPath = coverUrl,
                width = 56.dp,
                height = 84.dp,
                cornerRadius = ShapeRadius.Medium,
                placeholder = { PosterPlaceholder(size = 0.6f) },
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            trailing?.invoke()
        }
    }
}
