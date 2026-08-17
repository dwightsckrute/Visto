package com.pranshulgg.watchmaster.feature.tv.detail.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Es la vista para leer: cabe el título entero y la sinopsis, cosa que ni el carrusel ni la línea
 * de tiempo permiten. Reutiliza `listItemShape`, así que las esquinas del grupo se comportan igual
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

/**
 * El fotograma, en la proporción en la que se rodó.
 *
 * Antes iba pegado al borde de la fila y con las esquinas vivas, heredado de cuando la miniatura
 * hacía de cabecera de la fila. Recortado, separado del borde y con la misma curva que el resto de
 * la aplicación deja de parecer un trozo de imagen y pasa a parecer una fotografía.
 */
private val StillWidth = 100.dp
private val StillHeight = 56.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EpisodeRow(
    episode: TvEpisodeEntity,
    shape: androidx.compose.ui.graphics.Shape,
    onToggle: () -> Unit,
    onInfo: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

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
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "episode-row-chevron",
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
            modifier = Modifier
                .padding(Spacing.sm)
                // El desplegado no es un salto: la fila crece hasta la sinopsis entera con la
                // misma curva que el resto de la pantalla.
                .animateContentSize(motionScheme.defaultSpatialSpec()),
            // Justo lo necesario. Entre el fotograma y el botón de desplegar, la columna de texto
            // se queda con lo que sobra, y con separaciones generosas la sinopsis se cortaba a
            // media frase sin adelantar nada.
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            // Plegada, la fila la manda el fotograma y el texto se centra contra él. Desplegada
            // manda el texto, y un fotograma flotando a media altura de un párrafo largo queda
            // suelto, así que sube a la primera línea.
            verticalAlignment = if (expanded) Alignment.Top else Alignment.CenterVertically,
        ) {
            Box {
                PosterBox(
                    posterUrl = episode.still_path?.let { "https://image.tmdb.org/t/p/w500$it" },
                    apiPath = episode.still_path,
                    width = StillWidth,
                    height = StillHeight,
                    cornerRadius = ShapeRadius.Medium,
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = episode.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                // La sinopsis nunca desaparece del todo. Escondida entera, la fila plegada era un
                // título solo contra un fotograma de sesenta y cinco puntos, con un hueco debajo
                // que no decía nada; una línea equilibra la fila y además adelanta de qué va el
                // episodio, que es justo lo que se quiere saber antes de decidir si desplegarlo.
                Text(
                    text = episode.overview?.takeIf { it.isNotBlank() }
                        ?: localized("Sin sinopsis", "No summary"),
                    style = MaterialTheme.typography.bodySmall,
                    // Un poco más de interlínea que la del estilo: la sinopsis desplegada son
                    // seis o siete líneas seguidas y el texto apretado se lee peor.
                    lineHeight = 18.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Botón aparte y no toque en la fila: la fila ya marca como visto, que es lo que se
            // hace aquí cien veces por temporada. Leer la sinopsis es lo excepcional, así que es
            // lo que paga el coste de tener su propio destino.
            IconButton(onClick = { expanded = !expanded }) {
                Symbol(
                    icon = R.drawable.keyboard_arrow_down_24px,
                    desc = if (expanded) {
                        localized("Ocultar la sinopsis", "Hide the summary")
                    } else {
                        localized("Ver la sinopsis", "Show the summary")
                    },
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevronRotation),
                )
            }
        }
    }
}
