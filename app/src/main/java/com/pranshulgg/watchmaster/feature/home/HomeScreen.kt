package com.pranshulgg.watchmaster.feature.home

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
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.components.media.PosterBox
import com.pranshulgg.watchmaster.core.ui.navigation.NavRoutes
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.feature.search.SearchType
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.pranshulgg.watchmaster.feature.home.components.HomeGreeting
import com.pranshulgg.watchmaster.feature.home.components.HomeStat
import com.pranshulgg.watchmaster.feature.home.components.HomeStatsRow
import com.pranshulgg.watchmaster.feature.home.components.ContinueWatchingCarousel

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
                        value = state.libraryCount,
                        icon = R.drawable.bookmark_24px,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        onContainer = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    HomeStat(
                        label = localized("En curso", "Watching"),
                        value = state.inProgressCount,
                        icon = R.drawable.play_arrow_24px,
                        container = MaterialTheme.colorScheme.tertiaryContainer,
                        onContainer = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                    HomeStat(
                        label = localized("Visto", "Watched"),
                        value = state.finishedCount,
                        icon = R.drawable.check_24px,
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        onContainer = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                    HomeStat(
                        label = localized("Favoritos", "Favourites"),
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
                    ContinueWatchingCarousel(
                        items = state.continueWatching,
                        onItemClick = { item ->
                            if (item.mediaType == "movie") {
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
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { "${it.mediaType}-${it.id}-${it.seasonId ?: 0}" }) { item ->
                HomeMediaCard(
                    item = item,
                    showDate = showDate,
                    modifier = Modifier.animateItem(),
                    onClick = {
                        if (item.mediaType == "movie") {
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
            .width(136.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column {
            PosterBox(
                posterUrl = item.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" },
                apiPath = item.posterPath,
                width = 136.dp,
                height = 194.dp,
                cornerRadius = 0.dp,
                progressIndicatorSize = 32.dp,
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
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                // minLines además de maxLines: sin reservar las líneas, un título de una línea
                // dejaba la tarjeta más baja que la de al lado y la fila quedaba dentada.
                // Reservar por líneas y no con una altura en dp mantiene el ajuste cuando el
                // sistema usa un tamaño de letra mayor.
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (showDate) formatActivityDate(item) else localized(item.subtitleEs, item.subtitleEn),
                    style = MaterialTheme.typography.labelMedium,
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
