package com.pranshulgg.watchmaster.core.ui.components

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Pestañas de una biblioteca (pendientes, viendo, terminadas) atadas a su pager.
 *
 * La diferencia con un `PrimaryTabRow` normal está en el indicador. Con
 * `selectedTabIndex = pagerState.currentPage` solo se entera cuando la página ya ha cambiado, así
 * que al deslizar el contenido se mueve con el dedo pero el indicador se queda quieto y salta al
 * final. Aquí se interpola con el desplazamiento real del pager, de modo que acompaña al gesto y
 * se puede interrumpir a mitad, que es lo que pide el movimiento de Material 3 Expressive.
 *
 * Se usa `PrimaryTabRow` y no un grupo de botones a propósito: estas secciones son páginas
 * deslizables, y un grupo conectado no se empareja con un gesto de deslizamiento.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaTabRow(
    titles: List<String>,
    pagerState: PagerState,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryTabRow(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        selectedTabIndex = pagerState.currentPage,
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorLayout { measurable, constraints, tabPositions ->
                    if (tabPositions.isEmpty()) {
                        return@tabIndicatorLayout layout(0, 0) {}
                    }

                    val offset = pagerState.currentPageOffsetFraction
                    val from = pagerState.currentPage.coerceIn(tabPositions.indices)
                    // Al deslizar hacia delante el offset es positivo y el destino es la
                    // siguiente pestaña; hacia atrás, la anterior.
                    val to = (from + offset.sign.toInt()).coerceIn(tabPositions.indices)
                    val progress = offset.absoluteValue

                    // El indicador va bajo el texto, no bajo la pestaña entera, así que se
                    // centra con el ancho del contenido en lugar de pegarse al borde izquierdo.
                    fun centeredLeft(index: Int): Float = with(tabPositions[index]) {
                        left.value + (width.value - contentWidth.value) / 2f
                    }

                    val start = lerp(centeredLeft(from), centeredLeft(to), progress)
                    val indicatorWidth = lerp(
                        tabPositions[from].contentWidth.value,
                        tabPositions[to].contentWidth.value,
                        progress,
                    )

                    // Solo se fija el ancho: la altura la decide el propio indicador.
                    val widthPx = indicatorWidth.dp.roundToPx().coerceAtLeast(0)
                    val placeable = measurable.measure(
                        constraints.copy(minWidth = widthPx, maxWidth = widthPx)
                    )
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.place(
                            x = start.dp.roundToPx(),
                            y = constraints.maxHeight - placeable.height,
                        )
                    }
                },
            )
        },
    ) {
        titles.forEachIndexed { index, title ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = { onTabClick(index) },
                text = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

