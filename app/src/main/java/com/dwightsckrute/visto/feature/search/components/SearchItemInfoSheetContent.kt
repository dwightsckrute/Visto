package com.dwightsckrute.visto.feature.search.components

import com.dwightsckrute.visto.core.utils.posterUrl

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.feature.search.SearchItem
import com.dwightsckrute.visto.core.network.TvSeasonDto
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.components.media.MediaChip
import com.dwightsckrute.visto.core.ui.components.media.PosterBox
import com.dwightsckrute.visto.core.ui.components.media.PosterPlaceholder
import com.dwightsckrute.visto.data.getMovieGenreNames
import com.dwightsckrute.visto.feature.search.ui.SearchItemInfoSeasonSection
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import com.dwightsckrute.visto.core.ui.localization.localized

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchItemInfoSheetContent(
    item: SearchItem,
    seasonLoading: Boolean = true,
    seasonData: List<TvSeasonDto>,
    onSelectedSeason: (List<TvSeasonDto>?) -> Unit,
    watchlistViewModel: WatchlistViewModel
) {

    val poster = item.posterPath?.let {
        posterUrl(it)
    }

    val genreList = item.genreIds?.let { getMovieGenreNames(it) }



    Column(
        Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterBox(
                posterUrl = poster,
                apiPath = item.posterPath,
                width = 80.dp,
                height = 120.dp,
                cornerRadius = ShapeRadius.Large,
                placeholder = { PosterPlaceholder(size = 0.6f) }
            )
            Spacer(Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(
                    space = 6.dp,
                )
            ) {
                Text(
                    item.title,
                    fontWeight = FontWeight.W900,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 19.sp,
                )

                Row {

                    MediaChip(
                        if (item.releaseDate == "" || item.releaseDate == null) {
                            localized("Sin fecha", "No date")
                        } else {
                            // Buscado y no recortado: los cuatro primeros caracteres son el año
                            // en "2016-09-06" y el principio del apellido en "Aramburu · 2016".
                            item.releaseDate.releaseYear() ?: localized("Sin fecha", "No date")
                        },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    // Solo si hay nota: un libro no la trae y aquí se enseñaba una estrella con
                    // la palabra "null" al lado, igual que pasaba en la lista de resultados.
                    item.avg_rating?.takeIf { it > 0.0 }?.let { rating ->
                        Spacer(Modifier.width(3.dp))
                        MediaChip(
                            "%.1f".format(rating),
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            shapeRadius = ShapeRadius.Small,
                            icon = R.drawable.star_24px
                        )
                    }
                }

            }
        }
        Spacer(Modifier.height(12.dp))

        FlowRow(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            genreList?.forEach {
                MediaChip(
                    it,
                    containerColor = MaterialTheme.colorScheme.surfaceBright,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }

        }
        if (!seasonLoading && item.mediaType == "tv") {
            Spacer(Modifier.height(12.dp))
            SearchItemInfoSeasonSection(seasonData, onSelectedSeason, watchlistViewModel, item.id)
        } else if (seasonLoading && item.mediaType == "tv") {
            CircularProgressIndicator()
        }
        Spacer(Modifier.height(12.dp))

        item.overview?.let {
            Text(
                text = localized("Sinopsis", "Synopsis"),
                modifier = Modifier.padding(bottom = 2.dp),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.W700
            )
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 15,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))

        }
    }

}

/** El primer grupo de cuatro cifras que parezca un año, venga la fecha como venga. */
private fun String.releaseYear(): String? =
    Regex("(1[5-9]\\d{2}|2\\d{3})").find(this)?.value
