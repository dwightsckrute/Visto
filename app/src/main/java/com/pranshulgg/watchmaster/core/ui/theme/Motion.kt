package com.pranshulgg.watchmaster.core.ui.theme

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
}
