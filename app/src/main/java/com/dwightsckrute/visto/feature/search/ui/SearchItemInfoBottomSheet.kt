package com.dwightsckrute.visto.feature.search.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.dwightsckrute.visto.data.repository.MEDIA_TYPE_BOOK
import com.dwightsckrute.visto.core.ui.components.ActionBottomSheet
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.components.media.DatePickerSheet
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.snackbar.SnackbarManager
import com.dwightsckrute.visto.core.ui.theme.Spacing
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.network.TvSeasonDto
import com.dwightsckrute.visto.feature.search.SearchItem
import com.dwightsckrute.visto.feature.search.SearchViewModel
import com.dwightsckrute.visto.feature.search.components.SearchItemInfoSheetContent
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import kotlinx.coroutines.launch
import java.time.Instant

private data class PendingWatched(
    val item: SearchItem,
    val seasons: List<TvSeasonDto>?,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchItemInfoBottomSheet(
    viewModel: SearchViewModel,
    watchlistViewModel: WatchlistViewModel,
    sheetState: SheetState,
    show: Boolean,
    navController: NavController
) {

    val item = viewModel.uiState.value.selectedItem
    val tvDetailsList = viewModel.uiState.value.selectedSeasonList
    val scope = rememberCoroutineScope()
    // Lo que impide añadir es una serie cuyas temporadas aún no han llegado, no "no ser una
    // película": escrito de aquella forma, cualquier tipo nuevo nacía desactivado, y eso es lo
    // que dejó a los libros sin poder añadirse.
    val disabled = item?.mediaType == "tv" && tvDetailsList.isNullOrEmpty()

    // Se guarda antes de cerrar la hoja porque cerrarla limpia la selección del ViewModel, y
    // el selector de fecha aún necesita saber sobre qué título se está actuando.
    var pendingWatched by remember { mutableStateOf<PendingWatched?>(null) }

    // localized() es @Composable: los textos de los avisos se resuelven aquí, no dentro de las
    // lambdas de confirmación.
    val addedMessage = localized("Añadido a pendientes", "Added to watchlist")

    // Un libro no se ve, se lee. La hoja de resultados se escribió cuando solo había películas y
    // series, así que decía "vista" de todo lo que cayera dentro; ahora también caen libros.
    val isBook = item?.mediaType == MEDIA_TYPE_BOOK
    val watchedMessage =
        if (isBook) localized("Marcado como leído", "Marked as read")
        else localized("Marcada como vista", "Marked as watched")
    val markWatchedLabel =
        if (isBook) localized("Marcar como leído…", "Mark as read…")
        else localized("Marcar como vista…", "Mark as watched…")

    if (show && item != null)
        ActionBottomSheet(
            sheetState = sheetState,
            onCancel = {
                viewModel.hideSheet()
            },
            confirmText = localized("Añadir a pendientes", "Add to watchlist"),
            confirmBtnMaxWidth = true,
            isConfirmDisabled = disabled,
            onConfirm = {
                scope.launch {
                    viewModel.addToWatchlist(item, tvDetailsList, watchlistViewModel)
                    SnackbarManager.show(addedMessage)
                }
            }
        ) { hide ->
            // ActionBottomSheet ya limita la altura del conjunto; aquí solo se apilan.
            Column(modifier = Modifier.fillMaxWidth()) {
                SearchItemInfoSheetContent(
                    item = item,
                    seasonLoading = viewModel.seasonLoading,
                    seasonData = viewModel.seasonData,
                    onSelectedSeason = { seasons ->
                        viewModel.updateSelectedSeasonList(seasons)
                    },
                    watchlistViewModel
                )

                // Segunda vía: lo que ya habías visto antes de anotarlo no tiene por qué pasar
                // primero por pendientes. Cierra esta hoja antes de abrir el selector: dos
                // ModalBottomSheet superpuestas se comportan de forma impredecible.
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                        .heightIn(min = Spacing.minTouchTarget),
                    enabled = !disabled,
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        pendingWatched = PendingWatched(item, tvDetailsList)
                        hide()
                    },
                ) {
                    Symbol(
                        icon = R.drawable.check_24px,
                        desc = null,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = markWatchedLabel,
                        modifier = Modifier.padding(start = Spacing.sm),
                    )
                }
            }
        }

    val pending = pendingWatched
    val todayMillis = remember(pending) { System.currentTimeMillis() }
    DatePickerSheet(
        show = pending != null,
        initialDate = todayMillis,
        id = MARK_WATCHED_SHEET_ID,
        onDateSelected = { _, millis ->
            if (pending != null && millis != null) {
                viewModel.addAndMarkWatched(
                    item = pending.item,
                    tvDetails = pending.seasons,
                    watchlistViewModel = watchlistViewModel,
                    watchedAt = Instant.ofEpochMilli(millis),
                )
                SnackbarManager.show(watchedMessage)
            }
            pendingWatched = null
        },
        onDismiss = { pendingWatched = null },
    )
}

/** DatePickerSheet exige un id para reutilizarse desde los listados; aquí solo importa la fecha. */
private const val MARK_WATCHED_SHEET_ID = 0L
