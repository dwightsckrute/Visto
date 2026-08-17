package com.dwightsckrute.visto.feature.lists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.EmptyContainerPlaceholder
import com.dwightsckrute.visto.core.ui.components.LargeTopBarScaffold
import com.dwightsckrute.visto.core.ui.components.LoadingScreenPlaceholder
import com.dwightsckrute.visto.core.ui.components.NavigateUpBtn
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.navigation.NavRoutes
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import com.dwightsckrute.visto.core.ui.localization.localized

@Composable
fun ListsScreenScaffold(
    navController: NavController,
    viewModel: ListsViewModel,
    watchlistViewModel: WatchlistViewModel
) {

    val customLists by viewModel.customLists.collectAsStateWithLifecycle(initialValue = emptyList())
    val customListsLoading by viewModel.isLoading.collectAsStateWithLifecycle(initialValue = true)
    val watchlistItems by watchlistViewModel.watchlist.collectAsStateWithLifecycle()


    if (customListsLoading) {
        LoadingScreenPlaceholder()
        return
    }

    LargeTopBarScaffold(
        title = localized("Listas", "Lists"),
        navigationIcon = { NavigateUpBtn(navController) },
        fab = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(NavRoutes.listEntryScreen(-1L)) },
                text = { Text(localized("Crear lista", "Create list"), style = MaterialTheme.typography.titleMedium) },
                icon = {
                    Symbol(
                        R.drawable.add_24px,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            if (customLists.isEmpty()) {
                EmptyContainerPlaceholder(
                    text = "No se encontraron listas",
                    description = localized("Crea una lista para empezar", "Create a list to get started"),
                    icon = R.drawable.lists_24px
                )
            }
            ListsScreenContent(
                customLists,
                onClick = { navController.navigate(NavRoutes.viewListScreen(it)) },
                watchlistItems
            )
        }

    }


}