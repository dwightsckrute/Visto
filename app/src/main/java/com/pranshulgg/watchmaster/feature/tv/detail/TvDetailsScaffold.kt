package com.pranshulgg.watchmaster.feature.tv.detail

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pranshulgg.watchmaster.core.ui.components.LoadingScreenPlaceholder
import com.pranshulgg.watchmaster.core.utils.shareMedia
import com.pranshulgg.watchmaster.feature.shared.WatchlistViewModel
import com.pranshulgg.watchmaster.feature.shared.media.ui.FloatingToolbarMediaActionsParams
import com.pranshulgg.watchmaster.feature.shared.media.ui.MediaActionsFloatingToolbar
import com.pranshulgg.watchmaster.feature.tv.detail.ui.TvDetailsConfirmationDialog
import com.pranshulgg.watchmaster.feature.tv.detail.ui.TvDetailsNoteDialog
import com.pranshulgg.watchmaster.feature.tv.detail.ui.TvDetailsRatingDialog
import com.pranshulgg.watchmaster.feature.tv.detail.ui.TvWatchProviderSheet

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TvDetailsScaffold(
    id: Long,
    seasonNumber: Int,
    seasonId: Long,
    scrollBehavior: FloatingToolbarScrollBehavior,
    viewModel: TvDetailsViewModel,
    watchlistViewModel: WatchlistViewModel,
    navController: NavController
) {

    val loading = viewModel.loading
    val seasons by watchlistViewModel.seasonsForShow(id).collectAsState(initial = emptyList())
    val season = seasons.find { it.seasonNumber == seasonNumber }
    val watchlistFlow = remember(id) { watchlistViewModel.item(id) }
    val watchlistItem by watchlistFlow.collectAsStateWithLifecycle()
    val episodesFlow = remember(seasonId) { viewModel.seasonEpisodes(seasonId) }
    val episodes by episodesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val tvItem = viewModel.state


    val isSeriesPinned = watchlistItem?.isPinned == true

    if (loading || season == null || tvItem == null) {
        LoadingScreenPlaceholder()
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        bottomBar = {
            MediaActionsFloatingToolbar(
                scrollBehavior,
                season.status,
                actions = FloatingToolbarMediaActionsParams(
                    startWatching = { watchlistViewModel.startSeason(season.seasonId) },
                    resetWatching = { watchlistViewModel.resetSeason(season.seasonId) },
                    finishWatching = { viewModel.showRatingDialog() },
                    interruptWatching = { watchlistViewModel.interruptSeason(season.seasonId) },
                    delete = { viewModel.showConfirmationDialog() },
                    togglePin = {
                        watchlistItem?.let {
                            watchlistViewModel.setPinned(
                                season.showId,
                                !it.isPinned
                            )
                        }
                    },
                    share = { shareMedia(context, tvItem.name, id, isTv = true) },
                    // Aquí la unidad es la temporada, no la serie: se marcan sus episodios
                    // como vistos y se fecha la temporada, igual que hace "terminar".
                    markWatchedOn = { watchedAt ->
                        watchlistViewModel.finishSeason(season.seasonId, season.episodeCount)
                        watchlistViewModel.updateSeasonFinishedDate(season.seasonId, watchedAt)
                    },
                ),
                isPinned = isSeriesPinned,
                isTv = true
            )
        },

        )
    { _ ->
        TvDetailsContent(
            tvItem,
            navController,
            scrollBehavior,
            season,
            viewModel,
            watchlistViewModel,
            episodes
        )
    }

    TvDetailsNoteDialog(viewModel, watchlistViewModel, season)
    TvDetailsRatingDialog(viewModel, watchlistViewModel, season)
    TvDetailsConfirmationDialog(
        viewModel,
        watchlistViewModel,
        season,
        seasons.size,
        navController
    )
    TvWatchProviderSheet(
        sheetState,
        onDismiss = { viewModel.hideWatchProviderSheet() },
        viewModel
    )
}
