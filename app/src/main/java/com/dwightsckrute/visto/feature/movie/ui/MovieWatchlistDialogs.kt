package com.dwightsckrute.visto.feature.movie.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.snackbar.SnackbarManager
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.feature.movie.MovieHomeViewModel
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import com.dwightsckrute.visto.feature.shared.media.components.MediaConfirmationDialogContent
import com.dwightsckrute.visto.feature.shared.media.components.MediaRatingDialogContent
import com.dwightsckrute.visto.feature.shared.media.ui.watchstatus.confirmAction
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.localization.AppLanguage


@Composable
fun MovieWatchlistConfirmationDialog(
    watchlistViewModel: WatchlistViewModel,
    movieHomeViewModel: MovieHomeViewModel,
    watchlistItem: WatchlistItemEntity?,
) {
    val uiState = movieHomeViewModel.uiState.value

    watchlistItem?.let { item ->
        MediaConfirmationDialogContent(
            uiState.showConfirmationDialog,
            movieHomeViewModel::hideConfirmationDialog,
            onConfirm = {
                watchlistViewModel.delete(item.id)
                SnackbarManager.show(AppLanguage.text("Película eliminada: ${item.title}", "Movie deleted: ${item.title}"))
            }
        )
    }
}

@Composable
fun MovieWatchlistRatingDialog(
    watchlistViewModel: WatchlistViewModel,
    movieHomeViewModel: MovieHomeViewModel,
    watchlistItem: WatchlistItemEntity?,
) {
    val uiState = movieHomeViewModel.uiState.value

    watchlistItem?.let { item ->
        MediaRatingDialogContent(
            uiState.showRatingDialog,
            movieHomeViewModel::hideRatingDialog,
            isUpdateRating = uiState.isUpdateRating,
            originalRating = uiState.originalRating,
            onConfirm = { rating ->
                watchlistViewModel.setUserRating(item.id, rating)
                watchlistViewModel.finish(item.id)
                if (uiState.isUpdateRating) SnackbarManager.show(AppLanguage.text("Valoración actualizada", "Rating updated"))
            }
        )
    }
}


@Composable
fun MovieStatusWatchlistConfirmationDialog(
    watchlistViewModel: WatchlistViewModel,
    movieHomeViewModel: MovieHomeViewModel,
    watchlistItem: WatchlistItemEntity?,
) {
    val uiState = movieHomeViewModel.uiState.value

    watchlistItem?.let { item ->
        MediaConfirmationDialogContent(
            uiState.showStatusConfirmationDialog,
            movieHomeViewModel::hideStatusConfirmationDialog,
            status = item.status,
            onConfirm = {
                item.status.confirmAction(
                    start = {
                        watchlistViewModel.start(item.id)
                    },
                    finish = {
                    },
                    reset = {
                        watchlistViewModel.reset(item.id)
                    }
                )
            }
        )
    }
}
