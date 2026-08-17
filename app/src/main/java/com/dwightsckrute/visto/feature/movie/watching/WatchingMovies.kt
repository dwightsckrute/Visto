package com.dwightsckrute.visto.feature.movie.watching

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.EmptyContainerPlaceholder
import com.dwightsckrute.visto.core.ui.components.LoadingScreenPlaceholder
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.feature.movie.components.MovieItems
import com.dwightsckrute.visto.core.ui.localization.localized


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WatchingMovies(
    isLoading: Boolean = true,
    items: List<WatchlistItemEntity>, scrollBehavior: FloatingToolbarScrollBehavior,
    scrollBehaviorTopBar: TopAppBarScrollBehavior,
    navController: NavController,
    onLongActionMovieRequest: (WatchlistItemEntity) -> Unit
) {

    if (isLoading) {
        LoadingScreenPlaceholder()
    }

    if (items.isEmpty()) {
        EmptyContainerPlaceholder(
            R.drawable.movie_info_24px,
            "Nada en curso",
            description = localized("Parece que aún no has empezado a ver nada. ¡Encuentra tu próximo maratón!", "Looks like you have not started anything. Find your next binge!")
        )
    }

    val pinnedItems = items.filter { it.isPinned }
    val normalItems = items.filter { !it.isPinned }

    MovieItems(
        scrollBehavior,
        scrollBehaviorTopBar,
        navController,
        onLongActionMovieRequest,
        pinnedItems,
        normalItems
    )

}
