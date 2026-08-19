package com.dwightsckrute.visto.feature.lists

import com.dwightsckrute.visto.core.utils.posterUrl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.AvatarIcon
import com.dwightsckrute.visto.core.ui.components.AvatarMonogram
import com.dwightsckrute.visto.core.ui.components.listItemShape
import com.dwightsckrute.visto.core.ui.components.media.PosterBox
import com.dwightsckrute.visto.data.local.entity.CustomListEntity
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.data.local.mapper.toIcon
import com.dwightsckrute.visto.feature.shared.media.components.MediaListsRow
import com.dwightsckrute.visto.feature.shared.media.components.tv.TvWatchlistRow
import kotlin.collections.orEmpty
import com.dwightsckrute.visto.core.ui.localization.localized

@Composable
fun ListsScreenContent(
    customLists: List<CustomListEntity>,
    onClick: (Long) -> Unit,
    watchlistItems: List<WatchlistItemEntity>
) {


    val unPinnedLists = customLists.filter { !it.isPinned }
    val pinnedLists = customLists.filter { it.isPinned }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {


        if (pinnedLists.isNotEmpty()) {
            item {
                Text(
                    text = "Fijado",
                    modifier = Modifier.padding(bottom = 5.dp, start = 3.dp),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(
                pinnedLists,
                key = { _, item -> item.id }) { index, item ->
                val watchlistItemsFiltered = watchlistItems.filter { item.ids.contains(it.id) }
                ListItemRow(watchlistItemsFiltered, pinnedLists, index, item, onClick)
            }
        }

        if (unPinnedLists.isNotEmpty() && pinnedLists.isNotEmpty()) {
            item {
                Text(
                    text = "Otros",
                    modifier = Modifier.padding(bottom = 5.dp, start = 3.dp, top = 20.dp),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        itemsIndexed(
            unPinnedLists,
            key = { _, item -> item.id }) { index, item ->
            val watchlistItemsFiltered = watchlistItems.filter { item.ids.contains(it.id) }
            ListItemRow(watchlistItemsFiltered, unPinnedLists, index, item, onClick)
        }
    }
}

@Composable
private fun ListItemRow(
    watchlistItemsFiltered: List<WatchlistItemEntity>,
    customLists: List<CustomListEntity>,
    index: Int,
    item: CustomListEntity,
    onClick: (Long) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme


    val isOnly = customLists.singleOrNull() == item
    val isFirst = index == 0
    val isLast = index == customLists.lastIndex

    val shape = listItemShape(isOnly, isFirst, isLast)

    MediaListsRow(
        headline = item.name,
        description = item.description,
        shapes = shape,
        onClick = {
            onClick(item.id)
        },
        leading = {
            Box(modifier = Modifier.padding(top = 0.dp)) {
                AvatarIcon(
                    item.icon?.toIcon() ?: R.drawable.folder_24px,
                    containerColor = colorScheme.tertiaryContainer,
                    contentColor = colorScheme.onTertiaryContainer,
                )
            }
        },

        media = {
            if (!watchlistItemsFiltered.isEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    watchlistItemsFiltered.take(4).forEachIndexed { itemIndex, item ->
                        Box(
                            modifier = Modifier.offset(x = (-12).dp * itemIndex)
                        ) {
                            PosterBox(
                                posterUrl = posterUrl(item.posterPath),
                                apiPath = item.posterPath,
                                width = 40.dp,
                                height = 40.dp,
                                circular = true
                            )
                        }
                    }

                    if (watchlistItemsFiltered.size > 4) {
                        Box(
                            modifier = Modifier.offset(x = (-12).dp * 4)
                        ) {
                            AvatarMonogram(
                                "${watchlistItemsFiltered.size}+",
                                containerColor = colorScheme.surfaceContainer,
                                contentColor = colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                Text(
                    localized("No se encontraron películas", "No movies found"),
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
