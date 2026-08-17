package com.dwightsckrute.visto.feature.movie.detail.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.ActionBottomSheet
import com.dwightsckrute.visto.core.ui.components.EmptyContainerPlaceholder
import com.dwightsckrute.visto.feature.movie.detail.MovieDetailsViewModel
import com.dwightsckrute.visto.feature.shared.media.components.WatchProviderItem
import com.dwightsckrute.visto.feature.tv.detail.TvDetailsViewModel
import com.dwightsckrute.visto.core.ui.localization.localized


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieWatchProviderSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    viewModel: MovieDetailsViewModel
) {

    val providers = viewModel.uiState.value.currentWatchProviders

    if (viewModel.uiState.value.isWatchProviderSheetOpen)
        ActionBottomSheet(
            sheetState = sheetState,
            onCancel = { onDismiss() },
            onConfirm = { },
            confirmBtnMaxWidth = true,
            hideConfirmBtn = true,
            cancelText = "OK"
        ) {
            if (providers != null) {

                if (providers.flatrate != null) {
                    WatchProviderItem(localized("Suscripción", "Subscription"), providers.flatrate)
                }

                if (providers.buy != null) {
                    Spacer(Modifier.height(12.dp))
                    WatchProviderItem("Comprar", providers.buy)
                }

                Spacer(Modifier.height(10.dp))
            } else {
                EmptyContainerPlaceholder(
                    R.drawable.movie_info_24px,
                    "No se encontraron proveedores",
                    size = 0.7f,
                    fraction = 0.3f
                )
            }
        }

}