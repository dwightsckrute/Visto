package com.pranshulgg.watchmaster.core.ui.localization

import android.content.Context
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

object AppLanguage {
    private const val PREF_NAME = "app_prefs"
    private const val LANGUAGE_KEY = "app_language"

    @Volatile
    private var activeCode: String = "es"

    fun code(context: Context): String = context
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .getString(LANGUAGE_KEY, "es")
        .takeUnless { it.isNullOrBlank() }
        ?: "es"

    fun initialize(context: Context) {
        activeCode = code(context)
    }

    fun text(es: String, en: String): String = if (activeCode == "en") en else es

    fun localizedContext(context: Context): Context {
        initialize(context)
        val locale = Locale.forLanguageTag(activeCode)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    fun set(activity: ComponentActivity, code: String) {
        activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_KEY, code)
            .apply()
        activeCode = code
        activity.recreate()
    }

    fun tmdbLanguage(): String = if (activeCode == "en") "en-US" else "es-ES"
}

@Composable
fun localized(es: String, en: String): String {
    val language = LocalConfiguration.current.locales[0]?.language ?: "es"
    return if (language == "en") en else es
}
