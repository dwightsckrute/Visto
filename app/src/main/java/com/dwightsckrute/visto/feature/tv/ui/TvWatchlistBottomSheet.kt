package com.dwightsckrute.visto.feature.tv.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.components.ActionBottomSheet
import com.dwightsckrute.visto.data.local.entity.SeasonEntity
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.feature.movie.MovieHomeViewModel
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import com.dwightsckrute.visto.feature.shared.media.components.WatchlistMediaSheetContent
import com.dwightsckrute.visto.feature.tv.TvHomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvWatchlistBottomSheet(
    show: Boolean = false,
    sheetState: SheetState,
    seasonItem: SeasonEntity?,
    watchlistItem: WatchlistItemEntity?,
    onDismiss: () -> Unit,
    viewModel: WatchlistViewModel,
    tvHomeViewModel: TvHomeViewModel,
) {

    val mediaTitle =
        if (seasonItem != null) "${watchlistItem?.title}: ${seasonItem.name}" else watchlistItem?.title
    val mediaOptions = !(watchlistItem?.mediaType == "tv" && seasonItem == null)

    if (show && watchlistItem != null)
        ActionBottomSheet(
            showActions = false,
            sheetState = sheetState,
            onCancel = { onDismiss() },
            onConfirm = { },
        ) { hide ->
            WatchlistMediaSheetContent(
                id = seasonItem?.seasonId ?: watchlistItem.id,
                mediaTitle = mediaTitle,
                status = seasonItem?.status ?: watchlistItem.status,
                isTv = true,
                onUpdateRating = {
                    tvHomeViewModel.showUpdateRatingDialog(
                        originalRating = seasonItem?.seasonUserRating?.toFloat() ?: 0f
                    )
                },
                onWatchStatus = { status ->
                    if (status != WatchStatus.WATCHING) {
                        tvHomeViewModel.showStatusConfirmationDialog()
                    } else {
                        tvHomeViewModel.showRatingDialog()
                    }
                },
                onDelete = {
                    tvHomeViewModel.showConfirmationDialog()
                },
                onDeleteSeries = { id ->
                    tvHomeViewModel.showConfirmationDialog(id)
                },
                onDismiss = { hide() },
                viewModel = viewModel,
                isPinned = watchlistItem.isPinned,
                mediaOptions = mediaOptions,
                onChangeFinishData = {
                    tvHomeViewModel.showDatePicker()
                }
            )
        }
}