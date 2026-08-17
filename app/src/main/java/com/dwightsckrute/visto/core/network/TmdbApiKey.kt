package com.dwightsckrute.visto.core.network

import com.dwightsckrute.visto.BuildConfig
import com.dwightsckrute.visto.core.utils.PreferencesHelper

/**
 * Resuelve qué clave de TMDB usar en cada petición.
 *
 * La clave propia es opcional: si el usuario no configura ninguna, se sigue usando la incluida
 * en la compilación. Se consulta por petición, no al construir Retrofit, para que cambiarla
 * surta efecto sin reiniciar la aplicación.
 */
object TmdbApiKey {

    const val PREF_KEY = "tmdb_api_key"

    /** La clave del usuario, o null si no ha puesto ninguna. */
    fun userKey(): String? = PreferencesHelper.getString(PREF_KEY)?.trim()?.takeIf { it.isNotEmpty() }

    fun current(): String = userKey() ?: BuildConfig.TMDB_API_KEY

    fun isUserProvided(): Boolean = userKey() != null

    fun set(value: String) {
        PreferencesHelper.setString(PREF_KEY, value.trim())
    }

    fun clear() {
        PreferencesHelper.setString(PREF_KEY, "")
    }
}
