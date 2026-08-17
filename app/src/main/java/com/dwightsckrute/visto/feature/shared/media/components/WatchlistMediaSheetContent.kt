package com.dwightsckrute.visto.feature.shared.media.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.components.ActionBottomSheet
import com.dwightsckrute.visto.core.ui.components.SettingSection
import com.dwightsckrute.visto.core.ui.components.SettingTile
import com.dwightsckrute.visto.core.ui.components.SettingsTileIcon
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import com.dwightsckrute.visto.feature.shared.media.ui.watchstatus.actionLabel
import com.dwightsckrute.visto.feature.shared.media.ui.watchstatus.confirmAction
import com.dwightsckrute.visto.core.ui.localization.localized

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistMediaSheetContent(
    id: Long,
    isTv: Boolean = false,
    isPinned: Boolean = false,
    mediaTitle: String?,
    status: WatchStatus,
    onUpdateRating: () -> Unit,
    onWatchStatus: (WatchStatus) -> Unit,
    onDeleteSeries: (seriesId: Long) -> Unit = {},
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    onChangeFinishData: () -> Unit,
    viewModel: WatchlistViewModel,
    mediaOptions: Boolean = true
) {


    SettingSection(
        isModalOption = true,
        title = mediaTitle,
        tiles = listOf(

            if (mediaOptions) {
                SettingTile.ActionTile(
                    leading = { SettingsTileIcon(R.drawable.play_arrow_24px) },
                    title = status.actionLabel,
                    onClick = {
                        onWatchStatus(status)
                        onDismiss()
                    }
                )
            } else null,

            if (status == WatchStatus.WATCHING && mediaOptions) {
                SettingTile.ActionTile(
                    leading = { SettingsTileIcon(R.drawable.pause_24px) },
                    title = "Marcar como interrumpida",
                    onClick = {
                        if (isTv) {
                            viewModel.interruptSeason(id)
                        } else {
                            viewModel.interrupt(id)
                        }
                        onDismiss()
                    }
                )
            } else null,

            SettingTile.ActionTile(
                leading = { SettingsTileIcon(R.drawable.delete_24px) },
                title = if ((isTv && !mediaOptions)) "Eliminar serie" else localized("Eliminar", "Delete"),
                onClick = {
                    if ((isTv && !mediaOptions)) {
                        onDeleteSeries(id)
                    } else {
                        onDelete()
                    }
                    onDismiss()
                }
            ),

            if (!isTv || !mediaOptions) {
                SettingTile.ActionTile(
                    leading = { SettingsTileIcon(R.drawable.keep_24px) },
                    title = if (isPinned) "Desfijar" else "Fijar",
                    onClick = {
                        viewModel.setPinned(id, !isPinned)
                        onDismiss()
                    }
                )
            } else null,

            if (status == WatchStatus.FINISHED && mediaOptions) {
                SettingTile.ActionTile(
                    leading = { SettingsTileIcon(R.drawable.star_24px) },
                    title = localized("Actualizar valoración", "Update rating"),
                    onClick = {
                        onUpdateRating()
                        onDismiss()
                    }
                )
            } else null,

            if (status == WatchStatus.FINISHED) {
                SettingTile.ActionTile(
                    leading = { SettingsTileIcon(R.drawable.date_range_24px) },
                    title = localized("Cambiar fecha de finalización", "Change finish date"),
                    onClick = {
                        onChangeFinishData()
                        onDismiss()
                    }
                )
            } else null
        )
    )


}

