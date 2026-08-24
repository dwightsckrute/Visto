package com.dwightsckrute.visto.core.ui.localization

import androidx.core.content.edit
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

    /** El idioma activo, para pedir contenido en él a un catálogo externo. */
    fun currentCode(): String = activeCode

    fun localizedContext(context: Context): Context {
        initialize(context)
        val locale = Locale.forLanguageTag(activeCode)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    fun update(context: Context, code: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { putString(LANGUAGE_KEY, code) }
        activeCode = code
    }

    fun set(activity: ComponentActivity, code: String) {
        update(activity, code)
        activity.recreate()
    }

    fun tmdbLanguage(): String = if (activeCode == "en") "en-US" else "es-ES"
}

@Composable
fun localized(es: String, en: String): String {
    val language = LocalConfiguration.current.locales[0]?.language ?: "es"
    return if (language == "en") en else es
}
