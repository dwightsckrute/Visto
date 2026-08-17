package com.pranshulgg.watchmaster.feature.tv.detail.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.model.WatchStatus
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.components.media.MediaSectionCard
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.theme.ShapeRadius
import com.pranshulgg.watchmaster.core.ui.theme.Spacing
import com.pranshulgg.watchmaster.data.local.entity.SeasonEntity
import androidx.compose.foundation.layout.Column
import com.pranshulgg.watchmaster.core.ui.components.media.PosterBox
import com.pranshulgg.watchmaster.core.ui.components.media.PosterPlaceholder

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

    // El menú se mide contra el botón que lo abre. Por defecto se ajusta a su texto y sale una
    // columna estrecha en mitad de una tarjeta ancha, que es lo que lo hacía parecer pequeño;
    // al ocupar el ancho del botón, elegir temporada pasa a ser tan grande como abrirlo.
    val density = LocalDensity.current
    var buttonWidth by remember { mutableStateOf(0.dp) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "season-switcher-chevron",
    )

    Spacer(Modifier.height(Spacing.lg))
    MediaSectionCard(
        title = localized("Temporadas", "Seasons"),
        titleIcon = R.drawable.list_alt_24px,
    ) {
        Box(modifier = modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)) {
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Spacing.minTouchTarget)
                    .padding(vertical = Spacing.xs)
                    .onGloballyPositioned {
                        buttonWidth = with(density) { it.size.width.toDp() }
                    },
                shapes = ButtonDefaults.shapes(),
                contentPadding = PaddingValues(
                    horizontal = Spacing.lg,
                    vertical = Spacing.sm,
                ),
                onClick = { expanded = true },
            ) {
                // Dos líneas y no una: el botón es lo único que dice en qué temporada estás, así
                // que decir además cuánto llevas de ella ahorra abrir el desplegable para mirarlo.
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selected.seasonLabel(),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = selected.progressLabel(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
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
                modifier = if (buttonWidth > 0.dp) Modifier.width(buttonWidth) else Modifier,
            ) {
                ordered.forEach { season ->
                    val isSelected = season.seasonId == selectedSeasonId
                    val label = season.seasonLabel()
                    val state = season.stateLabel()
                    val progress = season.progressLabel()

                    DropdownMenuItem(
                        modifier = Modifier.semantics {
                            contentDescription = "$label, $state, $progress"
                        },
                        onClick = {
                            expanded = false
                            if (!isSelected) onSelectSeason(season)
                        },
                        // La carátula de la temporada, que es como se reconocen entre sí mucho
                        // antes que por el número. En una serie larga la lista de "Temporada N"
                        // es indistinguible; con la imagen se elige de un vistazo.
                        contentPadding = PaddingValues(
                            horizontal = Spacing.md,
                            vertical = Spacing.sm,
                        ),
                        leadingIcon = {
                            PosterBox(
                                posterUrl = season.posterPath
                                    ?.let { "https://image.tmdb.org/t/p/w154$it" },
                                apiPath = season.posterPath,
                                width = 44.dp,
                                height = 66.dp,
                                cornerRadius = ShapeRadius.Small,
                                progressIndicatorSize = 16.dp,
                                placeholder = { PosterPlaceholder(size = 0.5f) },
                            )
                        },
                        text = {
                            Column(modifier = Modifier.padding(start = Spacing.xs)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                                Text(
                                    text = "$state · $progress",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        trailingIcon = {
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
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(Spacing.md))
}

@Composable
private fun SeasonEntity.seasonLabel(): String =
    localized("Temporada $seasonNumber", "Season $seasonNumber")

/** Cuántos episodios llevas de la temporada, en palabras. */
@Composable
private fun SeasonEntity.progressLabel(): String {
    val watched = if (status == WatchStatus.FINISHED) episodeCount else (lastEpWatched ?: 0)
    return localized(
        "$watched de $episodeCount episodios",
        "$watched of $episodeCount episodes",
    )
}

@Composable
private fun SeasonEntity.stateLabel(): String = when (status) {
    WatchStatus.FINISHED -> localized("Terminada", "Finished")
    WatchStatus.WATCHING -> localized("En curso", "Watching")
    WatchStatus.INTERRUPTED -> localized("Interrumpida", "Paused")
    else -> localized("Pendiente", "Not started")
}
