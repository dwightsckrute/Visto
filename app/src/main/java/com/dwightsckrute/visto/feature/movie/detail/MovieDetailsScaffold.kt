package com.dwightsckrute.visto.feature.movie.detail

import android.annotation.SuppressLint
import android.widget.Space
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.components.sheetRecede
import com.dwightsckrute.visto.core.ui.components.LoadingScreenPlaceholder
import com.dwightsckrute.visto.core.ui.components.entranceSettle
import com.dwightsckrute.visto.core.ui.components.rememberEntranceSettle
import com.dwightsckrute.visto.core.utils.shareMedia
import com.dwightsckrute.visto.feature.movie.detail.ui.MovieDetailsConfirmationDialog
import com.dwightsckrute.visto.feature.movie.detail.ui.MovieDetailsNoteDialog
import com.dwightsckrute.visto.feature.movie.detail.ui.MovieDetailsRatingDialog
import com.dwightsckrute.visto.feature.movie.detail.ui.MovieWatchProviderSheet
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import com.dwightsckrute.visto.feature.shared.media.ui.FloatingToolbarMediaActionsParams
import com.dwightsckrute.visto.feature.shared.media.ui.MediaActionsFloatingToolbar
import com.dwightsckrute.visto.feature.tv.detail.ui.TvWatchProviderSheet
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MovieDetailsScaffold(
    id: Long,
    scrollBehavior: FloatingToolbarScrollBehavior,
    viewModel: MovieDetailsViewModel,
    watchlistViewModel: WatchlistViewModel,
    navController: NavController
) {

    val watchlistFlow = remember(id) { watchlistViewModel.item(id) }
    val watchlistItem by watchlistFlow.collectAsStateWithLifecycle()

    val movieItem = viewModel.state
    val context = LocalContext.current

    val loading = viewModel.loading
    val isMoviePinned = watchlistItem?.isPinned == true

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // El marcador de carga solo aparece si la espera se nota de verdad. Con los datos en caché
    // llegan en un par de frames, y meter una pantalla en blanco con un cargador entre la lista y
    // la ficha para luego quitarla es justo lo que se percibía como un tirón: un destello vale
    // menos que nada. Durante ese margen no se dibuja nada y la transición de salida de la
    // pantalla anterior cubre el hueco.
    val ready = !loading && movieItem != null

    var showPlaceholder by remember { mutableStateOf(false) }
    LaunchedEffect(ready) {
        showPlaceholder = false
        if (!ready) {
            delay(PLACEHOLDER_DELAY_MS)
            showPlaceholder = true
        }
    }

    // Si la ficha no estaba en caché, la transición de navegación se agota sobre una pantalla
    // vacía y el contenido aparecería de golpe al llegar. Esto lo hace entrar en movimiento.
    val settle = rememberEntranceSettle(ready)

    if (!ready || movieItem == null) {
        if (showPlaceholder) LoadingScreenPlaceholder()
        return
    }

    Scaffold(
        modifier = Modifier
            .entranceSettle(settle)
            .sheetRecede(sheetState),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        bottomBar = {
            MediaActionsFloatingToolbar(
                scrollBehavior,
                watchlistItem?.status ?: WatchStatus.WATCHING,
                actions = FloatingToolbarMediaActionsParams(
                    startWatching = { watchlistViewModel.start(id) },
                    resetWatching = { watchlistViewModel.reset(id) },
                    finishWatching = { viewModel.showRatingDialog() },
                    interruptWatching = { watchlistViewModel.interrupt(id) },
                    delete = { viewModel.showConfirmationDialog() },
                    togglePin = { watchlistViewModel.setPinned(id, !isMoviePinned) },
                    toggleFavorite = {
                        watchlistViewModel.setFavorite(id, watchlistItem?.isFavorite != true)
                    },
                    share = { shareMedia(context, movieItem.title, id, isTv = false) },
                    markWatchedOn = { watchedAt ->
                        watchlistViewModel.finish(id)
                        watchlistViewModel.updateFinishedDate(id, watchedAt)
                    },
                ),
                isPinned = isMoviePinned,
                isFavorite = watchlistItem?.isFavorite == true,
            )
        },

        ) { _ ->
        // Con la ficha en caché el contenido ya está listo en el primer frame y entraba de
        // golpe; sin caché aparecía tras el marcador, igual de seco. Este asentamiento corto lo
        // iguala. El disparador es un estado aparte porque animateFloatAsState arranca ya en su
        MovieDetailsContent(
            movieItem,
            watchlistItem,
            navController,
            scrollBehavior,
            viewModel,
            watchlistViewModel,
        )
    }


    MovieDetailsNoteDialog(viewModel, watchlistViewModel, watchlistItem)
    MovieDetailsRatingDialog(viewModel, watchlistViewModel, watchlistItem)
    MovieDetailsConfirmationDialog(viewModel, watchlistViewModel, watchlistItem, navController)
    MovieWatchProviderSheet(
        sheetState,
        onDismiss = { viewModel.hideWatchProviderSheet() },
        viewModel
    )
}

/** Margen antes de admitir que hay espera. Por debajo, el cargador molesta más que ayuda. */
private const val PLACEHOLDER_DELAY_MS = 180L
