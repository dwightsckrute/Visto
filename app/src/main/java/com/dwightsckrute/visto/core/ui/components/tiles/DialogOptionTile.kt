package com.dwightsckrute.visto.core.ui.components.tiles

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dwightsckrute.visto.core.ui.components.ActionBottomSheet
import com.dwightsckrute.visto.core.ui.components.pressScale

/** A setting row whose choices open in the same thumb-friendly bottom sheet as other actions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DialogOptionTile(
    headline: String,
    description: String? = null,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String = { it.toString() },
    leading: @Composable (() -> Unit)? = null,
    shapes: RoundedCornerShape,
    dialogTitle: String? = null,
    itemBgColor: Color,
) {
    var showSheet by remember { mutableStateOf(false) }
    val interactions = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactions),
        shape = shapes,
    ) {
        ListItem(
            modifier = Modifier.clickable(
                interactionSource = interactions,
                indication = LocalIndication.current,
            ) { showSheet = true },
            colors = ListItemDefaults.colors(containerColor = itemBgColor),
            leadingContent = leading,
            headlineContent = { Text(headline) },
            supportingContent = {
                if (description != null) {
                    Text(description)
                } else {
                    selectedOption?.let {
                        Text(
                            text = optionLabel(it),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            },
        )
    }

    if (showSheet) {
        var tempSelection by remember { mutableStateOf(selectedOption) }
        val listState = rememberLazyListState()
        val sheetState = rememberModalBottomSheetState()
        val showTopDivider by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
            }
        }
        val showBottomDivider by remember {
            derivedStateOf {
                val info = listState.layoutInfo
                val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
                info.totalItemsCount > 0 && lastVisible < info.totalItemsCount - 1
            }
        }

        ActionBottomSheet(
            sheetState = sheetState,
            onCancel = { showSheet = false },
            onConfirm = { tempSelection?.let(onOptionSelected) },
        ) { _ ->
            Column {
                Text(
                    text = dialogTitle ?: headline,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                    ) {
                        items(options) { option ->
                            Row(
                                modifier = Modifier
                                    .clickable { tempSelection = option }
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = option == tempSelection,
                                    onClick = { tempSelection = option },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = optionLabel(option),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showTopDivider,
                        modifier = Modifier.align(Alignment.TopCenter),
                    ) {
                        HorizontalDivider(Modifier.fillMaxWidth())
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showBottomDivider,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    ) {
                        HorizontalDivider(Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
