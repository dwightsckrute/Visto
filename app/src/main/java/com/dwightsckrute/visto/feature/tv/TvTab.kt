package com.dwightsckrute.visto.feature.tv

import androidx.compose.runtime.Composable
import com.dwightsckrute.visto.core.ui.localization.localized

enum class TvTab {
    WATCHLIST,
    WATCHING,
    FINISHED;

    /**
     * El título va aquí y no en el constructor del enum porque `localized` es @Composable: como
     * constante se fijaba en español al cargar la clase y ya no cambiaba al conmutar el idioma.
     */
    val title: String
        @Composable get() = when (this) {
            WATCHLIST -> localized("Pendientes", "Watchlist")
            WATCHING -> localized("Viendo", "Watching")
            FINISHED -> localized("Finalizadas", "Finished")
        }
}
