package com.dwightsckrute.visto.feature.lists.listEntry.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.EmptyContainerPlaceholder
import com.dwightsckrute.visto.core.ui.components.Gap
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListEntrySheetContent(
    items: List<WatchlistItemEntity>,
    onMovieSelect: (WatchlistItemEntity) -> Unit,
    selectedMovies: List<WatchlistItemEntity>,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        if (items.isEmpty()) {
            Column {
                Gap(15.dp)
                EmptyContainerPlaceholder(
                    text = localized("No se encontró contenido", "Nothing found"),
                    icon = R.drawable.movie_info_24px,
                    size = 0.8f
                )
            }
        } else {

            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item { Gap(16.dp) }
                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    ListEntrySelectableMediaItem(
                        item,
                        index,
                        items,
                        onMovieSelect,
                        selectedMovies
                    )
                }
                item {
                    Gap(WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 100.dp)
                }
            }
        }

        HorizontalFloatingToolbar(
            expanded = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    modifier = Modifier
                        .widthIn(min = 120.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    onClick = {
                        onCancel()
                    },
                ) {
                    Text(
                        localized("Cancelar", "Cancel"),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 16.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Button(
                    onClick = {
                        onConfirm()
                    },
                    modifier = Modifier
                        .widthIn(min = 120.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Text(
                        localized("Guardar", "Save"),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

}
