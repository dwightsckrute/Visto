package com.pranshulgg.watchmaster.feature.shared.media.ui.watchstatus

import com.pranshulgg.watchmaster.core.model.WatchStatus
import com.pranshulgg.watchmaster.R

fun WatchStatus.dialogMessage(isTv: Boolean = false): String {
    return when (this) {
        WatchStatus.WATCHING -> if (isTv) {
            "¿Quieres terminar esta temporada y marcar todos sus episodios como vistos?"
        } else {
            "¿Quieres marcar esta película como terminada?"
        }
        WatchStatus.INTERRUPTED -> if (isTv) {
            "¿Quieres continuar viendo esta temporada?"
        } else {
            "¿Quieres continuar viendo esta película?"
        }
        WatchStatus.FINISHED -> if (isTv) {
            "¿Quieres restablecer esta temporada, marcar sus episodios como no vistos y devolverla a pendientes?"
        } else {
            "¿Quieres restablecer esta película y devolverla a pendientes?"
        }
        else -> if (isTv) {
            "¿Quieres empezar a ver esta temporada?"
        } else {
            "¿Quieres empezar a ver esta película?"
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
        WatchStatus.WATCHING -> "Marcar como terminada"
        WatchStatus.INTERRUPTED -> "Continuar viendo"
        WatchStatus.FINISHED -> "Restablecer"
        else -> "Marcar como en curso"
    }

val WatchStatus.buttonIcon: Int
    get() = when (this) {
        WatchStatus.WATCHING -> R.drawable.check_24px
        WatchStatus.FINISHED -> R.drawable.restart_alt_24px
        else -> R.drawable.play_arrow_24px
    }
