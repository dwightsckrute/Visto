package com.pranshulgg.watchmaster.feature.tv.detail.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.pranshulgg.watchmaster.core.model.WatchStatus
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.theme.Spacing
import com.pranshulgg.watchmaster.data.local.entity.SeasonEntity

/**
 * Cambia de temporada sin salir de la serie.
 *
 * La ficha de una serie es en realidad la ficha de una temporada: recibe un `seasonId` y muestra
 * sus episodios. Sin esto, entrar a una serie te dejaba encerrado en la temporada por la que
 * habías entrado, sin forma de marcar episodios de las demás.
 *
 * Se oculta con una sola temporada, donde no habría nada que elegir.
 */
@Composable
fun SeasonSwitcher(
    seasons: List<SeasonEntity>,
    selectedSeasonId: Long,
    onSelectSeason: (SeasonEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (seasons.size < 2) return

    val ordered = seasons.sortedBy { it.seasonNumber }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        ordered.forEach { season ->
            val selected = season.seasonId == selectedSeasonId
            val label = localized(
                "Temporada ${season.seasonNumber}",
                "Season ${season.seasonNumber}",
            )
            // El estado no puede quedarse solo en el color del chip: se dice en la descripción.
            val stateLabel = when (season.status) {
                WatchStatus.FINISHED -> localized("terminada", "finished")
                WatchStatus.WATCHING -> localized("en curso", "watching")
                WatchStatus.INTERRUPTED -> localized("interrumpida", "paused")
                else -> localized("pendiente", "not started")
            }

            FilterChip(
                selected = selected,
                onClick = { onSelectSeason(season) },
                modifier = Modifier
                    .heightIn(min = Spacing.minTouchTarget)
                    .semantics { contentDescription = "$label, $stateLabel" },
                label = { Text(label) },
                shape = FilterChipDefaults.shape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        }
    }
}
