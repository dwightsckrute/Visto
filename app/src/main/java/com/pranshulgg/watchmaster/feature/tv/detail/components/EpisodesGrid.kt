package com.pranshulgg.watchmaster.feature.tv.detail.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.components.media.PosterBox
import com.pranshulgg.watchmaster.core.ui.components.media.PosterPlaceholder
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.theme.ShapeRadius
import com.pranshulgg.watchmaster.core.ui.theme.Spacing
import com.pranshulgg.watchmaster.data.local.entity.TvEpisodeEntity

/**
 * Los episodios como mosaico de dos columnas.
 *
 * Alternativa al carrusel, no un repuesto. El carrusel enseña un episodio cada vez y va bien para
 * seguir una temporada al día; el mosaico enseña la temporada entera, así que se ve el avance sin
 * desplazar y marcar varios seguidos son toques contiguos. En temporadas largas la diferencia se
 * nota.
 *
 * Conserva la imagen del episodio, que es lo que le da carácter al carrusel: sin ella esto sería
 * una lista de números. Lo visto se atenúa y gana una marca, de modo que el avance se lee de un
 * vistazo sin leer un solo texto.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EpisodesGrid(
    episodes: List<TvEpisodeEntity>,
    onToggle: (TvEpisodeEntity) -> Unit,
    onInfo: (TvEpisodeEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        maxItemsInEachRow = 2,
    ) {
        episodes.forEach { episode ->
            EpisodeTile(
                episode = episode,
                onToggle = { onToggle(episode) },
                onInfo = { onInfo(episode) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun FlowRowScope.EpisodeTile(
    episode: TvEpisodeEntity,
    onToggle: () -> Unit,
    onInfo: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    // Atenuar lo visto al 45% dejaba media temporada gris y sucia. Un velo mínimo basta para
    // distinguirlo sin apagar el fotograma, que es lo que le da vida a esta vista.
    val imageAlpha by animateFloatAsState(
        targetValue = if (episode.isWatched) .82f else 1f,
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "episode-tile-alpha",
    )
    val badgeColor by animateColorAsState(
        targetValue = if (episode.isWatched) colorScheme.primary else colorScheme.surfaceContainerHighest,
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "episode-tile-badge",
    )
    val onBadgeColor by animateColorAsState(
        targetValue = if (episode.isWatched) colorScheme.onPrimary else colorScheme.onSurface,
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "episode-tile-badge-content",
    )

    val state = if (episode.isWatched) {
        localized("visto", "watched")
    } else {
        localized("sin ver", "not watched")
    }
    val number = localized("Episodio ${episode.episode_number}", "Episode ${episode.episode_number}")

    Surface(
        shape = RoundedCornerShape(ShapeRadius.ExtraLarge),
        color = colorScheme.surfaceContainerHigh,
        // El anillo marca lo visto sin tocar la imagen; el color por sí solo no vale, pero
        // sumado a la insignia del check da una lectura clara de un vistazo.
        border = if (episode.isWatched) {
            BorderStroke(2.dp, colorScheme.primary)
        } else {
            null
        },
        modifier = Modifier
            .weight(1f)
            .height(140.dp)
            .clip(RoundedCornerShape(ShapeRadius.ExtraLarge))
            .combinedClickable(onClick = onToggle, onLongClick = onInfo)
            // El estado no puede quedarse solo en el color y la opacidad.
            .clearAndSetSemantics {
                contentDescription = "$number. ${episode.name}, $state"
            },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().alpha(imageAlpha)) {
                PosterBox(
                    posterUrl = episode.still_path?.let { "https://image.tmdb.org/t/p/w500$it" },
                    apiPath = episode.still_path,
                    fillMaxWidth = true,
                    height = 140.dp,
                    cornerRadius = ShapeRadius.None,
                    progressIndicatorSize = 24.dp,
                    placeholder = { PosterPlaceholder(size = 0.4f) },
                )
            }

            // Scrim funcional: sin él, un fotograma claro se come el título.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            // Degradado solo en la mitad baja y menos denso: antes ensombrecía
                            // el fotograma entero y todo se veía apagado.
                            0.5f to Color.Transparent,
                            1f to Color.Black.copy(alpha = .62f),
                        )
                    )
            )

            Surface(
                color = badgeColor,
                contentColor = onBadgeColor,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Spacing.sm),
            ) {
                Box(
                    modifier = Modifier.size(26.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (episode.isWatched) {
                        Symbol(
                            icon = R.drawable.check_24px,
                            desc = null,
                            color = onBadgeColor,
                            size = 16.dp,
                        )
                    } else {
                        Text(
                            text = episode.episode_number.toString(),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            Text(
                text = episode.name,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Spacing.sm),
            )
        }
    }
}
