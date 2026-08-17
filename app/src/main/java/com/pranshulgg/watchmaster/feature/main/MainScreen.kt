package com.pranshulgg.watchmaster.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection.Companion.Bottom
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.ui.components.TooltipIconBtn
import com.pranshulgg.watchmaster.core.ui.navigation.NavRoutes
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.feature.home.HomeScreen
import com.pranshulgg.watchmaster.feature.main.components.MainBottomBar
import com.pranshulgg.watchmaster.feature.movie.MovieHomeScreen
import com.pranshulgg.watchmaster.feature.search.SearchType
import com.pranshulgg.watchmaster.feature.tv.TvHomeScreen

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun MainScreen(
    navController: NavController
) {

    val viewModel: MainScreenNavViewModel = viewModel()

    val selectedDestination = viewModel.selectedDestination

    val scrollBehavior =
        FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = Bottom)

    val scrollBehaviorTopBar = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehaviorTopBar,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                title = {
                    Text(selectedDestination.label)
                },
                actions = {
                    TooltipIconBtn(
                        onClick = { navController.navigate(NavRoutes.LISTS_SCREEN) },
                        icon = R.drawable.lists_24px,
                        tooltipText = localized("Listas", "Lists")
                    )
                    TooltipIconBtn(
                        onClick = { navController.navigate(NavRoutes.SETTINGS) },
                        icon = R.drawable.settings_24px,
                        tooltipText = localized("Ajustes", "Settings")
                    )
                }
            )
        },
        bottomBar = {
            MainBottomBar(
                selected = selectedDestination,
                onSelect = viewModel::selectDestination,
                onSearchClick = {
                    navController.navigate(NavRoutes.search(SearchType.MULTI))
                },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        ) {

            when (selectedDestination) {
                MainDestination.Home -> HomeScreen(
                    navController
                )

                MainDestination.Movies -> MovieHomeScreen(
                    navController,
                    scrollBehavior,
                    scrollBehaviorTopBar,
                )

                MainDestination.Series -> TvHomeScreen(
                    navController,
                    scrollBehavior,
                    scrollBehaviorTopBar,
                )
            }
        }
    }
}
