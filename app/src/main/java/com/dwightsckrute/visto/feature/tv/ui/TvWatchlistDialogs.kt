package com.dwightsckrute.visto.feature.tv.ui

import androidx.compose.runtime.Composable
import com.dwightsckrute.visto.core.ui.snackbar.SnackbarManager
import com.dwightsckrute.visto.data.local.entity.SeasonEntity
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.feature.movie.MovieHomeViewModel
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import com.dwightsckrute.visto.feature.shared.media.components.MediaConfirmationDialogContent
import com.dwightsckrute.visto.feature.shared.media.components.MediaRatingDialogContent
import com.dwightsckrute.visto.feature.shared.media.ui.watchstatus.confirmAction
import com.dwightsckrute.visto.feature.tv.TvHomeViewModel
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.localization.AppLanguage

@Composable
fun TvWatchlistConfirmationDialog(
    watchlistViewModel: WatchlistViewModel,
    tvHomeViewModel: TvHomeViewModel,
    season: SeasonEntity?,
) {
    val uiState = tvHomeViewModel.uiState.value

    MediaConfirmationDialogContent(
        uiState.showConfirmationDialog,
        tvHomeViewModel::hideConfirmationDialog,
        customHeadline = if (uiState.seriesId != null) localized("Eliminar serie", "Delete show") else null,
        customMessage = if (uiState.seriesId != null) localized("¿Seguro que quieres eliminar esta serie? Se borrarán todas las temporadas. Esta acción no se puede deshacer.", "Delete this show? Every season will be removed. This cannot be undone.") else null,
        isTv = true,
        onConfirm = {
            if (uiState.seriesId != null) {
                watchlistViewModel.delete(uiState.seriesId)
            } else if (season != null) {
                watchlistViewModel.deleteSeason(season.seasonId)
            }
            SnackbarManager.show(
                if (season != null) {
                    AppLanguage.text("Temporada eliminada: ${season.name}", "Season deleted: ${season.name}")
                } else AppLanguage.text("Serie eliminada", "Show deleted")
            )
        }
    )
}

@Composable
fun TvWatchlistRatingDialog(
    watchlistViewModel: WatchlistViewModel,
    tvHomeViewModel: TvHomeViewModel,
    season: SeasonEntity?,
) {
    val uiState = tvHomeViewModel.uiState.value

    season?.let { item ->
        MediaRatingDialogContent(
            uiState.showRatingDialog,
            tvHomeViewModel::hideRatingDialog,
            isUpdateRating = uiState.isUpdateRating,
            originalRating = uiState.originalRating,
            onConfirm = { rating ->
                watchlistViewModel.setSeasonUserRating(item.seasonId, rating)
                watchlistViewModel.finishSeason(item.seasonId, item.seasonNumber)
                if (uiState.isUpdateRating) SnackbarManager.show(AppLanguage.text("Valoración actualizada", "Rating updated"))
            }
        )
    }
}


@Composable
fun TvStatusWatchlistConfirmationDialog(
    watchlistViewModel: WatchlistViewModel,
    tvHomeViewModel: TvHomeViewModel,
    season: SeasonEntity?,
) {
    val uiState = tvHomeViewModel.uiState.value

    season?.let { item ->
        MediaConfirmationDialogContent(
            uiState.showStatusConfirmationDialog,
            tvHomeViewModel::hideStatusConfirmationDialog,
            status = item.status,
            isTv = true,
            onConfirm = {
                item.status.confirmAction(
                    start = {
                        watchlistViewModel.startSeason(item.seasonId)
                    },
                    finish = {
                    },
                    reset = {
                        watchlistViewModel.resetSeason(item.seasonId)
                    }
                )
            }
        )
    }
}
