package com.dwightsckrute.visto.feature.search

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.dwightsckrute.visto.core.ui.components.NavigateUpBtn
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenScaffold(
    viewModel: SearchViewModel,
    navController: NavController,
    searchType: SearchType,
    onSearchTypeChange: (SearchType) -> Unit,
    watchlistViewModel: WatchlistViewModel,
) {
    val title = when (searchType) {
        SearchType.MOVIE -> localized("Buscar películas", "Search movies")
        SearchType.TV -> localized("Buscar series", "Search TV shows")
        SearchType.PERSON -> localized("Buscar personas", "Search people")
        SearchType.BOOK -> localized("Buscar libros", "Search books")
        SearchType.MULTI -> localized("Buscar en Visto", "Search Visto")
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { NavigateUpBtn(navController) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
    ) { paddingValues ->
        SearchScreenContent(
            paddingValues = paddingValues,
            onSearchTypeChange = onSearchTypeChange,
            viewModel = viewModel,
            searchType = searchType,
            watchlistViewModel = watchlistViewModel,
        )
    }
}
