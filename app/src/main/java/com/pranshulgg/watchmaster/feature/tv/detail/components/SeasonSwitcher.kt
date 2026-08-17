package com.pranshulgg.watchmaster.feature.tv.detail.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
 * Usa `ButtonGroup`, el grupo conectado de Material 3 Expressive: es el componente pensado para
 * una selección única entre pocas opciones, se ensancha al pulsar y manda las que no caben a un
 * menú de desbordamiento, cosa necesaria en series con muchas temporadas.
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

    // El contenido de ButtonGroup no es un ámbito @Composable, así que las etiquetas se
    // resuelven aquí. De paso el estado va en el texto y no solo en el color del botón.
    val entries = seasons.sortedBy { it.seasonNumber }.map { season ->
        val number = localized(
            "Temporada ${season.seasonNumber}",
            "Season ${season.seasonNumber}",
        )
        val state = when (season.status) {
            WatchStatus.FINISHED -> localized("terminada", "finished")
            WatchStatus.WATCHING -> localized("en curso", "watching")
            WatchStatus.INTERRUPTED -> localized("interrumpida", "paused")
            else -> localized("pendiente", "not started")
        }
        season to "$number · $state"
    }

    ButtonGroup(
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState)
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
    ) {
        entries.forEach { (season, label) ->
            toggleableItem(
                checked = season.seasonId == selectedSeasonId,
                label = label,
                onCheckedChange = { checked ->
                    // Selección única: volver a pulsar la activa no la desmarca.
                    if (checked) onSelectSeason(season)
                },
            )
        }
    }
}
