package com.pranshulgg.watchmaster.feature.tv.detail.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import com.pranshulgg.watchmaster.core.ui.components.ActionBottomSheet
import com.pranshulgg.watchmaster.data.local.entity.TvEpisodeEntity
import com.pranshulgg.watchmaster.feature.tv.detail.components.EpisodeInfoSheetContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvDetailsEpisodeInfoSheet(
    episode: TvEpisodeEntity?,
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    sheetState: SheetState
) {

    if (show && episode != null)
        ActionBottomSheet(
            sheetState = sheetState,
            onCancel = { onDismiss() },
            onConfirm = { onConfirm() },
            confirmBtnMaxWidth = true,
            confirmText = if (episode.isWatched) "Marcar como no visto" else "Marcar como visto"
        ) {
            EpisodeInfoSheetContent(episode)
        }
}