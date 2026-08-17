package com.pranshulgg.watchmaster.feature.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.components.Tooltip
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.theme.Spacing
import com.pranshulgg.watchmaster.feature.main.MainDestination

/**
 * Zona inferior de la pantalla principal: la navegación flotante y, separada de ella, la acción
 * global de búsqueda.
 *
 * Es el único punto donde se resuelven los insets inferiores de esta barra, para que las
 * pantallas de contenido no los vuelvan a sumar por su cuenta.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainBottomBar(
    selected: MainDestination,
    onSelect: (MainDestination) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Spacing.lg,
                end = Spacing.lg,
                top = Spacing.lg,
                bottom = bottomInset + Spacing.lg,
            ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppFloatingNavigationBar(
            // `fill = false` es obligatorio, no un detalle de estilo. ShortNavigationBar mide
            // cada destino con `constraints.constrain(Constraints.fixed(anchoDelItem))`: si
            // recibe constraints de ancho fijo, ese constrain eleva cada item al ancho completo
            // de la barra y todos menos el primero se colocan fuera de pantalla. Con
            // `fill = false` el mínimo es 0 y la medida de cada item se respeta.
            // Tampoco sirve `width(IntrinsicSize.Min)` para que la píldora se ajuste al
            // contenido: CenteredContentMeasurePolicy hace `layout(constraints.maxWidth)` y en
            // la pasada de intrínsecos ese maxWidth es infinito, lo que revienta con
            // "Size(2147483647 x 0) is out of range". Ambos casos verificados en dispositivo
            // con M3 1.5.0-alpha17.
            modifier = Modifier.weight(1f, fill = false),
            selected = selected,
            onSelect = onSelect,
        )

        Tooltip(
            localized("Buscar películas y series", "Search movies and TV shows"),
            preferredPosition = TooltipAnchorPosition.Above,
            spacing = 10.dp,
        ) {
            FloatingActionButton(
                // 56 dp es el tamaño oficial del FAB, no un valor arbitrario.
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onSearchClick,
            ) {
                Symbol(
                    icon = R.drawable.search_24px,
                    desc = localized("Buscar", "Search"),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
