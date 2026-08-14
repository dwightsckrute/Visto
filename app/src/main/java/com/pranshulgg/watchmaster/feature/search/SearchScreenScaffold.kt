package com.pranshulgg.watchmaster.feature.search

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.pranshulgg.watchmaster.core.ui.components.NavigateUpBtn
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.feature.shared.WatchlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenScaffold(
    viewModel: SearchViewModel,
    navController: NavController,
    searchType: SearchType,
    watchlistViewModel: WatchlistViewModel,
) {
    val title = when (searchType) {
        SearchType.MOVIE -> localized("Buscar películas", "Search movies")
        SearchType.TV -> localized("Buscar series", "Search TV shows")
        SearchType.PERSON -> localized("Buscar personas", "Search people")
        SearchType.MULTI -> localized("Buscar", "Search")
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
            viewModel = viewModel,
            searchType = searchType,
            watchlistViewModel = watchlistViewModel,
        )
    }
}
