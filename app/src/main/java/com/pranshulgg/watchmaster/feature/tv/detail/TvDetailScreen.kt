package com.pranshulgg.watchmaster.feature.tv.detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection.Companion.Bottom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.pranshulgg.watchmaster.core.ui.components.ErrorContainer
import com.pranshulgg.watchmaster.data.CountryWatchProviders
import com.pranshulgg.watchmaster.feature.shared.WatchlistViewModel
import com.pranshulgg.watchmaster.feature.tv.detail.ui.TvDetailEffects

data class TvDetailsUiState(
    val showNoteDialog: Boolean = false,
    val note: String = "",
    val showRatingDialog: Boolean = false,
    val showConfirmationDialog: Boolean = false,
    val isWatchProviderSheetOpen: Boolean = false,
    val currentWatchProviders: CountryWatchProviders? = null
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TvDetailsScreen(id: Long, seasonNumber: Int, navController: NavController, seasonId: Long) {

    val viewModel: TvDetailsViewModel = hiltViewModel()
    val watchlistViewModel: WatchlistViewModel = hiltViewModel()
    var showError by remember { mutableStateOf(false) }

    // La temporada visible es estado de esta pantalla, no un destino de navegación. Cambiarla
    // navegando hacía que la serie entera saliera y volviera a entrar con su transición y su
    // pantalla de carga, cuando lo único que cambia es qué episodios se listan.
    var selectedSeasonNumber by rememberSaveable(id) { mutableIntStateOf(seasonNumber) }
    var selectedSeasonId by rememberSaveable(id) { mutableLongStateOf(seasonId) }

    TvDetailEffects(
        id,
        selectedSeasonNumber,
        viewModel,
        selectedSeasonId,
        onError = { showError = true },
    )

    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = Bottom)

    if (showError) {
        ErrorContainer(onRetry = {
            showError = false
            viewModel.load(id, onError = { showError = true })
        }, errorDescription = "No se pudieron cargar los detalles de la serie. Inténtalo de nuevo.")
    }


    TvDetailsScaffold(
        id = id,
        seasonNumber = selectedSeasonNumber,
        scrollBehavior = scrollBehavior,
        viewModel = viewModel,
        watchlistViewModel = watchlistViewModel,
        navController = navController,
        seasonId = selectedSeasonId,
        onSelectSeason = { target ->
            selectedSeasonNumber = target.seasonNumber
            selectedSeasonId = target.seasonId
        },
    )

}
