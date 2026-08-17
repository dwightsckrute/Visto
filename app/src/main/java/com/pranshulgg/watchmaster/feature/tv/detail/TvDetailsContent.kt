package com.pranshulgg.watchmaster.feature.tv.detail

import java.util.concurrent.TimeUnit
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pranshulgg.watchmaster.core.model.WatchStatus
import com.pranshulgg.watchmaster.core.ui.components.media.MediaStatusSection
import com.pranshulgg.watchmaster.core.ui.navigation.NavRoutes
import com.pranshulgg.watchmaster.core.ui.snackbar.SnackbarManager
import com.pranshulgg.watchmaster.data.local.entity.SeasonEntity
import com.pranshulgg.watchmaster.data.local.entity.TvBundle
import com.pranshulgg.watchmaster.data.local.entity.TvEpisodeEntity
import com.pranshulgg.watchmaster.feature.shared.WatchlistViewModel
import com.pranshulgg.watchmaster.feature.shared.media.components.CastTvSection
import com.pranshulgg.watchmaster.feature.shared.media.components.NotesSection
import com.pranshulgg.watchmaster.feature.shared.media.components.OverviewSection
import com.pranshulgg.watchmaster.feature.tv.detail.components.EpisodesSection
import com.pranshulgg.watchmaster.feature.tv.detail.components.SeasonSwitcher
import com.pranshulgg.watchmaster.feature.tv.detail.components.TvHeroHeader
import kotlinx.coroutines.launch
import kotlin.text.get

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TvDetailsContent(
    tvItem: TvBundle,
    navController: NavController,
    scrollBehavior: FloatingToolbarScrollBehavior,
    season: SeasonEntity?,
    viewModel: TvDetailsViewModel,
    watchlistViewModel: WatchlistViewModel,
    episodes: List<TvEpisodeEntity>,
    allSeasons: List<SeasonEntity>,
    onSelectSeason: (SeasonEntity) -> Unit,
) {


    val isFinished = season?.status == WatchStatus.FINISHED

    val watchlistFlow = remember(tvItem.id) { watchlistViewModel.item(tvItem.id) }
    val watchlistItem by watchlistFlow.collectAsStateWithLifecycle()


    val cachedHrs = season?.cachedAt?.let { TimeUnit.MILLISECONDS.toHours(it) }
        ?: TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis())
    val currentHrs = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis())

    val refreshUnlocked = currentHrs.minus(cachedHrs) > 24

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()


    PullToRefreshBox(
        isRefreshing = isRefreshing,
        state = pullToRefreshState,
        onRefresh = {
            isRefreshing = true

            if (season != null && (episodes.isEmpty() || refreshUnlocked)) {
                SnackbarManager.show("Obteniendo datos…")
                viewModel.refreshSeasonData(season)
                isRefreshing = false
            } else {
                SnackbarManager.show("Los datos ya están actualizados")
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
        ) {
            item {
                if (season != null) {
                    TvHeroHeader(
                        tvItem,
                        watchlistItem,
                        navController,
                        isFinished,
                        season,
                        userRating = season.seasonUserRating,
                        onUpdateRating = { newRating ->
                            watchlistViewModel.setSeasonUserRating(season.seasonId, newRating)
                            SnackbarManager.show("Valoración actualizada")
                        }
                    )


                    MediaStatusSection(
                        status = season.status,
                        onClick = { viewModel.showWatchProviderSheet(tvItem.watchProviders?.results["ES"]) })
                    OverviewSection(tvItem.overview)
                    NotesSection(
                        season.seasonNotes.isNullOrBlank(),
                        { viewModel.showNoteDialog(season.seasonNotes ?: "") },
                        season.seasonNotes ?: ""
                    )
                    SeasonSwitcher(
                        seasons = allSeasons,
                        selectedSeasonId = season.seasonId,
                        onSelectSeason = onSelectSeason,
                    )
                    // Los episodios se cruzan con un fundido en lugar de desaparecer y volver.
                    // Quitar el bloque mientras carga la temporada nueva hacía saltar el layout
                    // y daba la sensación de estar entrando en otra pantalla.
                    AnimatedContent(
                        targetState = episodes,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "season-episodes",
                    ) { seasonEpisodes ->
                        if (seasonEpisodes.isEmpty()) {
                            // Hueco del mismo alto mientras llegan: sin spinner, porque los
                            // episodios salen de Room y aparecen enseguida.
                            Spacer(Modifier.height(120.dp))
                        } else {
                            EpisodesSection(
                                seasonEpisodes,
                                viewModel,
                                season
                            )
                        }
                    }
                    CastTvSection(tvItem, onCastClick = { personId ->
                        navController.navigate(
                            NavRoutes.personScreen(personId)
                        )
                    })
                    Spacer(modifier = Modifier.height(56.dp))
                } else {
                    Text("No se encontró ninguna temporada")
                }
            }
        }
    }
}
