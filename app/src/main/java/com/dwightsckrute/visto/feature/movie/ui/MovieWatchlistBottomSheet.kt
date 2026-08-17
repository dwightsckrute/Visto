package com.dwightsckrute.visto.feature.movie.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.components.ActionBottomSheet
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.feature.movie.MovieHomeViewModel
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import com.dwightsckrute.visto.feature.shared.media.components.WatchlistMediaSheetContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieWatchlistBottomSheet(
    show: Boolean = false,
    sheetState: SheetState,
    watchlistItem: WatchlistItemEntity?,
    onDismiss: () -> Unit,
    viewModel: WatchlistViewModel,
    movieHomeViewModel: MovieHomeViewModel
) {

    if (show && watchlistItem != null)
        ActionBottomSheet(
            showActions = false,
            sheetState = sheetState,
            onCancel = { onDismiss() },
            onConfirm = { },
        ) { hide ->
            WatchlistMediaSheetContent(
                id = watchlistItem.id,
                mediaTitle = watchlistItem.title,
                status = watchlistItem.status,
                onUpdateRating = {
                    movieHomeViewModel.showUpdateRatingDialog(
                        originalRating = watchlistItem.userRating?.toFloat() ?: 0f
                    )
                },
                onWatchStatus = { status ->
                    if (status != WatchStatus.WATCHING) {
                        movieHomeViewModel.showStatusConfirmationDialog()
                    } else {
                        movieHomeViewModel.showRatingDialog()
                    }
                },
                onDelete = {
                    movieHomeViewModel.showConfirmationDialog()
                },
                onDismiss = { hide() },
                viewModel = viewModel,
                isPinned = watchlistItem.isPinned,
                onChangeFinishData = {
                    movieHomeViewModel.showDatePicker()
                }
            )
        }
}