package com.dwightsckrute.visto.feature.shared.media.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.components.AppPill
import com.dwightsckrute.visto.core.ui.components.PillSize
import com.dwightsckrute.visto.core.ui.theme.Spacing

/**
 * El estado de una película o temporada, y su nota si la terminaste.
 *
 * Antes era una cápsula partida: la etiqueta con media esquina redonda y la nota soldada al otro
 * lado con esquinas distintas, todo con su 12sp en negrita a mano. Ahora son dos píldoras
 * separadas del componente común, igual que en el diario. Dos piezas contiguas se leen igual de
 * bien que una partida y se parecen al resto de la aplicación, que era el problema.
 */
@Composable
fun WatchListStatusPill(
    containerColor: Color,
    contentColor: Color,
    text: String,
    status: WatchStatus,
    rating: Double? = null,
    showRating: Boolean = true,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppPill(
            label = text,
            container = containerColor,
            onContainer = contentColor,
            size = PillSize.Small,
        )
        // La nota solo cuando la hay y cuando significa algo: una nota junto a "Pendiente" sería
        // una valoración de algo que no has visto.
        if (status == WatchStatus.FINISHED && showRating && rating != null) {
            AppPill(
                label = "%.1f".format(rating),
                icon = R.drawable.star_24px,
                container = MaterialTheme.colorScheme.tertiaryContainer,
                onContainer = MaterialTheme.colorScheme.onTertiaryContainer,
                size = PillSize.Small,
            )
        }
    }
}
