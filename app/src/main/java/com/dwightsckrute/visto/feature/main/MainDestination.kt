package com.dwightsckrute.visto.feature.main

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.localization.localized

/**
 * Destinos principales persistentes de Visto.
 *
 * Buscar no está aquí a propósito: es una acción global, no un destino equivalente, y por eso
 * vive como FAB separado. Ver `docs/DESIGN_SYSTEM.md`.
 */
enum class MainDestination(
    @param:DrawableRes val selectedIcon: Int,
    @param:DrawableRes val unselectedIcon: Int,
) {
    Home(
        selectedIcon = R.drawable.home_filled_24px,
        unselectedIcon = R.drawable.home_24px,
    ),
    Movies(
        selectedIcon = R.drawable.movie_filled_24px,
        unselectedIcon = R.drawable.movie_24px,
    ),
    Series(
        selectedIcon = R.drawable.tv_filled_24px,
        unselectedIcon = R.drawable.tv_24px,
    );

    val label: String
        @Composable get() = when (this) {
            Home -> localized("Inicio", "Home")
            Movies -> localized("Películas", "Movies")
            Series -> localized("Series", "TV shows")
        }

    companion object {
        /**
         * Resuelve la pestaña inicial guardada en preferencias. Se conservan las etiquetas en
         * español e inglés porque las instalaciones antiguas guardaron el valor ya traducido.
         */
        fun fromPreference(value: String?): MainDestination = when (value) {
            "movies", "Películas" -> Movies
            "tv", "Series" -> Series
            else -> Home
        }
    }
}
