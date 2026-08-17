package com.pranshulgg.watchmaster.feature.main.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.theme.Elevation
import com.pranshulgg.watchmaster.feature.main.MainDestination

/**
 * Navegación principal flotante de Visto.
 *
 * Es una composición fina, no un componente reinventado: la semántica de navegación, el
 * indicador de selección y su animación los aporta el `ShortNavigationBar` oficial de
 * Material 3 Expressive. Lo único propio es el contenedor exterior en píldora, porque
 * `ShortNavigationBar` no expone `shape`.
 *
 * No es una API de Material; el nombre es interno del proyecto. Ver `docs/DESIGN_SYSTEM.md`.
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
        ShortNavigationBar(
            // El tono y la sombra los pone la píldora exterior; la barra solo aporta
            // semántica y disposición. Sin insets propios: los resuelve quien la coloca.
            containerColor = Color.Transparent,
            contentColor = colorScheme.onPrimaryContainer,
            windowInsets = WindowInsets(0, 0, 0, 0),
            arrangement = ShortNavigationBarArrangement.Centered,
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
