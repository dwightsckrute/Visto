package com.dwightsckrute.visto.core.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.dwightsckrute.visto.core.ui.localization.AppLanguage
import com.dwightsckrute.visto.data.repository.idToOpenLibraryKey

/**
 * Comparte una ficha con un enlace a su catálogo.
 *
 * El enlace depende de dónde salió la ficha: una película o una serie viven en TMDB y un libro en
 * Open Library. Compartir un libro no hacía nada porque la acción se dejó vacía al montar su
 * pantalla; con un identificador de libro, la dirección de TMDB habría llevado a una página que
 * no existe, que es peor que no compartir.
 */
fun shareMedia(
    context: Context,
    title: String,
    tmdbId: Long,
    isTv: Boolean,
    isBook: Boolean = false,
) {
    val url = if (isBook) {
        idToOpenLibraryKey(tmdbId)?.let { "https://openlibrary.org/works/$it" }
    } else {
        "https://www.themoviedb.org/${if (isTv) "tv" else "movie"}/$tmdbId"
    }

    val verb = if (isBook) {
        AppLanguage.text("He leído", "I read")
    } else {
        AppLanguage.text("He visto", "I watched")
    }
    val message = listOfNotNull(
        AppLanguage.text("$verb «$title» en Visto.", "$verb “$title” on Visto."),
        url,
    ).joinToString("\n")
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, message)
    }
    val chooser = Intent.createChooser(
        sendIntent,
        AppLanguage.text("Compartir con", "Share with"),
    )
    if (context !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}
