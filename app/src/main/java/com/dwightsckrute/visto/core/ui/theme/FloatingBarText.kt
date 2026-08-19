package com.dwightsckrute.visto.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle

/**
 * La etiqueta de las dos barras flotantes: la de navegación y la de acciones de una ficha.
 *
 * Existe porque no coincidían. La de navegación usaba el `labelMedium` que trae por defecto
 * `ShortNavigationBarItem` y la de acciones un `titleMedium`, un 35% más alto medido en pantalla,
 * y las dos ocupan el mismo sitio en el mismo borde: se ven una detrás de otra al entrar en una
 * ficha y saltaba a la vista que no eran la misma pieza.
 *
 * `labelLarge` porque es el punto en el que ninguna de las dos empeora: la de navegación gana algo
 * de legibilidad y la acción principal no se queda en letra de nota al pie.
 */
val floatingBarLabel: TextStyle
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.typography.labelLarge
