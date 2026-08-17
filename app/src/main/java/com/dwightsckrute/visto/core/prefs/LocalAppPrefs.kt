package com.dwightsckrute.visto.core.prefs

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppPrefs = staticCompositionLocalOf<AppPrefsState> {
    error("LocalAppPrefs not provided")
}
