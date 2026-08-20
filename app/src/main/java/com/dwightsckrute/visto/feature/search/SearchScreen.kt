package com.dwightsckrute.visto.feature.search

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
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

    // El tipo llega por la ruta pero deja de estar fijado ahí: quien entra a buscar una película
    // y se acuerda de un libro no debería tener que salir y volver a entrar por otra puerta.
    var activeType by rememberSaveable { mutableStateOf(searchType) }

    SearchScreenScaffold(
        viewModel = viewModel,
        navController = navController,
        searchType = activeType,
        onSearchTypeChange = { type ->
            activeType = type
            if (viewModel.query.isNotBlank()) viewModel.search(type)
        },
        watchlistViewModel = watchlistViewModel,
        sheetState = sheetState,
    )

    SearchItemInfoBottomSheet(
        viewModel,
        watchlistViewModel,
        sheetState,
        viewModel.uiState.value.isSheetOpen,
        navController,
    )
}
