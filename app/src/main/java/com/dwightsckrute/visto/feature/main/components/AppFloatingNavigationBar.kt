package com.dwightsckrute.visto.feature.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.theme.Elevation
import com.dwightsckrute.visto.core.ui.theme.Spacing
import com.dwightsckrute.visto.feature.main.MainDestination

/**
 * Navegación principal flotante de Visto: una píldora compacta que se ajusta a su contenido y
 * deja respirar el fondo a los lados, en lugar de ocupar todo el ancho.
 *
 * Usa los `ShortNavigationBarItem` oficiales de Material 3 Expressive, que aportan la semántica
 * de selección y la animación del indicador, pero **no** el contenedor `ShortNavigationBar`:
 * ese contenedor siempre se dimensiona a `constraints.maxWidth` y no soporta medición
 * intrínseca, así que con él la píldora no puede ajustarse al contenido. Los items, en cambio,
 * usan `defaultMinSize` y se miden por su contenido dentro de un `Row` normal.
 *
 * El nombre es interno del proyecto, no una API de Material. Ver `docs/DESIGN_SYSTEM.md`.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppFloatingNavigationBar(
    selected: MainDestination,
    onSelect: (MainDestination) -> Unit,
    modifier: Modifier = Modifier,
    destinations: List<MainDestination> = MainDestination.entries,
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = colorScheme.primaryContainer,
        contentColor = colorScheme.onPrimaryContainer,
        shadowElevation = Elevation.floating,
    ) {
        Row(
            // selectableGroup lo aportaba ShortNavigationBar; al no usarlo, la semántica de
            // grupo de selección única para accesibilidad hay que declararla aquí.
            modifier = Modifier
                .selectableGroup()
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEach { destination ->
                val isSelected = destination == selected
                ShortNavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelect(destination) },
                    icon = {
                        Symbol(
                            icon = if (isSelected) {
                                destination.selectedIcon
                            } else {
                                destination.unselectedIcon
                            },
                            color = if (isSelected) {
                                colorScheme.onSurface
                            } else {
                                colorScheme.onPrimaryContainer
                            },
                        )
                    },
                    // Solo el destino activo muestra etiqueta. ShortNavigationBarItem la
                    // mostraría siempre si se le pasa, y tres etiquetas en español permanentes
                    // desbordarían la píldora en pantallas estrechas o con font scale alto.
                    label = if (isSelected) {
                        {
                            Text(
                                text = destination.label,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                            )
                        }
                    } else {
                        null
                    },
                    // La etiqueta acompaña al icono en lugar de colocarse debajo, para que la
                    // barra siga siendo una píldora baja y el destino activo se lea de un vistazo.
                    iconPosition = NavigationItemIconPosition.Start,
                    colors = ShortNavigationBarItemDefaults.colors(
                        selectedIndicatorColor = colorScheme.surfaceContainer,
                        selectedIconColor = colorScheme.onSurface,
                        selectedTextColor = colorScheme.onSurface,
                        unselectedIconColor = colorScheme.onPrimaryContainer,
                        unselectedTextColor = colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }
    }
}
