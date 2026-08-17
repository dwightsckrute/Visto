package com.pranshulgg.watchmaster.feature.tv.detail.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.theme.ShapeRadius
import com.pranshulgg.watchmaster.core.ui.theme.Spacing
import com.pranshulgg.watchmaster.data.local.entity.TvEpisodeEntity

/**
 * Los episodios como cuadrícula de números.
 *
 * Alternativa al carrusel, no un repuesto discreto. El carrusel enseña un episodio cada vez, con
 * su imagen, y va bien para ir siguiendo una temporada al día. Esta vista enseña la temporada
 * entera de un vistazo: se ve cuánto llevas sin desplazar, y marcar varios seguidos es tocar
 * casillas contiguas en lugar de arrastrar. En temporadas de veinte episodios la diferencia es
 * grande.
 *
 * Un toque marca o desmarca. Una pulsación larga abre la ficha del episodio, que es donde vive el
 * resto de información.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EpisodeTile(
    episode: TvEpisodeEntity,
    onToggle: () -> Unit,
    onInfo: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    val container by animateColorAsState(
        targetValue = if (episode.isWatched) {
            colorScheme.primary
        } else {
            colorScheme.surfaceContainerHighest
        },
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "episode-tile-container",
    )
    val content by animateColorAsState(
        targetValue = if (episode.isWatched) colorScheme.onPrimary else colorScheme.onSurface,
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "episode-tile-content",
    )

    val state = if (episode.isWatched) {
        localized("visto", "watched")
    } else {
        localized("sin ver", "not watched")
    }
    val label = localized("Episodio ${episode.episode_number}", "Episode ${episode.episode_number}")

    Surface(
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(ShapeRadius.Medium),
        modifier = Modifier
            .size(Spacing.minTouchTarget)
            .clip(RoundedCornerShape(ShapeRadius.Medium))
            .combinedClickable(onClick = onToggle, onLongClick = onInfo)
            // El estado se dice con palabras: en la cuadrícula solo lo distingue el color.
            .clearAndSetSemantics { contentDescription = "$label, $state" },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = episode.episode_number.toString(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
