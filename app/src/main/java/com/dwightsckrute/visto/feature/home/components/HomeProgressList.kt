package com.dwightsckrute.visto.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dwightsckrute.visto.core.ui.components.media.PosterBox
import com.dwightsckrute.visto.core.ui.components.media.PosterPlaceholder
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.theme.Spacing
import com.dwightsckrute.visto.core.utils.posterUrl
import com.dwightsckrute.visto.feature.home.HomeMediaItem

/**
 * Lo que tienes empezado, en vertical y entero.
 *
 * Sustituye al carrusel. Un `HorizontalMultiBrowseCarousel` recorta las tarjetas de los extremos
 * a propósito —así insinúa que hay más— y el precio es que la mitad de lo que enseña no se puede
 * leer ni tocar bien: había que desplazar para averiguar qué era cada cosa. Aquí cabe todo lo
 * empezado a la vez, con el título completo y sitio para decir por dónde vas.
 *
 * Y la portada sale del sitio común: el carrusel componía la URL de TMDB a mano, así que la de un
 * libro llegaba mal formada.
 */
@Composable
fun HomeProgressList(
    items: List<HomeMediaItem>,
    onItemClick: (HomeMediaItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items.forEach { item ->
            HomeProgressRow(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
private fun HomeProgressRow(item: HomeMediaItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(ShapeRadius.ExtraLarge),
        color = MaterialTheme.colorScheme.surfaceBright,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterBox(
                posterUrl = posterUrl(item.posterPath, "w342"),
                apiPath = item.posterPath,
                width = 60.dp,
                height = 90.dp,
                cornerRadius = ShapeRadius.Large,
                placeholder = { PosterPlaceholder(size = 0.6f) },
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = localized(item.subtitleEs, item.subtitleEn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // La barra solo donde significa algo. Una temporada sabe por qué episodio vas;
                // una película o un libro no llevan esa cuenta, y una barra vacía ahí diría que
                // no has empezado algo que sí empezaste.
                item.progress?.let { progress ->
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                    )
                }
            }
        }
    }
}
