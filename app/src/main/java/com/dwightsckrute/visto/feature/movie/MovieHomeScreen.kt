package com.dwightsckrute.visto.feature.movie

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dwightsckrute.visto.core.ui.components.media.DatePickerSheet
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.feature.movie.ui.MovieStatusWatchlistConfirmationDialog
import com.dwightsckrute.visto.feature.movie.ui.MovieWatchlistBottomSheet
import com.dwightsckrute.visto.feature.movie.ui.MovieWatchlistConfirmationDialog
import com.dwightsckrute.visto.feature.movie.ui.MovieWatchlistRatingDialog
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import java.time.Instant


data class MovieHomeUiState(
    val showConfirmationDialog: Boolean = false,
    val showRatingDialog: Boolean = false,
    val actionSheetItem: WatchlistItemEntity? = null,
    val isUpdateRating: Boolean = false,
    val originalRating: Float = 0f,
    val isSheetOpen: Boolean = false,
    val showStatusConfirmationDialog: Boolean = false,
    val showDatePicker: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MovieHomeScreen(
    navController: NavController,
    scrollBehavior: FloatingToolbarScrollBehavior,
    scrollBehaviorTopBar: TopAppBarScrollBehavior,
) {

    val viewModel: WatchlistViewModel = hiltViewModel()
    val items by viewModel.watchlist.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val state = remember(items, isLoading) {
        MovieHomeState(items, isLoading)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val movieHomeViewModel: MovieHomeViewModel = hiltViewModel()
    val currentItem = movieHomeViewModel.uiState.value.actionSheetItem


    MovieTabsContent(
        navController = navController,
        scrollBehavior = scrollBehavior,
        scrollBehaviorTopBar = scrollBehaviorTopBar,
        state = state,
        onLongActionMovieRequest = { item ->
            movieHomeViewModel.showBottomSheet(item)
        }
    )


    MovieWatchlistBottomSheet(
        show = movieHomeViewModel.uiState.value.isSheetOpen,
        sheetState = sheetState,
        watchlistItem = currentItem,
        onDismiss = movieHomeViewModel::hideBottomSheet,
        viewModel,
        movieHomeViewModel
    )

    MovieWatchlistConfirmationDialog(
        watchlistViewModel = viewModel,
        movieHomeViewModel = movieHomeViewModel,
        watchlistItem = currentItem
    )

    MovieWatchlistRatingDialog(
        watchlistViewModel = viewModel,
        movieHomeViewModel = movieHomeViewModel,
        watchlistItem = currentItem
    )

    MovieStatusWatchlistConfirmationDialog(
        watchlistViewModel = viewModel,
        movieHomeViewModel = movieHomeViewModel,
        watchlistItem = currentItem
    )

    DatePickerSheet(
        show = movieHomeViewModel.uiState.value.showDatePicker,
        initialDate = currentItem?.finishedDate?.toEpochMilli(),
        id = currentItem?.id ?: -1L,
        onDateSelected = { idIt, dateIt ->
            if (dateIt != null) {
                viewModel.updateFinishedDate(idIt, Instant.ofEpochMilli(dateIt))
            }
        },
        onDismiss = {
            movieHomeViewModel.hideDatePicker()
        }
    )

}


