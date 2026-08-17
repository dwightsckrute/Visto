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
            SummaryCard(
                state = state,
                modifier = Modifier.animateItem(),
            )
        }

        if (state.continueWatching.isNotEmpty()) {
            item {
                HomeSection(
                    title = localized("Continuar viendo", "Continue watching"),
                    subtitle = localized("Retoma justo donde lo dejaste", "Pick up where you left off"),
                    items = state.continueWatching,
                    navController = navController,
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

@Composable
private fun SummaryCard(
    state: HomeUiState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Symbol(
                        icon = R.drawable.movie_24px,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = localized("Tu biblioteca", "Your library"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = localized("Todo lo que estás disfrutando, de un vistazo", "Everything you are enjoying, at a glance"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SummaryMetric(localized("Guardado", "Saved"), state.libraryCount, Modifier.weight(1f))
                    SummaryMetric(localized("En curso", "In progress"), state.inProgressCount, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SummaryMetric(localized("Visto", "Watched"), state.finishedCount, Modifier.weight(1f))
                    SummaryMetric(localized("Favoritos", "Favorites"), state.favoriteCount, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .7f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 2 }) togetherWith
                        (fadeOut(tween(160)) + slideOutVertically(tween(220)) { -it / 2 })
                },
                label = "summary-metric-value",
            ) { animatedValue ->
                Text(
                    text = animatedValue.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
            item.progress?.let { progress ->
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                )
            }
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (showDate) formatActivityDate(item) else localized(item.subtitleEs, item.subtitleEn),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
