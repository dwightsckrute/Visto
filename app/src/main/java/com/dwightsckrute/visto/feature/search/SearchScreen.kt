package com.dwightsckrute.visto.feature.search

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dwightsckrute.visto.core.network.TvSeasonDto
import com.dwightsckrute.visto.feature.search.ui.SearchItemInfoBottomSheet
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel

data class SearchUiState(
    val selectedItem: SearchItem? = null,
    val selectedSeasonList: List<TvSeasonDto>? = null,
    val isSheetOpen: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    searchType: SearchType,
) {
    val viewModel: SearchViewModel = hiltViewModel()
    val watchlistViewModel: WatchlistViewModel = hiltViewModel()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    SearchScreenScaffold(
        viewModel = viewModel,
        navController = navController,
        searchType = searchType,
        watchlistViewModel = watchlistViewModel,
    )

    SearchItemInfoBottomSheet(
        viewModel,
        watchlistViewModel,
        sheetState,
        viewModel.uiState.value.isSheetOpen,
        navController,
    )
}
