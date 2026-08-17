package com.dwightsckrute.visto.feature.shared.media.components.tv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.prefs.LocalAppPrefs
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.navigation.NavRoutes
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.components.listItemShape
import com.dwightsckrute.visto.core.ui.components.media.PosterBox
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.data.local.entity.SeasonEntity
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.core.ui.localization.localized

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TvWatchlistRow(
    item: WatchlistItemEntity,
    index: Int,
    items: List<WatchlistItemEntity>,
    navController: NavController,
    onLongActionTvSeasonRequest: (SeasonEntity?, WatchlistItemEntity) -> Unit,
    seasons: List<SeasonEntity>,
    allSeasons: List<SeasonEntity>,
) {
    val isOnly = items.singleOrNull() == item
    val isFirst = index == 0
    val isLast = index == items.lastIndex

    val shape = listItemShape(isOnly, isFirst, isLast)


    var expanded by rememberSaveable(item.id) { mutableStateOf(false) }

    val motionScheme = MaterialTheme.motionScheme

    // Cada pestaña recibe solo las temporadas de su estado, así que "Pendientes" enseñaría
    // únicamente las pendientes. Al agrupar, la serie se trata como una unidad y muestra todas
    // sus temporadas con su estado, sin importar la pestaña desde la que se mire.
    val groupSeasons = LocalAppPrefs.current.groupSeasons
    val shownSeasons = if (groupSeasons) allSeasons else seasons

    Surface(
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .combinedClickable(
                onClick = {
                    expanded = !expanded
                },
                onLongClick = {
                    onLongActionTvSeasonRequest(null, item)
                }
            ),
        color = MaterialTheme.colorScheme.surfaceBright
    ) {
        Column() {
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
                    posterUrl = "https://image.tmdb.org/t/p/w154${item.posterPath}",
                    apiPath = item.posterPath,
                    cornerRadius = ShapeRadius.None,
                    progressIndicatorSize = 40.dp
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,

                        )
                    Text(
                        item.overview ?: localized("No se encontró ninguna sinopsis", "No summary was found"),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }


                val rotationAngle by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = motionScheme.slowEffectsSpec(),
                )

                Symbol(
                    R.drawable.keyboard_arrow_down_24px,
                    size = 32.dp,
                    modifier = Modifier.rotate(rotationAngle)
                )

            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = motionScheme.slowSpatialSpec()),
                exit = shrinkVertically(animationSpec = motionScheme.slowSpatialSpec()),
            ) {
                Column(
                    Modifier.padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
                    Spacer(Modifier.height(8.dp))
                    shownSeasons.forEachIndexed { i, season ->
                        val isOnlySeason = shownSeasons.singleOrNull() == season
                        val isFirstSeason = i == 0
                        val isLastSeason = i == shownSeasons.lastIndex

                        val shapeSeasonRow =
                            listItemShape(isOnlySeason, isFirstSeason, isLastSeason)

                        TvWatchlistSeasonRow(
                            season,
                            shapeSeasonRow,
                            navController,
                            onLongActionTvSeasonRequest = {
                                onLongActionTvSeasonRequest(
                                    season,
                                    item
                                )
                            })
                    }
                }

            }
        }
    }


}