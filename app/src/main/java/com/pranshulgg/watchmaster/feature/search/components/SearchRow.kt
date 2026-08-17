package com.pranshulgg.watchmaster.feature.search.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.feature.search.SearchItem
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.components.media.PosterBox
import com.pranshulgg.watchmaster.core.ui.components.media.PosterPlaceholder
import com.pranshulgg.watchmaster.core.ui.theme.ShapeRadius
import com.pranshulgg.watchmaster.core.ui.localization.localized

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchRow(
    item: SearchItem,
    index: Int,
    results: List<SearchItem>,
    modifier: Modifier = Modifier,
    onSearchItemClick: () -> Unit,
) {

    val isOnly = results.singleOrNull() == item
    val isFirst = index == 0
    val isLast = index == results.lastIndex

    val shape = when {
        isOnly -> RoundedCornerShape(16.dp)
        isFirst -> RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp
        )

        isLast -> RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )

        else -> RoundedCornerShape(4.dp)
    }

    val poster = item.posterPath?.let {
        "https://image.tmdb.org/t/p/w154$it"
    }

    val titleMaxLines = 2
    val overviewMaxLines = remember { mutableIntStateOf(1) }


    Surface(
        shape = shape,
        modifier = modifier
            .clip(shape)
            .clickable { onSearchItemClick() },
        color = MaterialTheme.colorScheme.surfaceBright

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clipToBounds()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 12.dp,
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PosterBox(
                posterUrl = poster,
                apiPath = item.posterPath,
                width = 80.dp,
                height = 120.dp,
                cornerRadius = ShapeRadius.None,
                placeholder = { PosterPlaceholder(size = 0.6f) }
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = item.title,
                    // titleMedium de la escala del tema en vez de 17.sp con peso 900: ese peso
                    // no existe en Google Sans Flex tal como está configurada y el tamaño se
                    // salía de la escala, así que los títulos no acompañaban al resto.
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = titleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { textLayoutResult: TextLayoutResult ->
                        overviewMaxLines.intValue = if (textLayoutResult.lineCount == 1) 2 else 1
                    }
                )
                Text(
                    item.overview?.takeIf { it.isNotBlank() }
                        ?: localized("No se encontró ninguna sinopsis", "No summary was found"),
                    maxLines = overviewMaxLines.intValue,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(5.dp))
                // FlowRow y no Row: con font scale alto o títulos largos en español, tres
                // distintivos no caben en una línea y deben envolver en vez de recortarse.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    MediaTypeChip(mediaType = item.mediaType)
                    StarDateChip(
                        if (item.releaseDate == "" || item.releaseDate == null) {
                            localized("Sin fecha", "No date")
                        } else {
                            item.releaseDate.take(
                                4
                            )
                        },
                        isDate = true,
                    )
                    if (item.mediaType != "person") {
                        StarDateChip("%.1f".format(item.avg_rating))
                    }
                }
            }
        }
    }
}

/**
 * Distintivo del tipo de contenido de un resultado.
 *
 * Antes el tipo solo se insinuaba con un icono dentro del chip del año, fácil de pasar por alto,
 * y `person` caía en la rama `else`, así que una persona se mostraba con icono de película.
 * Ahora cada tipo tiene etiqueta, icono y pareja de color propias.
 */
@Composable
private fun MediaTypeChip(mediaType: String?) {
    val (containerColor, contentColor) = when (mediaType) {
        "tv" -> MaterialTheme.colorScheme.tertiaryContainer to
            MaterialTheme.colorScheme.onTertiaryContainer

        "person" -> MaterialTheme.colorScheme.secondaryContainer to
            MaterialTheme.colorScheme.onSecondaryContainer

        else -> MaterialTheme.colorScheme.primaryContainer to
            MaterialTheme.colorScheme.onPrimaryContainer
    }
    val icon = when (mediaType) {
        "tv" -> R.drawable.tv_24px
        "person" -> R.drawable.groups_2_24px
        else -> R.drawable.movie_24px
    }
    val label = when (mediaType) {
        "tv" -> localized("Serie", "TV show")
        "person" -> localized("Persona", "Person")
        else -> localized("Película", "Movie")
    }

    Chip(
        text = label,
        icon = icon,
        // El texto ya nombra el tipo, así que el icono es decorativo: describirlo otra vez
        // haría que TalkBack lo leyese por duplicado.
        iconDesc = null,
        containerColor = containerColor,
        contentColor = contentColor,
    )
}

@Composable
private fun StarDateChip(
    text: String,
    isDate: Boolean = false,
) {
    Chip(
        text = text,
        icon = if (isDate) R.drawable.date_range_24px else R.drawable.star_24px,
        iconDesc = if (isDate) {
            localized("Año", "Year")
        } else {
            localized("Valoración", "Rating")
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun Chip(
    text: String,
    icon: Int,
    iconDesc: String?,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        color = containerColor,
        shape = CircleShape,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 6.dp, end = 8.dp)
        ) {
            Symbol(
                icon = icon,
                desc = iconDesc,
                color = contentColor,
                size = 16.dp,
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
