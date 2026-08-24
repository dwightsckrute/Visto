package com.dwightsckrute.visto.feature.tv.detail.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.dwightsckrute.visto.core.ui.snackbar.SnackbarManager
import com.dwightsckrute.visto.data.local.entity.SeasonEntity
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import com.dwightsckrute.visto.feature.shared.media.components.MediaConfirmationDialogContent
import com.dwightsckrute.visto.feature.shared.media.components.MediaNoteDialogContent
import com.dwightsckrute.visto.feature.shared.media.components.MediaRatingDialogContent
import com.dwightsckrute.visto.feature.tv.detail.TvDetailsViewModel
import com.dwightsckrute.visto.core.ui.localization.AppLanguage

@Composable
fun TvDetailsNoteDialog(
    viewModel: TvDetailsViewModel,
    watchlistViewModel: WatchlistViewModel,
    season: SeasonEntity?,
) {
    val uiState = viewModel.uiState.value

    season?.let { seasonIt ->
        MediaNoteDialogContent(
            uiState.showNoteDialog,
            uiState.note,
            seasonIt.seasonNotes ?: "",
            viewModel::updateNoteText,
            viewModel::hideNoteDialog,
            onConfirm = {
                watchlistViewModel.setSeasonNote(seasonIt.seasonId, it)
            }
        )
    }
}

@Composable
fun TvDetailsRatingDialog(
    viewModel: TvDetailsViewModel,
    watchlistViewModel: WatchlistViewModel,
    season: SeasonEntity?,
) {
    val uiState = viewModel.uiState.value

    season?.let { seasonIt ->
        MediaRatingDialogContent(
            uiState.showRatingDialog,
            viewModel::hideRatingDialog,
            onConfirm = { rating ->
                watchlistViewModel.setSeasonUserRating(seasonIt.seasonId, rating)
                if (!uiState.ratingOnly) {
                    watchlistViewModel.finishSeason(seasonIt.seasonId, seasonIt.episodeCount)
                }
            }
        )
    }
}


@Composable
fun TvDetailsConfirmationDialog(
    viewModel: TvDetailsViewModel,
    watchlistViewModel: WatchlistViewModel,
    season: SeasonEntity?,
    seasonSize: Int,
    navController: NavController
) {
    val uiState = viewModel.uiState.value

    season?.let { seasonIt ->
        MediaConfirmationDialogContent(
            uiState.showConfirmationDialog,
            viewModel::hideConfirmationDialog,
            onConfirm = {
                if (seasonSize == 1) {
                    watchlistViewModel.delete(seasonIt.showId)
                } else {
                    watchlistViewModel.deleteSeason(seasonIt.seasonId)
                }
                SnackbarManager.show(AppLanguage.text("Temporada eliminada: ${seasonIt.name}", "Season deleted: ${seasonIt.name}"))
                navController.popBackStack()
            },
            isTv = true
        )
    }
}

