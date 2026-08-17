package com.pranshulgg.watchmaster.feature.tv.detail.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
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
 * Los episodios como una línea de tiempo vertical.
 *
 * Sustituye a una cuadrícula de dos columnas que nunca llegó a funcionar: recortaba el fotograma
 * en un cuadro pequeño, dejaba el título en dos líneas cortadas y, sobre todo, no contaba nada que
 * la lista no contara mejor. Una rejilla ordena, pero una temporada no es una rejilla: es una
 * sucesión, y el avance va siempre de arriba abajo sin huecos.
 *
 * Eso es lo que dibuja el raíl: el hilo se colorea hasta donde has llegado, así que el progreso se
 * ve como una barra continua y no como una cuenta de marcas sueltas. El fotograma se conserva
 * —recortado en círculo sobre el hilo, que es lo que le da el aire de hitos— porque era lo mejor
 * del carrusel y perderlo dejaba una lista de números.
 *
 * Un toque marca o desmarca. Una pulsación larga abre la ficha del episodio.
 */
@Composable
fun EpisodesTimeline(
    episodes: List<TvEpisodeEntity>,
    onToggle: (TvEpisodeEntity) -> Unit,
    onInfo: (TvEpisodeEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
    ) {
        episodes.forEachIndexed { index, episode ->
            TimelineEntry(
                episode = episode,
                isFirst = index == 0,
                isLast = index == episodes.lastIndex,
                onToggle = { onToggle(episode) },
                onInfo = { onInfo(episode) },
            )
        }
    }
}

/** Diámetro del hito. Lo bastante grande para que el fotograma se reconozca. */
private val NodeSize = 52.dp

/** Grosor del aro de color que rodea el fotograma. */
private val NodeRing = 3.dp

/** Lo que queda para el fotograma una vez descontado el aro por los dos lados. */
private val NodeInner = NodeSize - NodeRing * 2

/** Ancho del carril. Deja aire a los lados del hito para que el hilo respire. */
private val RailWidth = 68.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelineEntry(
    episode: TvEpisodeEntity,
    isFirst: Boolean,
    isLast: Boolean,
    onToggle: () -> Unit,
    onInfo: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    val threadColor by animateColorAsState(
        targetValue = if (episode.isWatched) colorScheme.primary else colorScheme.outlineVariant,
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "timeline-thread",
    )
    val ringColor by animateColorAsState(
        targetValue = if (episode.isWatched) colorScheme.primary else colorScheme.surfaceContainerHighest,
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "timeline-ring",
    )

    val state = if (episode.isWatched) {
        localized("visto", "watched")
    } else {
        localized("sin ver", "not watched")
    }
    val number = localized("Episodio ${episode.episode_number}", "Episode ${episode.episode_number}")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(onClick = onToggle, onLongClick = onInfo)
            .clearAndSetSemantics {
                contentDescription = "$number. ${episode.name}, $state"
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(RailWidth)
                .fillMaxHeight()
                // El hilo se dibuja de borde a borde y el hito lo tapa por el centro. Cortarlo
                // en los extremos de la temporada evita que parezca que viene de algún sitio.
                .drawBehind {
                    val x = size.width / 2f
                    val middle = size.height / 2f
                    drawLine(
                        color = threadColor,
                        start = Offset(x, if (isFirst) middle else 0f),
                        end = Offset(x, if (isLast) middle else size.height),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            // El hito y su chapa van en una caja del tamaño del hito. Si la chapa se alineara
            // dentro del carril se iría a la esquina de la celda, que es tan alta como el texto
            // de al lado, y acabaría suelta bajo el círculo en vez de pegada a él.
            Box(modifier = Modifier.size(NodeSize)) {
                Surface(
                    shape = CircleShape,
                    color = ringColor,
                    modifier = Modifier.size(NodeSize),
                ) {
                    Box(
                        modifier = Modifier.padding(NodeRing),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(modifier = Modifier.clip(CircleShape)) {
                            PosterBox(
                                posterUrl = episode.still_path
                                    ?.let { "https://image.tmdb.org/t/p/w500$it" },
                                apiPath = episode.still_path,
                                width = NodeInner,
                                height = NodeInner,
                                cornerRadius = ShapeRadius.None,
                                progressIndicatorSize = 16.dp,
                                placeholder = { PosterPlaceholder(size = 0.5f) },
                            )
                        }
                    }
                }

                // La chapa dice el número, y el número deja paso a la marca al verlo. El color
                // sólido no es decoración: encima del hilo y del fotograma, un tono de superficie
                // se perdía y el número quedaba flotando sobre la imagen.
                Surface(
                    shape = CircleShape,
                    color = if (episode.isWatched) {
                        colorScheme.primary
                    } else {
                        colorScheme.secondaryContainer
                    },
                    contentColor = if (episode.isWatched) {
                        colorScheme.onPrimary
                    } else {
                        colorScheme.onSecondaryContainer
                    },
                    border = BorderStroke(1.5.dp, colorScheme.surfaceBright),
                    modifier = Modifier.align(Alignment.BottomEnd),
                ) {
                    Box(modifier = Modifier.size(21.dp), contentAlignment = Alignment.Center) {
                        if (episode.isWatched) {
                            Symbol(
                                icon = R.drawable.check_24px,
                                desc = null,
                                color = colorScheme.onPrimary,
                                size = 13.dp,
                            )
                        } else {
                            Text(
                                text = episode.episode_number.toString(),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = Spacing.md, top = Spacing.md, bottom = Spacing.md),
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
