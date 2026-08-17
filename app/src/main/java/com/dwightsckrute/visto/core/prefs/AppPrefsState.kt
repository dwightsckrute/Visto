package com.dwightsckrute.visto.core.prefs

import com.dwightsckrute.visto.core.ui.theme.ThemeVariantType

data class AppPrefsState(
    val appTheme: String,
    val setAppTheme: (String) -> Unit,

    val themeColor: String,
    val setThemeColor: (String) -> Unit,

    val isCustomTheme: Boolean,
    val useCustomTheme: (Boolean) -> Unit,

    val useDynamicColor: Boolean,
    val setDynamicColor: (Boolean) -> Unit,

    val useAmoledBlack: Boolean,
    val setAmoledBlack: (Boolean) -> Unit,

    val themeVariant: ThemeVariantType,
    val setThemeVariant: (ThemeVariantType) -> Unit,

    val defaultTab: String,
    val setDefaultTab: (String) -> Unit,

    /** true: al desplegar una serie se muestra su progreso combinado en vez de cada temporada. */
    val groupSeasons: Boolean,
    val setGroupSeasons: (Boolean) -> Unit,

    /** "carousel" (por defecto), "timeline" o "list": cómo se listan los episodios. */
    val episodeLayout: String,
    val setEpisodeLayout: (String) -> Unit,

    /**
     * true: la sección de episodios arranca plegada.
     *
     * Se guarda en vez de vivir en la pantalla porque lo que se pide de ella es que la ficha no
     * abra ocupada por la lista. Si el plegado se olvidara al salir, habría que repetirlo en cada
     * serie y no resolvería nada.
     */
    val episodesCollapsed: Boolean,
    val setEpisodesCollapsed: (Boolean) -> Unit,
)
