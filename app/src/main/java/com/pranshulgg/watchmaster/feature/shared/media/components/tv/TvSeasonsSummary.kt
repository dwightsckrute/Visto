package com.pranshulgg.watchmaster.feature.shared.media.components.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.pranshulgg.watchmaster.core.model.WatchStatus
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.theme.Spacing
import com.pranshulgg.watchmaster.data.local.entity.SeasonEntity

/**
 * Progreso de la serie entera, como alternativa al desglose temporada a temporada.
 *
 * Suma episodios vistos y totales de todas las temporadas guardadas, así que refleja lo que hay
 * en la biblioteca, no el catálogo completo de TMDB.
 */
@Composable
fun TvSeasonsSummary(
    seasons: List<SeasonEntity>,
    modifier: Modifier = Modifier,
) {
    val totals = remember(seasons) {
        val episodes = seasons.sumOf { it.episodeCount }
        val watched = seasons.sumOf { season ->
            // Una temporada terminada cuenta entera aunque no se marcara episodio a episodio.
            if (season.status == WatchStatus.FINISHED) {
                season.episodeCount
            } else {
                (season.lastEpWatched ?: 0).coerceIn(0, season.episodeCount)
            }
        }
        Triple(watched, episodes, seasons.size)
    }
    val (watched, episodes, seasonCount) = totals
    val fraction = if (episodes > 0) watched.toFloat() / episodes else 0f

    val seasonsLabel = if (seasonCount == 1) {
        localized("1 temporada", "1 season")
    } else {
        localized("$seasonCount temporadas", "$seasonCount seasons")
    }
    val episodesLabel = localized(
        "$watched de $episodes episodios",
        "$watched of $episodes episodes",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            // Un único anuncio para todo el bloque: leer barra y textos por separado no aporta.
            .clearAndSetSemantics {
                contentDescription = "$seasonsLabel, $episodesLabel"
            },
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = seasonsLabel,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = episodesLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
