package com.pranshulgg.watchmaster.feature.tv.detail.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.components.listItemShape
import com.pranshulgg.watchmaster.core.ui.components.media.PosterBox
import com.pranshulgg.watchmaster.core.ui.components.media.PosterPlaceholder
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.theme.ShapeRadius
import com.pranshulgg.watchmaster.core.ui.theme.Spacing
import com.pranshulgg.watchmaster.data.local.entity.TvEpisodeEntity

/**
 * Los episodios como filas, con la misma silueta que las temporadas al desplegar una serie.
 *
 * Es la vista para leer: cabe el título entero y la sinopsis, cosa que ni el carrusel ni el
 * mosaico permiten. Reutiliza `listItemShape`, así que las esquinas del grupo se comportan igual
 * que en el resto de listas de la aplicación y no parece una pieza traída de fuera.
 *
 * Un toque marca o desmarca. Una pulsación larga abre la ficha del episodio.
 */
@Composable
fun EpisodesList(
    episodes: List<TvEpisodeEntity>,
    onToggle: (TvEpisodeEntity) -> Unit,
    onInfo: (TvEpisodeEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        episodes.forEachIndexed { index, episode ->
            EpisodeRow(
                episode = episode,
                shape = listItemShape(
                    episodes.size == 1,
                    index == 0,
                    index == episodes.lastIndex,
                ),
                onToggle = { onToggle(episode) },
                onInfo = { onInfo(episode) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EpisodeRow(
    episode: TvEpisodeEntity,
    shape: androidx.compose.ui.graphics.Shape,
    onToggle: () -> Unit,
    onInfo: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    val badge by animateColorAsState(
        targetValue = if (episode.isWatched) colorScheme.primary else colorScheme.surfaceContainerHighest,
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "episode-row-badge",
    )
    val onBadge by animateColorAsState(
        targetValue = if (episode.isWatched) colorScheme.onPrimary else colorScheme.onSurface,
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "episode-row-badge-content",
    )

    val state = if (episode.isWatched) {
        localized("visto", "watched")
    } else {
        localized("sin ver", "not watched")
    }
    val number = localized("Episodio ${episode.episode_number}", "Episode ${episode.episode_number}")

    Surface(
        shape = shape,
        color = colorScheme.surfaceBright,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .combinedClickable(onClick = onToggle, onLongClick = onInfo)
            .clearAndSetSemantics {
                contentDescription = "$number. ${episode.name}, $state"
            },
    ) {
        Row(
            modifier = Modifier.padding(end = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                PosterBox(
                    posterUrl = episode.still_path?.let { "https://image.tmdb.org/t/p/w500$it" },
                    apiPath = episode.still_path,
                    width = 116.dp,
                    height = 76.dp,
                    cornerRadius = ShapeRadius.None,
                    progressIndicatorSize = 24.dp,
                    placeholder = { PosterPlaceholder(size = 0.5f) },
                )
                Surface(
                    color = badge,
                    contentColor = onBadge,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(Spacing.xs),
                ) {
                    Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                        if (episode.isWatched) {
                            Symbol(
                                icon = R.drawable.check_24px,
                                desc = null,
                                color = onBadge,
                                size = 15.dp,
                            )
                        } else {
                            Text(
                                text = episode.episode_number.toString(),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = episode.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = episode.overview?.takeIf { it.isNotBlank() }
                        ?: localized("Sin sinopsis", "No summary"),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
