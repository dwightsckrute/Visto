package com.dwightsckrute.visto.feature.tv.detail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import com.dwightsckrute.visto.feature.tv.detail.TvDetailsViewModel

@Composable
fun TvDetailEffects(
    id: Long,
    seasonNumber: Int,
    viewModel: TvDetailsViewModel,
    seasonId: Long,
    onError: () -> Unit
) {
    LaunchedEffect(id) { viewModel.load(id, onError = { onError() }) }
    LaunchedEffect(seasonNumber) { viewModel.loadEpisodes(id, seasonId, seasonNumber) }
    LaunchedEffect(Unit) { viewModel.loading }

}