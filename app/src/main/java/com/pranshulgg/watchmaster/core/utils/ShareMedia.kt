package com.pranshulgg.watchmaster.core.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.pranshulgg.watchmaster.core.ui.localization.AppLanguage

fun shareMedia(
    context: Context,
    title: String,
    tmdbId: Long,
    isTv: Boolean,
) {
    val mediaType = if (isTv) "tv" else "movie"
    val url = "https://www.themoviedb.org/$mediaType/$tmdbId"
    val message = AppLanguage.text(
        "He visto «$title» en Visto.\n$url",
        "I watched “$title” on Visto.\n$url",
    )
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
