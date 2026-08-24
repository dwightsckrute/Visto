package com.dwightsckrute.visto.feature.shared.media.ui.watchstatus

import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.localization.AppLanguage

// Estas son funciones normales, no composables, así que el idioma se resuelve con
// AppLanguage.text en lugar de localized().

/**
 * Lo que se pregunta antes de cambiar de estado.
 *
 * Tres vocabularios y no dos: una temporada se ve, una película se ve, y un libro se lee. Sin la
 * tercera rama, confirmar el estado de un libro preguntaba por "esta película".
 */
fun WatchStatus.dialogMessage(isTv: Boolean = false, isBook: Boolean = false): String {
    if (isBook) return bookDialogMessage()
    return when (this) {
        WatchStatus.WATCHING -> if (isTv) {
            AppLanguage.text(
                "¿Quieres terminar esta temporada y marcar todos sus episodios como vistos?",
                "Finish this season and mark all its episodes as watched?",
            )
        } else {
            AppLanguage.text(
                "¿Quieres marcar esta película como terminada?",
                "Mark this movie as finished?",
            )
        }

        WatchStatus.INTERRUPTED -> if (isTv) {
            AppLanguage.text(
                "¿Quieres continuar viendo esta temporada?",
                "Carry on watching this season?",
            )
        } else {
            AppLanguage.text(
                "¿Quieres continuar viendo esta película?",
                "Carry on watching this movie?",
            )
        }

        WatchStatus.FINISHED -> if (isTv) {
            AppLanguage.text(
                "¿Quieres restablecer esta temporada, marcar sus episodios como no vistos y devolverla a pendientes?",
                "Reset this season, mark its episodes as unwatched and send it back to the watchlist?",
            )
        } else {
            AppLanguage.text(
                "¿Quieres restablecer esta película y devolverla a pendientes?",
                "Reset this movie and send it back to the watchlist?",
            )
        }

        else -> if (isTv) {
            AppLanguage.text(
                "¿Quieres empezar a ver esta temporada?",
                "Start watching this season?",
            )
        } else {
            AppLanguage.text(
                "¿Quieres empezar a ver esta película?",
                "Start watching this movie?",
            )
        }
    }
}

fun WatchStatus.confirmAction(
    start: () -> Unit,
    reset: () -> Unit,
    finish: () -> Unit,
) {
    when (this) {
        WatchStatus.WATCHING -> finish()
        WatchStatus.FINISHED -> reset()
        else -> start()
    }
}

/**
 * Lo que dice el botón grande de la cápsula flotante.
 *
 * El mismo tercer vocabulario que los diálogos, que aquí faltaba: la cápsula de un libro ofrecía
 * "marcar como terminada" y "continuar viendo". El diálogo que salía después sí hablaba de leer,
 * así que el botón y su confirmación decían cosas distintas del mismo gesto.
 */
fun WatchStatus.actionLabel(isBook: Boolean = false): String = when (this) {
    WatchStatus.WATCHING -> if (isBook) {
        AppLanguage.text("Marcar como leído", "Mark as read")
    } else {
        AppLanguage.text("Marcar como terminada", "Mark as finished")
    }

    WatchStatus.INTERRUPTED -> if (isBook) {
        AppLanguage.text("Continuar leyendo", "Carry on reading")
    } else {
        AppLanguage.text("Continuar viendo", "Carry on watching")
    }

    WatchStatus.FINISHED -> AppLanguage.text("Restablecer", "Reset")

    else -> if (isBook) {
        AppLanguage.text("Empezar a leer", "Start reading")
    } else {
        AppLanguage.text("Marcar como en curso", "Mark as watching")
    }
}

fun WatchStatus.buttonIcon(isBook: Boolean = false): Int = when (this) {
    WatchStatus.WATCHING -> R.drawable.check_24px
    WatchStatus.FINISHED -> R.drawable.restart_alt_24px
    // Un libro no se reproduce.
    else -> if (isBook) R.drawable.book_24px else R.drawable.play_arrow_24px
}

private fun WatchStatus.bookDialogMessage(): String = when (this) {
    WatchStatus.WATCHING -> AppLanguage.text(
        "¿Quieres marcar este libro como leído?",
        "Mark this book as read?",
    )

    WatchStatus.INTERRUPTED -> AppLanguage.text(
        "¿Quieres seguir leyendo este libro?",
        "Carry on reading this book?",
    )

    WatchStatus.FINISHED -> AppLanguage.text(
        "¿Quieres restablecer este libro y devolverlo a pendientes?",
        "Reset this book and send it back to your to-read list?",
    )

    else -> AppLanguage.text(
        "¿Quieres empezar a leer este libro?",
        "Start reading this book?",
    )
}
