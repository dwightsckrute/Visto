package com.dwightsckrute.visto.feature.calendar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.AppPill
import com.dwightsckrute.visto.core.ui.components.PillSize
import com.dwightsckrute.visto.core.ui.components.media.PosterBox
import com.dwightsckrute.visto.core.ui.components.media.PosterPlaceholder
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.theme.Spacing
import com.dwightsckrute.visto.core.utils.posterUrl
import com.dwightsckrute.visto.feature.calendar.EntryType
import com.dwightsckrute.visto.feature.calendar.WatchedEntry

/**
 * Lo que viste un día.
 *
 * Es el final del recorrido: has elegido un día del calendario y esto es la respuesta, así que
 * tiene el tamaño de una respuesta y no el de un renglón de índice. La carátula era de cuarenta y
 * ocho puntos pegada al borde, más pequeña que la del propio calendario que hay justo encima; aquí
 * manda ella, porque es lo que se reconoce antes que cualquier texto.
 */
@Composable
fun WatchedEntryRow(
    entry: WatchedEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEpisode = entry.mediaType == EntryType.EPISODE
    val isTv = entry.mediaType == EntryType.TV || isEpisode
    val isBook = entry.mediaType == EntryType.BOOK
    // Un libro trae la portada ya como URL; componerla otra vez daba una dirección imposible.
    val poster = posterUrl(entry.posterPath, "w342")
    val kind = when {
        isEpisode -> localized("Episodio", "Episode")
        isTv -> localized("Serie", "TV show")
        isBook -> localized("Libro", "Book")
        else -> localized("Película", "Movie")
    }
    val ratingLabel = entry.userRating?.let {
        localized("tu nota, %.1f".format(it), "your rating, %.1f".format(it))
    }
    val description = listOfNotNull(entry.title, entry.subtitle, kind, ratingLabel)
        .joinToString(", ")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clearAndSetSemantics { contentDescription = description },
        shape = RoundedCornerShape(ShapeRadius.ExtraLarge),
        color = MaterialTheme.colorScheme.surfaceBright,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterBox(
                posterUrl = poster,
                apiPath = entry.posterPath,
                width = 72.dp,
                height = 108.dp,
                cornerRadius = ShapeRadius.Large,
                placeholder = { PosterPlaceholder(size = 0.6f) },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                // Qué episodio, bajo el nombre de la serie. Un día con cuatro capítulos seguidos
                // serían cuatro filas idénticas sin esto.
                entry.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppPill(
                        label = kind,
                        icon = when {
                            isBook -> R.drawable.book_24px
                            isTv -> R.drawable.tv_24px
                            else -> R.drawable.movie_24px
                        },
                        container = if (isEpisode) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        onContainer = if (isEpisode) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        size = PillSize.Small,
                    )
                    // La nota solo si la pusiste. Un hueco vacío donde debería ir una nota
                    // sugiere que falta algo, y no falta nada.
                    entry.userRating?.let { rating ->
                        AppPill(
                            label = "%.1f".format(rating),
                            icon = R.drawable.star_24px,
                            container = MaterialTheme.colorScheme.tertiaryContainer,
                            onContainer = MaterialTheme.colorScheme.onTertiaryContainer,
                            size = PillSize.Small,
                        )
                    }
                }
            }
        }
    }
}
