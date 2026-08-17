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
import com.pranshulgg.watchmaster.data.local.entity.SeasonEntity
import com.pranshulgg.watchmaster.core.utils.shareMedia
import com.pranshulgg.watchmaster.feature.shared.WatchlistViewModel
import com.pranshulgg.watchmaster.feature.shared.media.ui.FloatingToolbarMediaActionsParams
import com.pranshulgg.watchmaster.feature.shared.media.ui.MediaActionsFloatingToolbar
import com.pranshulgg.watchmaster.feature.tv.detail.ui.TvDetailsConfirmationDialog
import com.pranshulgg.watchmaster.feature.tv.detail.ui.TvDetailsNoteDialog
import com.pranshulgg.watchmaster.feature.tv.detail.ui.TvDetailsRatingDialog
import com.pranshulgg.watchmaster.feature.tv.detail.ui.TvWatchProviderSheet
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

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
    navController: NavController,
    onSelectSeason: (SeasonEntity) -> Unit,
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

    // El marcador de carga solo aparece si la espera se nota de verdad. Con los datos en caché
    // llegan en un par de frames, y meter una pantalla en blanco con un cargador entre la lista y
    // la ficha para luego quitarla es justo lo que se percibía como un tirón: un destello vale
    // menos que nada. Durante ese margen no se dibuja nada y la transición de salida de la
    // pantalla anterior cubre el hueco.
    var showPlaceholder by remember { mutableStateOf(false) }
    LaunchedEffect(loading || season == null || tvItem == null) {
        showPlaceholder = false
        if (loading || season == null || tvItem == null) {
            delay(PLACEHOLDER_DELAY_MS)
            showPlaceholder = true
        }
    }

    if (loading || season == null || tvItem == null) {
        if (showPlaceholder) LoadingScreenPlaceholder()
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
        // Mismo asentamiento que en la ficha de película: con datos en caché el contenido
        // entraba de golpe. El disparador va en un estado aparte porque las animaciones de
        TvDetailsContent(
            tvItem,
            navController,
            scrollBehavior,
            season,
            viewModel,
            watchlistViewModel,
            episodes,
            allSeasons = seasons,
            onSelectSeason = onSelectSeason,
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

/** Margen antes de admitir que hay espera. Por debajo, el cargador molesta más que ayuda. */
private const val PLACEHOLDER_DELAY_MS = 180L
