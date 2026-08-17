package com.pranshulgg.watchmaster.feature.tv.detail.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.model.WatchStatus
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.theme.ShapeRadius
import com.pranshulgg.watchmaster.core.ui.theme.Spacing
import com.pranshulgg.watchmaster.data.local.entity.SeasonEntity

/**
 * Cambia de temporada sin salir de la serie.
 *
 * La ficha de una serie es en realidad la ficha de una temporada: recibe un `seasonId` y muestra
 * sus episodios. Sin esto, entrar a una serie te dejaba encerrado en la temporada por la que
 * habías entrado, sin forma de marcar episodios de las demás.
 *
 * Desplegable y no grupo de botones: una serie puede tener diez o veinte temporadas y un grupo
 * conectado acaba mandando casi todo a un menú de desbordamiento, que es un desplegable peor
 * disfrazado. Así el nombre de la temporada activa se lee entero y la lista crece sin deformar
 * nada. Cada entrada dice su estado con texto, no solo con color.
 *
 * Se oculta con una sola temporada, donde no habría nada que elegir.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SeasonSwitcher(
    seasons: List<SeasonEntity>,
    selectedSeasonId: Long,
    onSelectSeason: (SeasonEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (seasons.size < 2) return

    val ordered = remember(seasons) { seasons.sortedBy { it.seasonNumber } }
    val selected = ordered.firstOrNull { it.seasonId == selectedSeasonId } ?: ordered.first()
    var expanded by remember { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "season-switcher-chevron",
    )

    Box(modifier = modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)) {
        FilledTonalButton(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Spacing.minTouchTarget),
            shapes = ButtonDefaults.shapes(),
            onClick = { expanded = true },
        ) {
            Symbol(
                icon = R.drawable.list_alt_24px,
                desc = null,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = selected.seasonLabel(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Symbol(
                icon = R.drawable.keyboard_arrow_down_24px,
                desc = null,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.rotate(chevronRotation),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(ShapeRadius.Large),
        ) {
            ordered.forEach { season ->
                val isSelected = season.seasonId == selectedSeasonId
                val label = season.seasonLabel()
                val state = season.stateLabel()

                DropdownMenuItem(
                    modifier = Modifier.semantics { contentDescription = "$label, $state" },
                    onClick = {
                        expanded = false
                        if (!isSelected) onSelectSeason(season)
                    },
                    leadingIcon = {
                        if (isSelected) {
                            Symbol(
                                icon = R.drawable.check_24px,
                                desc = null,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Spacer(Modifier.size(24.dp))
                        }
                    },
                    text = {
                        Text(
                            text = label,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = state,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SeasonEntity.seasonLabel(): String =
    localized("Temporada $seasonNumber", "Season $seasonNumber")

@Composable
private fun SeasonEntity.stateLabel(): String = when (status) {
    WatchStatus.FINISHED -> localized("Terminada", "Finished")
    WatchStatus.WATCHING -> localized("En curso", "Watching")
    WatchStatus.INTERRUPTED -> localized("Interrumpida", "Paused")
    else -> localized("Pendiente", "Not started")
}
