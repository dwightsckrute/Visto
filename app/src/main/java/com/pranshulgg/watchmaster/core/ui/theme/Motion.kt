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

    /**
     * Cambios de pantalla completa.
     *
     * Más larga que las demás a propósito: una pantalla entera que se sustituye necesita tiempo
     * para leerse. Con 200 ms el cambio era correcto en el papel y brusco en la mano.
     */
    const val DurationLong = 450
}
