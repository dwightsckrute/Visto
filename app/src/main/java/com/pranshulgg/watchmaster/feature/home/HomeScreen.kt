package com.pranshulgg.watchmaster.feature.home

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
            SummaryCard(state)
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
                    subtitle = localized(
                        "Tus últimas películas y temporadas terminadas",
                        "Your latest completed movies and seasons",
                    ),
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SummaryCard(state: HomeUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Symbol(
                        icon = R.drawable.movie_24px,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = localized("Tu biblioteca", "Your library"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = localized(
                            "Todo lo que estás disfrutando, de un vistazo",
                            "Everything you are enjoying, at a glance",
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .75f),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SummaryMetric(localized("Guardado", "Saved"), state.libraryCount, Modifier.weight(1f))
                SummaryMetric(localized("En curso", "In progress"), state.inProgressCount, Modifier.weight(1f))
                SummaryMetric(localized("Visto", "Watched"), state.finishedCount, Modifier.weight(1f))
                SummaryMetric(localized("Favoritos", "Favorites"), state.favoriteCount, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = .62f),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
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
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(136.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
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
                localized(
                    "Añade películas y series para ver tu actividad reciente, continuar temporadas y descubrir tus hábitos.",
                    "Add movies and TV shows to see recent activity, continue seasons, and discover your habits.",
                ),
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
    val locale = if (localized("es", "en") == "en") Locale.US else Locale("es", "ES")
    val formatted = date.format(DateTimeFormatter.ofPattern("d MMM", locale))
    return "${localized(item.subtitleEs, item.subtitleEn)} · $formatted"
}
