package com.pranshulgg.watchmaster.core.prefs

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.pranshulgg.watchmaster.core.utils.PreferencesHelper
import com.pranshulgg.watchmaster.core.ui.theme.ThemeVariantType

object AppPrefs {
    private val _appTheme = mutableStateOf("system")
    private val _themeColor = mutableStateOf("#2196f3")
    private val _isCustomTheme = mutableStateOf(false)
    private val _useDynamicColor = mutableStateOf(false)
    private val _useAmoledBlack = mutableStateOf(false)

    private val _defaultTab = mutableStateOf("home")
    private val _groupSeasons = mutableStateOf(false)
    private val _episodeLayout = mutableStateOf("carousel")
    private val _episodesCollapsed = mutableStateOf(false)

    private val _themeVariant =
        mutableStateOf(ThemeVariantType.EXPRESSIVE)


    fun initPrefs(context: Context) {
        PreferencesHelper.init(context)

        _appTheme.value = when (val saved = PreferencesHelper.getString("app_theme")) {
            "Oscuro", "Dark", "dark" -> "dark"
            "Claro", "Light", "light" -> "light"
            else -> "system"
        }
        _themeColor.value = PreferencesHelper.getString("theme_color") ?: "#2196f3"
        _isCustomTheme.value = PreferencesHelper.getBool("isCustomTheme") ?: false
        _useDynamicColor.value = PreferencesHelper.getBool("useDynamicColor") ?: false
        _useAmoledBlack.value = PreferencesHelper.getBool("useAmoledBlack") ?: false
        _themeVariant.value =
            PreferencesHelper.getString("theme_variant")
                ?.let {
                    runCatching { ThemeVariantType.valueOf(it) }.getOrNull()
                }
                ?: ThemeVariantType.EXPRESSIVE
        _groupSeasons.value = PreferencesHelper.getBool("group_seasons") ?: false
        _episodeLayout.value = when (PreferencesHelper.getString("episode_layout")) {
            // El mosaico se retiró y la línea de tiempo ocupa su sitio: quien lo tuviera elegido
            // hereda el reemplazo en vez de volver al carrusel sin haber tocado nada.
            "grid", "timeline" -> "timeline"
            "list" -> "list"
            else -> "carousel"
        }
        _episodesCollapsed.value = PreferencesHelper.getBool("episodes_collapsed") ?: false
        _defaultTab.value = when (val saved = PreferencesHelper.getString("default_tab")) {
            "Películas", "Movies", "movies" -> "movies"
            "Series", "tv" -> "tv"
            else -> "home"
        }
    }

    @Composable
    fun state(): AppPrefsState = AppPrefsState(

        appTheme = _appTheme.value,
        setAppTheme = {
            _appTheme.value = it
            PreferencesHelper.setString("app_theme", it)
        },

        themeColor = _themeColor.value,
        setThemeColor = {
            _themeColor.value = it
            PreferencesHelper.setString("theme_color", it)
        },

        isCustomTheme = _isCustomTheme.value,
        useCustomTheme = {
            _isCustomTheme.value = it
            PreferencesHelper.setBool("isCustomTheme", it)
        },

        useDynamicColor = _useDynamicColor.value,
        setDynamicColor = {
            _useDynamicColor.value = it
            PreferencesHelper.setBool("useDynamicColor", it)
        },

        useAmoledBlack = _useAmoledBlack.value,
        setAmoledBlack = {
            _useAmoledBlack.value = it
            PreferencesHelper.setBool("useAmoledBlack", it)
        },

        themeVariant = _themeVariant.value,
        setThemeVariant = {
            _themeVariant.value = it
            PreferencesHelper.setString("theme_variant", it.name)
        },

        defaultTab = _defaultTab.value,
        setDefaultTab = {
            _defaultTab.value = it
            PreferencesHelper.setString("default_tab", it)
        },

        groupSeasons = _groupSeasons.value,
        setGroupSeasons = {
            _groupSeasons.value = it
            PreferencesHelper.setBool("group_seasons", it)
        },

        episodeLayout = _episodeLayout.value,
        setEpisodeLayout = {
            _episodeLayout.value = it
            PreferencesHelper.setString("episode_layout", it)
        },

        episodesCollapsed = _episodesCollapsed.value,
        setEpisodesCollapsed = {
            _episodesCollapsed.value = it
            PreferencesHelper.setBool("episodes_collapsed", it)
        },
    )
}
