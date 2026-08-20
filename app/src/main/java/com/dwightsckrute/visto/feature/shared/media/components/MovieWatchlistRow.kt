package com.dwightsckrute.visto.feature.shared.media.components

import com.dwightsckrute.visto.core.utils.SharedPosterSize
import com.dwightsckrute.visto.core.ui.navigation.sharedPoster
import com.dwightsckrute.visto.core.ui.navigation.posterSharedKey
import com.dwightsckrute.visto.core.utils.posterUrl

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.dwightsckrute.visto.core.ui.components.listItemShape
import com.dwightsckrute.visto.core.ui.components.media.PosterBox
import com.dwightsckrute.visto.core.ui.components.media.PosterPlaceholder
import com.dwightsckrute.visto.core.ui.navigation.NavRoutes
import com.dwightsckrute.visto.data.repository.MEDIA_TYPE_BOOK
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.feature.shared.media.ui.watchstatus.asStatusDates
import com.dwightsckrute.visto.feature.shared.media.ui.watchstatus.toWatchListItemStatusUiPill
import com.dwightsckrute.visto.core.ui.localization.localized


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MovieWatchlistRow(
    item: WatchlistItemEntity,
    index: Int,
    items: List<WatchlistItemEntity>,
    navController: NavController,
    onLongActionMovieRequest: () -> Unit
) {

    val isOnly = items.singleOrNull() == item
    val isFirst = index == 0
    val isLast = index == items.lastIndex

    val titleMaxLines = 2
    val overviewMaxLines = remember { mutableIntStateOf(1) }
    var isReady by remember { mutableStateOf(false) }


    val poster = item.posterPath?.let {
        posterUrl(it, SharedPosterSize)
    }

    val status = item.status.toWatchListItemStatusUiPill(item.asStatusDates())

    val shape = listItemShape(isOnly, isFirst, isLast)

    Surface(
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .alpha(if (isReady) 1f else 0f)
            .combinedClickable(
                onClick = {
                    // La misma fila sirve para películas y libros; solo cambia a dónde lleva.
                    navController.navigate(
                        if (item.mediaType == MEDIA_TYPE_BOOK) {
                            NavRoutes.bookDetail(item.id)
                        } else {
                            NavRoutes.movieDetail(item.id)
                        }
                    )
                },
                onLongClick = {
                    onLongActionMovieRequest()
                }
            ),
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
            Box(
                modifier = Modifier
                    .size(80.dp, height = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                PosterBox(
                    posterUrl = poster,
                    apiPath = item.posterPath,
                    width = 80.dp,
                    height = 120.dp,
                    progressIndicatorSize = 40.dp,
                    cornerRadius = ShapeRadius.None,
                    // El origen: esta imagen es la que crece hasta la ficha.
                    modifier = Modifier.sharedPoster(posterSharedKey(item.id))
                )
            }
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
                    onTextLayout = { result ->
                        val newLines = if (result.lineCount == 1) 2 else 1
                        if (overviewMaxLines.intValue != newLines) {
                            overviewMaxLines.intValue = newLines
                            isReady = true
                        } else {
                            isReady = true
                        }
                    }

                )
                Text(
                    item.overview ?: localized("No se encontró ninguna sinopsis", "No summary was found"),
                    maxLines = overviewMaxLines.intValue,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(5.dp))
                WatchListStatusPill(
                    status.containerColor,
                    status.contentColor,
                    status.statusLabel,
                    item.status,
                    item.userRating,
                )
            }
        }
    }


}


