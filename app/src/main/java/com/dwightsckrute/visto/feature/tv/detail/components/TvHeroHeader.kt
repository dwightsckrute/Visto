package com.dwightsckrute.visto.feature.tv.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dwightsckrute.visto.core.utils.posterUrl
import com.dwightsckrute.visto.core.utils.SharedPosterSize
import com.dwightsckrute.visto.core.ui.navigation.sharedPoster
import com.dwightsckrute.visto.core.ui.navigation.posterSharedKey
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.AppPill
import com.dwightsckrute.visto.core.ui.components.PillSize
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.components.media.MediaDetailsScreenHeader
import com.dwightsckrute.visto.core.ui.components.media.PosterBox
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.data.getTvGenreNames
import com.dwightsckrute.visto.data.local.entity.SeasonEntity
import com.dwightsckrute.visto.data.local.entity.TvBundle
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TvHeroHeader(
    tv: TvBundle,
    watchlistItem: WatchlistItemEntity?,
    navController: NavController,
    isFinished: Boolean,
    seasonItem: SeasonEntity,
    userRating: Double? = 0.0,
    onUpdateRating: (Double) -> Unit
) {

    val genre = tv.genres

    val genreList = getTvGenreNames(genre.map { it.id })


    Box(modifier = Modifier.fillMaxWidth()) {

        AsyncImage(
            model = "https://image.tmdb.org/t/p/w1280${tv.backdrop_path}",
            contentDescription = tv.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp),
            contentScale = ContentScale.Crop
        )

        MediaDetailsScreenHeader(
            navController,
            isFinished,
            userRating,
            onUpdateRating = onUpdateRating,
            isTv = true,
            seasonUserRating = seasonItem.seasonUserRating
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.3f to MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                            1.0f to MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 1f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {

            PosterBox(
                posterUrl = posterUrl(seasonItem.posterPath, SharedPosterSize),
                apiPath = seasonItem.posterPath,
                width = 120.dp,
                height = 180.dp,
                cornerRadius = ShapeRadius.Large,
                progressIndicatorSize = 40.dp,
                modifier = Modifier.sharedPoster(posterSharedKey(seasonItem.seasonId))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = tv.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        shadow = Shadow(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            offset = Offset(2f, 2f),
                            blurRadius = 6f
                        )
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    lineHeight = 30.sp,
                    overflow = TextOverflow.Ellipsis,

                    )
                Text(
                    text = "${seasonItem.name} • ${seasonItem.airDate?.take(4) ?: "—"}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    genreList.forEach { text ->
                        AppPill(
                            label = text,
                            container = MaterialTheme.colorScheme.tertiaryContainer,
                            onContainer = MaterialTheme.colorScheme.onTertiaryContainer,
                            size = PillSize.Small,
                        )
                    }
                    AppPill(
                        label = "%.1f".format(watchlistItem?.avgRating),
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
