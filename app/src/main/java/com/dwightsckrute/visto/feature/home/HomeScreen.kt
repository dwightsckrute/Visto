package com.dwightsckrute.visto.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dwightsckrute.visto.feature.home.stat.LibraryStat
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.components.media.PosterBox
import com.dwightsckrute.visto.core.ui.navigation.NavRoutes
import com.dwightsckrute.visto.core.utils.posterUrl
import com.dwightsckrute.visto.data.repository.MEDIA_TYPE_BOOK
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.feature.search.SearchType
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.dwightsckrute.visto.feature.home.components.HomeGreeting
import com.dwightsckrute.visto.feature.home.components.HomeStat
import com.dwightsckrute.visto.feature.home.components.HomeStatsRow
import com.dwightsckrute.visto.feature.home.components.HomeProgressList

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isEmpty) {
        EmptyHome(onAdd = { navController.navigate(NavRoutes.search(SearchType.MULTI)) })
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            HomeGreeting(
                inProgress = state.inProgressCount,
                watchedThisMonth = state.watchedThisMonth,
                modifier = Modifier.animateItem(),
            )
        }

        item {
            HomeStatsRow(
                stats = listOf(
                    HomeStat(
                        label = localized("Guardado", "Saved"),
                        onClick = {
                            navController.navigate(NavRoutes.libraryStat(LibraryStat.SAVED))
                        },
                        value = state.libraryCount,
                        icon = R.drawable.bookmark_24px,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        onContainer = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    HomeStat(
                        label = localized("En curso", "Watching"),
                        onClick = {
                            navController.navigate(NavRoutes.libraryStat(LibraryStat.IN_PROGRESS))
                        },
                        value = state.inProgressCount,
                        icon = R.drawable.play_arrow_24px,
                        container = MaterialTheme.colorScheme.tertiaryContainer,
                        onContainer = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                    HomeStat(
                        label = localized("Visto", "Watched"),
                        onClick = {
                            navController.navigate(NavRoutes.libraryStat(LibraryStat.FINISHED))
                        },
                        value = state.finishedCount,
                        icon = R.drawable.check_24px,
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        onContainer = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                    HomeStat(
                        label = localized("Libros", "Books"),
                        onClick = {
                            navController.navigate(NavRoutes.libraryStat(LibraryStat.BOOKS))
                        },
                        value = state.bookCount,
                        icon = R.drawable.book_24px,
                        container = MaterialTheme.colorScheme.surfaceContainerHighest,
                        onContainer = MaterialTheme.colorScheme.onSurface,
                    ),
                    HomeStat(
                        label = localized("Favoritos", "Favourites"),
                        onClick = {
                            navController.navigate(NavRoutes.libraryStat(LibraryStat.FAVOURITES))
                        },
                        value = state.favoriteCount,
                        icon = R.drawable.favorite_24px,
                        container = MaterialTheme.colorScheme.surfaceContainerHighest,
                        onContainer = MaterialTheme.colorScheme.onSurface,
                    ),
                ),
                modifier = Modifier.animateItem(),
            )
        }

        if (state.continueWatching.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.animateItem(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = localized("Continuar viendo", "Continue watching"),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = localized("Retoma justo donde lo dejaste", "Pick up where you left off"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HomeProgressList(
                        items = state.continueWatching,
                        onItemClick = { item ->
                            if (item.mediaType == MEDIA_TYPE_BOOK) {
                                navController.navigate(NavRoutes.bookDetail(item.id))
                            } else if (item.mediaType == "movie") {
                                navController.navigate(NavRoutes.movieDetail(item.id))
                            } else if (item.seasonNumber != null && item.seasonId != null) {
                                navController.navigate(
                                    NavRoutes.tvDetail(item.id, item.seasonNumber, item.seasonId)
                                )
                            }
                        },
                    )
                }
            }
        }

        // Los libros, con su propio encabezado y su propio vocabulario. Van después de lo
        // audiovisual y no mezclados con ello: "continuar viendo" no vale para algo que se lee, y
        // una sola lista obligaría a que una de las dos mitades hablara mal.
        if (state.readingBooks.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.animateItem(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = localized("Leyendo ahora", "Reading now"),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = localized(
                                "Los libros que tienes empezados",
                                "The books you have on the go",
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HomeProgressList(
                        items = state.readingBooks,
                        onItemClick = { item ->
                            navController.navigate(NavRoutes.bookDetail(item.id))
                        },
                    )
                }
            }
        }

        if (state.recentlyRead.isNotEmpty()) {
            item {
                HomeSection(
                    title = localized("Leído recientemente", "Recently read"),
                    subtitle = localized(
                        "Tus últimos libros terminados",
                        "Your latest finished books",
                    ),
                    items = state.recentlyRead,
                    navController = navController,
                    showDate = true,
                )
            }
        }

        if (state.recentlyWatched.isNotEmpty()) {
            item {
                HomeSection(
                    title = localized("Visto recientemente", "Recently watched"),
                    subtitle = localized("Tus últimas películas y temporadas terminadas", "Your latest finished movies and seasons"),
                    items = state.recentlyWatched,
                    navController = navController,
                    showDate = true,
                )
            }
        }

        if (state.recentlyAdded.isNotEmpty()) {
            item {
                HomeSection(
                    title = localized("Añadido recientemente", "Recently added"),
                    subtitle = localized("Lo último que guardaste en Visto", "Your latest additions to Visto"),
                    items = state.recentlyAdded,
                    navController = navController,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeSection(
    title: String,
    subtitle: String,
    items: List<HomeMediaItem>,
    navController: NavController,
    showDate: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // Rejilla y no fila desplazable: en horizontal solo se veían dos tarjetas y media y había
        // que arrastrar para saber qué más había. En tres columnas cabe todo lo que enseña la
        // sección de una vez, que es lo que se quiere de una pantalla de inicio.
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = HOME_GRID_COLUMNS,
        ) {
            items.take(HOME_GRID_COLUMNS * 2).forEach { item ->
                HomeMediaCard(
                    item = item,
                    showDate = showDate,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (item.mediaType == MEDIA_TYPE_BOOK) {
                            navController.navigate(NavRoutes.bookDetail(item.id))
                        } else if (item.mediaType == "movie") {
                            navController.navigate(NavRoutes.movieDetail(item.id))
                        } else if (item.seasonNumber != null && item.seasonId != null) {
                            navController.navigate(
                                NavRoutes.tvDetail(item.id, item.seasonNumber, item.seasonId)
                            )
                        }
                    },
                )
            }
        }
    }
}

/**
 * Columnas de las rejillas del inicio.
 *
 * Cuatro y no tres: con tres, la caja de cada tarjeta pesaba más que la carátula que contiene, que
 * es exactamente lo que hay que evitar en una pantalla hecha de carátulas. A cuatro la portada
 * manda y caben ocho cosas sin desplazar.
 */
private const val HOME_GRID_COLUMNS = 4

@Composable
private fun HomeMediaCard(
    item: HomeMediaItem,
    showDate: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (pressed) .965f else 1f,
        animationSpec = tween(durationMillis = if (pressed) 90 else 220),
        label = "home-media-card-scale",
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            // Sin ancho fijo: lo pone la rejilla, que reparte el que haya entre sus columnas.
            // Con 136dp clavados, cuatro tarjetas no cabían y la fila se salía.
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column {
            PosterBox(
                posterUrl = posterUrl(item.posterPath, "w342"),
                apiPath = item.posterPath,
                fillMaxWidth = true,
                height = 124.dp,
                cornerRadius = 0.dp,
                progressIndicatorSize = 24.dp,
            )
            // El hueco de la barra se reserva siempre. Mostrarla solo cuando hay progreso hacía
            // que unas tarjetas midieran 4 dp más que otras en la misma fila.
            if (item.progress != null) {
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                )
            } else {
                Spacer(Modifier.height(4.dp))
            }
            // Menos caja alrededor: con doce puntos por lado, el marco pesaba más que la
            // carátula que enmarca.
            Column(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                // minLines además de maxLines: sin reservar las líneas, un título de una línea
                // dejaba la tarjeta más baja que la de al lado y la fila quedaba dentada.
                // Reservar por líneas y no con una altura en dp mantiene el ajuste cuando el
                // sistema usa un tamaño de letra mayor.
                Text(
                    item.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (showDate) formatActivityDate(item) else localized(item.subtitleEs, item.subtitleEn),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EmptyHome(onAdd: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 96.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Symbol(
                    icon = R.drawable.play_arrow_24px,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(22.dp)
                        .size(44.dp),
                )
            }
            Text(
                localized("Tu historia empieza aquí", "Your story starts here"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                localized("Añade películas y series para ver tu actividad reciente, continuar temporadas y descubrir tus hábitos.", "Add movies and shows to see your recent activity, carry on with seasons and discover your habits."),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onAdd) {
                Text(localized("Buscar películas y series", "Search movies and TV shows"))
            }
        }
    }
}

@Composable
private fun formatActivityDate(item: HomeMediaItem): String {
    val date = item.activityDate.atZone(ZoneId.systemDefault()).toLocalDate()
    val locale = if (localized("es", "en") == "en") Locale.US else Locale.forLanguageTag("es-ES")
    val formatted = date.format(DateTimeFormatter.ofPattern("d MMM", locale))
    return "${localized(item.subtitleEs, item.subtitleEn)} · $formatted"
}
