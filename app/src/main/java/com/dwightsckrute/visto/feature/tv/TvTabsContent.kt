package com.dwightsckrute.visto.feature.tv

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.dwightsckrute.visto.core.ui.components.MediaTabRow
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.components.EmptyContainerPlaceholder
import com.dwightsckrute.visto.data.local.entity.SeasonEntity
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.feature.movie.MovieTab
import com.dwightsckrute.visto.feature.movie.finished.FinishedMovies
import com.dwightsckrute.visto.feature.movie.watching.WatchingMovies
import com.dwightsckrute.visto.feature.movie.watchlist.WatchlistMovies
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import com.dwightsckrute.visto.feature.tv.finished.FinishedTv
import com.dwightsckrute.visto.feature.tv.watching.WatchingTv
import com.dwightsckrute.visto.feature.tv.watchlist.WatchlistTv
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TvTabsContent(
    navController: NavController,
    state: TvHomeState,
    scrollBehavior: FloatingToolbarScrollBehavior,
    scrollBehaviorTopBar: TopAppBarScrollBehavior,
    onLongActionTvSeasonRequest: (SeasonEntity?, WatchlistItemEntity) -> Unit,
) {

    val tabs = TvTab.entries
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()


    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            MediaTabRow(
                titles = tabs.map { it.title },
                pagerState = pagerState,
                onTabClick = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
            )
        },
    ) { innerPadding ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) { page ->


            when (tabs[page]) {
                TvTab.WATCHLIST -> {
                    WatchlistTv(
                        state.isLoading,
                        state.items,
                        scrollBehavior,
                        scrollBehaviorTopBar,
                        navController,
                        onLongActionTvSeasonRequest = { item, watchlistItem ->
                            onLongActionTvSeasonRequest(item, watchlistItem)
                        },
                        state.seasonWatchlist,
                        state.allSeasons
                    )
                }

                TvTab.WATCHING -> {
                    WatchingTv(
                        state.isLoading, state.items,
                        scrollBehavior,
                        scrollBehaviorTopBar,
                        navController,
                        onLongActionTvSeasonRequest = { item, watchlistItem ->
                            onLongActionTvSeasonRequest(item, watchlistItem)
                        },
                        state.seasonWatching,
                        state.allSeasons
                    )
                }

                TvTab.FINISHED -> {
                    FinishedTv(
                        state.isLoading, state.items,
                        scrollBehavior,
                        scrollBehaviorTopBar,
                        navController,
                        onLongActionTvSeasonRequest = { item, watchlistItem ->
                            onLongActionTvSeasonRequest(item, watchlistItem)
                        },
                        state.seasonFinished,
                        state.allSeasons
                    )
                }
            }

        }
    }
}