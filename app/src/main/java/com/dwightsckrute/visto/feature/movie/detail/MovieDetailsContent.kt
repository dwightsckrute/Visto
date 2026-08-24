package com.dwightsckrute.visto.feature.movie.detail

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.components.media.MediaStatusSection
import com.dwightsckrute.visto.core.ui.navigation.NavRoutes
import com.dwightsckrute.visto.core.ui.snackbar.SnackbarManager
import com.dwightsckrute.visto.data.local.entity.MovieBundle
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.feature.movie.detail.components.MovieHeroHeader
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import com.dwightsckrute.visto.feature.shared.media.components.CastMovieSection
import com.dwightsckrute.visto.feature.shared.media.components.NotesSection
import com.dwightsckrute.visto.feature.shared.media.components.OverviewSection
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.localization.AppLanguage

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MovieDetailsContent(
    movieItem: MovieBundle,
    watchlistItem: WatchlistItemEntity?,
    navController: NavController,
    scrollBehavior: FloatingToolbarScrollBehavior,
    viewModel: MovieDetailsViewModel,
    watchlistViewModel: WatchlistViewModel
) {
    val isFinished = watchlistItem?.status == WatchStatus.FINISHED
    val cachedHrs = movieItem.cachedAt / 1000 / 60 / 60
    val currentHrs = System.currentTimeMillis() / 1000 / 60 / 60

    val refreshUnlocked = currentHrs.minus(cachedHrs) > 24

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()


    PullToRefreshBox(
        isRefreshing = isRefreshing,
        state = pullToRefreshState,
        onRefresh = {
            isRefreshing = true

            if (refreshUnlocked) {
                SnackbarManager.show(AppLanguage.text("Obteniendo datos…", "Fetching data…"))
                viewModel.load(
                    movieItem.id,
                    forceFetch = true,
                    onError = {
                        SnackbarManager.show(AppLanguage.text("No se pudieron obtener los datos de la película", "Could not fetch the movie data"))
                    })
                isRefreshing = false
            } else {
                SnackbarManager.show(AppLanguage.text("Los datos ya están actualizados", "The data is already up to date"))
                isRefreshing = false
            }
        },
        indicator = {
            LoadingIndicator(
                pullToRefreshState,
                isRefreshing,
                modifier = Modifier
                    .zIndex(99999f)
                    .align(Alignment.TopCenter)
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior)
    ) {
        LazyColumn(
            modifier = Modifier
                .nestedScroll(scrollBehavior)
                .imePadding()
        )
        {

            item {
                if (watchlistItem != null) {
                    MovieHeroHeader(
                        movieItem,
                        watchlistItem,
                        navController,
                        isFinished,
                        watchlistItem.userRating,
                        onUpdateRating = { newRating ->
                            watchlistViewModel.setUserRating(watchlistItem.id, newRating)
                            SnackbarManager.show(AppLanguage.text("Valoración actualizada", "Rating updated"))
                        },
                        onToggleFavorite = {
                            watchlistViewModel.setFavorite(
                                watchlistItem.id,
                                !watchlistItem.isFavorite,
                            )
                        },
                    )
                    MediaStatusSection(
                        status = watchlistItem.status,
                        onClick = { viewModel.showWatchProviderSheet(movieItem.watchProviders?.results["ES"]) })
                    OverviewSection(movieItem.overview)
                    NotesSection(
                        watchlistItem.notes.isNullOrBlank(),
                        { viewModel.showNoteDialog(watchlistItem.notes ?: "") },
                        watchlistItem.notes ?: ""
                    )
                    CastMovieSection(movieItem, onCastClick = { personId ->
                        navController.navigate(
                            NavRoutes.personScreen(personId)
                        )
                    })
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }
        }
    }

}
