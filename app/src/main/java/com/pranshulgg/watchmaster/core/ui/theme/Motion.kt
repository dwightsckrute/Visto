package com.pranshulgg.watchmaster.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

/**
 * Duraciones de respaldo de Visto. Ver `docs/DESIGN_SYSTEM.md`.
 *
 * La primera opción siempre es `MaterialTheme.motionScheme`: sus specs ya coordinan
 * spring/tween con el resto de Material 3 Expressive. Estas constantes existen solo para
 * las transiciones que necesitan un `tween` explícito, de forma que no aparezca una
 * duración distinta por pantalla.
 */
object AppMotion {
    /** Salidas y desvanecidos breves. */
    const val DurationShort = 200

    /** Entradas y transiciones entre pantallas. */
    const val DurationMedium = 350

    /**
     * Cambios de pantalla completa.
     *
     * Más larga que las demás a propósito: una pantalla entera que se sustituye necesita tiempo
     * para leerse. Con 200 ms el cambio era correcto en el papel y brusco en la mano.
     */
    const val DurationLong = 450

    /**
     * Emphasized decelerate: arranca rápido y frena largo. Es la curva de Material para lo que
     * entra en pantalla.
     *
     * Vive aquí y no en quien la usa porque la comparten dos animaciones que el ojo ve como una
     * sola: la de navegación y la del contenido que llega tarde a esa misma navegación. Si cada
     * una llevara su curva, el empalme entre ambas se notaría.
     */
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
}
