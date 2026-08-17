package com.pranshulgg.watchmaster.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.core.ui.components.media.PosterBox
import com.pranshulgg.watchmaster.core.ui.components.media.PosterPlaceholder
import com.pranshulgg.watchmaster.core.ui.theme.ShapeRadius
import com.pranshulgg.watchmaster.core.ui.theme.Spacing
import com.pranshulgg.watchmaster.feature.home.HomeMediaItem

/**
 * "Continuar viendo" como carrusel de Material 3 Expressive.
 *
 * Es la única sección del inicio que lo usa, y a propósito: el carrusel da protagonismo a lo que
 * estás viendo ahora mismo y deja el resto de filas como listas tranquilas. Ponerlo en todas
 * convertiría la pantalla en una feria.
 *
 * La carátula manda y el texto va encima con un degradado funcional, no decorativo: sin él, un
 * póster claro se come el título.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContinueWatchingCarousel(
    items: List<HomeMediaItem>,
    onItemClick: (HomeMediaItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val carouselState = rememberCarouselState(itemCount = { items.size })

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        preferredItemWidth = 220.dp,
        itemSpacing = Spacing.sm,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.lg),
    ) { index ->
        val item = items[index]

        Box(
            modifier = Modifier
                .fillMaxSize()
                .maskClip(RoundedCornerShape(ShapeRadius.ExtraLarge))
                .clickable { onItemClick(item) },
        ) {
            PosterBox(
                posterUrl = item.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
                apiPath = item.posterPath,
                fillMaxWidth = true,
                height = 240.dp,
                cornerRadius = ShapeRadius.None,
                placeholder = { PosterPlaceholder(size = 0.5f) },
            )

            // Scrim funcional: garantiza contraste del texto sobre cualquier carátula.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(ShapeRadius.ExtraLarge))
                    .background(
                        Brush.verticalGradient(
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(alpha = .78f),
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                item.progress?.let { progress ->
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = .3f),
                    )
                }
            }
        }
    }
}
