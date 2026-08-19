package com.dwightsckrute.visto.feature.tv.detail.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.ActionBottomSheet
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.components.media.DatePickerSheet
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.theme.Spacing
import com.dwightsckrute.visto.core.utils.formatDate
import com.dwightsckrute.visto.data.local.entity.TvEpisodeEntity
import com.dwightsckrute.visto.feature.tv.detail.components.EpisodeInfoSheetContent

/**
 * La ficha de un episodio, con su fecha de visionado editable.
 *
 * La fecha se puede cambiar porque marcar es lo que se hace al terminar de ver, y no siempre se
 * marca al terminar de ver: los episodios de anoche se marcan por la mañana, y una temporada
 * entera vista hace meses se marca de golpe hoy. Sin poder corregirla, el diario registraría
 * cuándo tocaste el botón en vez de cuándo lo viste, que es justo lo contrario de llevar la
 * cuenta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvDetailsEpisodeInfoSheet(
    episode: TvEpisodeEntity?,
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onChangeWatchedDate: (Long) -> Unit,
    sheetState: SheetState,
) {
    var pickingDate by remember { mutableStateOf(false) }

    if (show && episode != null) {
        ActionBottomSheet(
            sheetState = sheetState,
            onCancel = { onDismiss() },
            onConfirm = { onConfirm() },
            confirmBtnMaxWidth = true,
            confirmText = if (episode.isWatched) {
                localized("Marcar como no visto", "Mark as not watched")
            } else {
                localized("Marcar como visto", "Mark as watched")
            },
        ) {
            Column {
                EpisodeInfoSheetContent(episode)
                // Solo cuando hay fecha que cambiar. En uno sin ver no habría nada que corregir,
                // y ofrecerlo insinuaría que fechar y marcar son cosas distintas.
                episode.watchedDate?.takeIf { episode.isWatched }?.let { watched ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(ShapeRadius.Large),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                            .clip(RoundedCornerShape(ShapeRadius.Large))
                            .clickable { pickingDate = true },
                    ) {
                        Row(
                            modifier = Modifier
                                .heightIn(min = Spacing.minTouchTarget)
                                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Symbol(
                                icon = R.drawable.date_range_24px,
                                desc = null,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = localized("Visto el", "Watched on"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = watched.formatDate(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Text(
                                text = localized("Cambiar", "Change"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    DatePickerSheet(
                        show = pickingDate,
                        initialDate = watched.toEpochMilli(),
                        id = episode.epId,
                        onDateSelected = { selected, _ ->
                            onChangeWatchedDate(selected)
                            pickingDate = false
                        },
                        onDismiss = { pickingDate = false },
                    )
                }
            }
        }
    }
}
