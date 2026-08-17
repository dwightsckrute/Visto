package com.dwightsckrute.visto.feature.calendar.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.theme.Spacing

/** El tramo de años que ofrece la ruleta. Fuera de aquí no hay diario que valga. */
private val YearRange = 1900..2100

/** Alto de cada año en la ruleta. De él salen el centrado y el ajuste al soltar. */
private val ItemHeight = 48.dp

/** Cuántos años se ven a la vez. Impar a propósito: hay uno en el centro. */
private const val VisibleItems = 5

/**
 * Elegir el año en una ruleta.
 *
 * Antes el año se escribía en el propio mando, sustituyendo el número por un campo. Funcionaba,
 * pero abría el teclado encima de medio calendario para pedir cuatro dígitos, y hasta que no
 * escribías no sabías si aquello aceptaba texto. Una ruleta se entiende sin instrucciones, se
 * mueve con el mismo dedo con el que ya estabas navegando y no tapa nada.
 *
 * Se decide al aceptar. Arrastrando se pasa por veinte años que no quieres, y aplicar cada uno al
 * vuelo dejaría el calendario de detrás dando saltos.
 */
@Composable
fun YearPickerDialog(
    initialYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val years = remember { YearRange.toList() }
    val initialIndex = remember(initialYear) {
        years.indexOf(initialYear).coerceAtLeast(0)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    // El año elegido es el que queda en el centro. Con el ajuste al soltar, el desplazamiento
    // acaba en cero, pero a media inercia hay que redondear al más cercano.
    val selectedIndex by remember {
        derivedStateOf {
            val offset = listState.firstVisibleItemScrollOffset
            val height = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 1
            listState.firstVisibleItemIndex + if (offset > height / 2) 1 else 0
        }
    }
    val selectedYear = years.getOrElse(selectedIndex) { initialYear }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localized("Elegir año", "Pick a year")) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ItemHeight * VisibleItems),
                contentAlignment = Alignment.Center,
            ) {
                // La banda del centro marca dónde cae la elección. Sin ella la ruleta gira sin
                // decir cuál de los cinco años es el que se está eligiendo.
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ItemHeight),
                    shape = RoundedCornerShape(ShapeRadius.Large),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {}

                LazyColumn(
                    state = listState,
                    flingBehavior = rememberSnapFlingBehavior(listState),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // El relleno de arriba y abajo es lo que deja al primer y último año
                    // llegar al centro; sin él, 1900 y 2100 no se podrían elegir.
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        vertical = ItemHeight * (VisibleItems / 2),
                    ),
                    verticalArrangement = Arrangement.Top,
                ) {
                    items(years, key = { it }) { year ->
                        val isSelected = year == selectedYear
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ItemHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = year.toString(),
                                style = if (isSelected) {
                                    MaterialTheme.typography.headlineSmall
                                } else {
                                    MaterialTheme.typography.titleMedium
                                },
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedYear) }) {
                Text(localized("Aceptar", "OK"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localized("Cancelar", "Cancel"))
            }
        },
        modifier = Modifier.padding(Spacing.md),
    )
}
