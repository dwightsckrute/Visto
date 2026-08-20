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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Surface
import com.dwightsckrute.visto.core.utils.SharedPosterSize
import com.dwightsckrute.visto.core.ui.navigation.sharedPoster
import com.dwightsckrute.visto.core.ui.navigation.posterSharedKey
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.components.media.DatePickerSheet
import com.dwightsckrute.visto.core.utils.formatDate
import java.time.Instant
import androidx.compose.ui.platform.LocalContext
import com.dwightsckrute.visto.core.utils.shareMedia
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.components.AppPill
import com.dwightsckrute.visto.core.ui.components.entranceSettle
import com.dwightsckrute.visto.core.ui.components.rememberEntranceSettle
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
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import com.dwightsckrute.visto.feature.shared.media.ui.FloatingToolbarMediaActionsParams
import com.dwightsckrute.visto.feature.shared.media.ui.MediaActionsFloatingToolbar
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
    // La barra se esconde al bajar, como en el cine. Se crea aquí y no se recibe porque esta
    // pantalla no vive dentro de ninguna que ya tenga una.
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Companion.Bottom,
    )
    val viewModel: WatchlistViewModel = hiltViewModel()
    val detailViewModel: BookDetailViewModel = hiltViewModel()
    val context = LocalContext.current
    val flow = remember(id) { viewModel.item(id) }
    val book by flow.collectAsStateWithLifecycle()
    val author by detailViewModel.author.collectAsStateWithLifecycle()

    LaunchedEffect(id) { detailViewModel.load(id) }

    // El mismo asentamiento que la ficha de una película: con los datos ya en la base, el
    // contenido aparecía de golpe en el primer frame mientras la transición de navegación seguía
    // corriendo. Aquí no hay espera de red que lo disimule, así que se notaba más que en ninguna.
    val settle = rememberEntranceSettle(book != null)

    LargeTopBarScaffold(
        title = book?.title.orEmpty(),
        navigationIcon = { NavigateUpBtn(navController) },
        // La misma barra flotante que una película o una temporada. Tres botones arriba decían
        // lo mismo, pero eran lo único de la aplicación que decía el estado de esa forma.
        bottomBar = {
            book?.let { item ->
                MediaActionsFloatingToolbar(
                    scrollBehavior = scrollBehavior,
                    itemStatus = item.status,
                    actions = FloatingToolbarMediaActionsParams(
                        startWatching = { viewModel.start(item.id) },
                        resetWatching = { viewModel.reset(item.id) },
                        finishWatching = { viewModel.finish(item.id) },
                        interruptWatching = { viewModel.interrupt(item.id) },
                        delete = {
                            viewModel.delete(item.id)
                            navController.popBackStack()
                        },
                        togglePin = { viewModel.setPinned(item.id, !item.isPinned) },
                        toggleFavorite = { viewModel.setFavorite(item.id, !item.isFavorite) },
                        share = {
                            shareMedia(
                                context = context,
                                title = item.title,
                                tmdbId = item.id,
                                isTv = false,
                                isBook = true,
                            )
                        },
                        markWatchedOn = { readAt ->
                            viewModel.finish(item.id)
                            viewModel.updateFinishedDate(item.id, readAt)
                        },
                    ),
                    isPinned = item.isPinned,
                    isFavorite = item.isFavorite,
                    isBook = true,
                )
            }
        },
        // Sin acciones aquí: favorito, compartir y eliminar viven en el menú de la barra
        // flotante, y repetirlos arriba era ofrecer dos caminos al mismo sitio en la misma
        // pantalla.
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
            modifier = Modifier
                .fillMaxSize()
                .entranceSettle(settle)
                .nestedScroll(scrollBehavior),
            // Hueco para la barra flotante. Con solo `xxl` la última sección quedaba debajo de
            // ella y no había forma de sacarla: una ficha corta no da recorrido suficiente para
            // que la barra se retire, así que el hueco tiene que estar aunque no se desplace.
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = FLOATING_BAR_CLEARANCE,
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
                        posterUrl = posterUrl(item.posterPath, SharedPosterSize),
                        apiPath = item.posterPath,
                        width = 120.dp,
                        height = 180.dp,
                        cornerRadius = ShapeRadius.Large,
                        placeholder = { PosterPlaceholder(size = 0.6f) },
                        modifier = Modifier.sharedPoster(posterSharedKey(item.id)),
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

            // La fecha en que lo terminaste, editable, igual que en un episodio. Marcar ocurre
            // cuando te acuerdas y no cuando cerraste el libro, así que sin poder corregirla el
            // diario registraría el día que tocaste el botón.
            if (item.status == WatchStatus.FINISHED) {
                item {
                    ReadDateRow(
                        finished = item.finishedDate,
                        onPick = { millis ->
                            viewModel.updateFinishedDate(item.id, Instant.ofEpochMilli(millis))
                        },
                        bookId = item.id,
                    )
                }
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
 * El día en que lo terminaste.
 *
 * Solo aparece con el libro terminado: en uno pendiente no habría nada que fechar, y en uno a
 * medias la fecha que importa es la de acabarlo.
 */
@Composable
private fun ReadDateRow(
    finished: Instant?,
    bookId: Long,
    onPick: (Long) -> Unit,
) {
    var picking by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(ShapeRadius.ExtraLarge),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .clip(RoundedCornerShape(ShapeRadius.ExtraLarge))
            .clickable { picking = true },
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = Spacing.minTouchTarget)
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Symbol(
                icon = R.drawable.date_range_24px,
                desc = null,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = localized("Leído el", "Read on"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = finished?.formatDate() ?: localized("Sin fecha", "No date"),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (finished != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Text(
                text = if (finished != null) {
                    localized("Cambiar", "Change")
                } else {
                    localized("Añadir", "Add")
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    DatePickerSheet(
        show = picking,
        initialDate = finished?.toEpochMilli() ?: System.currentTimeMillis(),
        id = bookId,
        onDateSelected = { _, date ->
            date?.let(onPick)
            picking = false
        },
        onDismiss = { picking = false },
    )
}

/**
 * Lo que hay que dejar libre bajo el contenido para que la barra flotante no tape nada.
 *
 * Su alto más el aire con el que se separa del borde. La ficha de una película se remata con un
 * espaciador equivalente; aquí va como relleno de la lista, que es lo mismo dicho una sola vez.
 */
private val FLOATING_BAR_CLEARANCE = 112.dp
