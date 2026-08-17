package com.dwightsckrute.visto.feature.shared.media.ui.watchstatus

import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.localization.AppLanguage

// Estas son funciones normales, no composables, así que el idioma se resuelve con
// AppLanguage.text en lugar de localized().

fun WatchStatus.dialogMessage(isTv: Boolean = false): String {
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

val WatchStatus.actionLabel: String
    get() = when (this) {
        WatchStatus.WATCHING -> AppLanguage.text("Marcar como terminada", "Mark as finished")
        WatchStatus.INTERRUPTED -> AppLanguage.text("Continuar viendo", "Carry on watching")
        WatchStatus.FINISHED -> AppLanguage.text("Restablecer", "Reset")
        else -> AppLanguage.text("Marcar como en curso", "Mark as watching")
    }

val WatchStatus.buttonIcon: Int
    get() = when (this) {
        WatchStatus.WATCHING -> R.drawable.check_24px
        WatchStatus.FINISHED -> R.drawable.restart_alt_24px
        else -> R.drawable.play_arrow_24px
    }
