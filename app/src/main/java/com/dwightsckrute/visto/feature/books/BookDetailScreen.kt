package com.dwightsckrute.visto.feature.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.components.AppPill
import com.dwightsckrute.visto.core.ui.components.LargeTopBarScaffold
import com.dwightsckrute.visto.core.ui.components.NavigateUpBtn
import com.dwightsckrute.visto.core.ui.components.PillSize
import com.dwightsckrute.visto.core.ui.components.TooltipIconBtn
import com.dwightsckrute.visto.core.ui.components.media.MediaSectionCard
import com.dwightsckrute.visto.core.ui.components.media.PosterBox
import com.dwightsckrute.visto.core.ui.components.media.PosterPlaceholder
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.theme.Spacing
import com.dwightsckrute.visto.core.utils.posterUrl
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel

/**
 * La ficha de un libro.
 *
 * Reutiliza las piezas de las fichas de cine —la cabecera grande, las tarjetas de sección, las
 * píldoras— y no su andamiaje, porque aquel gira alrededor de traerse una ficha de TMDB y aquí
 * todo lo que hay ya está guardado. La sinopsis se pidió al añadir el libro; si Open Library no
 * la tenía, se dice, en vez de dejar un hueco que parezca un error.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookDetailScreen(id: Long, navController: NavController) {
    val viewModel: WatchlistViewModel = hiltViewModel()
    val detailViewModel: BookDetailViewModel = hiltViewModel()
    val flow = remember(id) { viewModel.item(id) }
    val book by flow.collectAsStateWithLifecycle()
    val author by detailViewModel.author.collectAsStateWithLifecycle()

    LaunchedEffect(id) { detailViewModel.load(id) }

    LargeTopBarScaffold(
        title = book?.title.orEmpty(),
        navigationIcon = { NavigateUpBtn(navController) },
        actions = {
            book?.let { item ->
                TooltipIconBtn(
                    icon = R.drawable.favorite_24px,
                    tooltipText = if (item.isFavorite) {
                        localized("Quitar de favoritos", "Remove from favourites")
                    } else {
                        localized("Añadir a favoritos", "Add to favourites")
                    },
                    onClick = { viewModel.setFavorite(item.id, !item.isFavorite) },
                )
                // Borrar existe aquí porque no existía en ningún otro sitio: un libro añadido por
                // error se quedaba en la biblioteca para siempre.
                TooltipIconBtn(
                    icon = R.drawable.delete_24px,
                    tooltipText = localized("Quitar de la biblioteca", "Remove from library"),
                    onClick = {
                        viewModel.delete(item.id)
                        navController.popBackStack()
                    },
                )
            }
        },
    ) { padding ->
        val item = book
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = localized("Este libro ya no está", "This book is gone"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@LargeTopBarScaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = Spacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    PosterBox(
                        posterUrl = posterUrl(item.posterPath, "w342"),
                        apiPath = item.posterPath,
                        width = 120.dp,
                        height = 180.dp,
                        cornerRadius = ShapeRadius.Large,
                        placeholder = { PosterPlaceholder(size = 0.6f) },
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        // El autor se guardó donde iría la fecha de estreno, porque es lo que se
                        // lee junto al título en cualquier lista de libros.
                        item.releaseDate?.takeIf { it.isNotBlank() }?.let { author ->
                            Text(
                                text = author,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        AppPill(
                            label = item.status.readingLabel(),
                            icon = R.drawable.book_24px,
                            size = PillSize.Small,
                        )
                    }
                }
            }

            item {
                ReadingActions(
                    status = item.status,
                    onToRead = { viewModel.reset(item.id) },
                    onReading = { viewModel.start(item.id) },
                    onRead = { viewModel.finish(item.id) },
                )
            }

            author?.let { info ->
                item {
                    MediaSectionCard(
                        title = localized("El autor", "The author"),
                        titleIcon = R.drawable.article_person_24px,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.lg),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        ) {
                            PosterBox(
                                posterUrl = info.photoUrl,
                                apiPath = info.photoUrl,
                                width = 64.dp,
                                height = 64.dp,
                                circular = true,
                                placeholder = { PosterPlaceholder(size = 0.5f) },
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                Text(
                                    text = info.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                info.years?.let { years ->
                                    Text(
                                        text = years,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        info.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                            Text(
                                text = bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 6,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = Spacing.lg),
                            )
                        }
                    }
                }

                if (info.works.isNotEmpty()) {
                    item {
                        MediaSectionCard(
                            title = localized("Del mismo autor", "By the same author"),
                            titleIcon = R.drawable.book_24px,
                            trailingContent = {
                                AppPill(
                                    label = localized(
                                        "${info.totalWorks} obras",
                                        "${info.totalWorks} works",
                                    ),
                                    size = PillSize.Small,
                                )
                            },
                        ) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = Spacing.lg),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            ) {
                                items(info.works, key = { it.id }) { other ->
                                    Column(modifier = Modifier.width(96.dp)) {
                                        PosterBox(
                                            posterUrl = other.coverUrl,
                                            apiPath = other.coverUrl,
                                            width = 96.dp,
                                            height = 144.dp,
                                            cornerRadius = ShapeRadius.Medium,
                                            placeholder = { PosterPlaceholder(size = 0.5f) },
                                        )
                                        Text(
                                            text = other.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(top = Spacing.xs),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                MediaSectionCard(
                    title = localized("Sinopsis", "Synopsis"),
                    titleIcon = R.drawable.description_24px,
                ) {
                    Text(
                        text = item.overview?.takeIf { it.isNotBlank() }
                            ?: localized(
                                "Open Library no tiene sinopsis de este libro.",
                                "Open Library has no synopsis for this book.",
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Spacing.lg),
                    )
                }
            }
        }
    }
}

/**
 * Los tres estados, como tres botones.
 *
 * Una barra flotante como la del cine daría un solo botón que va rotando de estado, y para un
 * libro eso obliga a pasar por "leyendo" para llegar a "leído". Aquí se salta directo, que es lo
 * que se hace al añadir algo que ya leíste.
 */
@Composable
private fun ReadingActions(
    status: WatchStatus,
    onToRead: () -> Unit,
    onReading: () -> Unit,
    onRead: () -> Unit,
) {
    val options = listOf(
        Triple(WatchStatus.WANT_TO_WATCH, localized("Pendiente", "To read"), onToRead),
        Triple(WatchStatus.WATCHING, localized("Leyendo", "Reading"), onReading),
        Triple(WatchStatus.FINISHED, localized("Leído", "Read"), onRead),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        options.forEach { (value, label, action) ->
            val selected = status == value ||
                (value == WatchStatus.WATCHING && status == WatchStatus.INTERRUPTED)
            androidx.compose.material3.Surface(
                onClick = action,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(ShapeRadius.ExtraLarge),
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                contentColor = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            ) {
                Box(
                    modifier = Modifier.padding(vertical = Spacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
