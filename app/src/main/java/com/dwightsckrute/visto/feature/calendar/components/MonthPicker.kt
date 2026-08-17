package com.dwightsckrute.visto.feature.calendar.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.theme.Spacing
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Elegir mes y año de una vez.
 *
 * Antes solo se podía avanzar de mes en mes con dos flechas, así que volver a lo que viste el año
 * pasado eran doce toques y ninguna pista de dónde había algo. Aquí los doce meses están a un
 * toque y los que tienen algo van marcados, de modo que se navega hacia donde hay contenido en
 * vez de a ciegas.
 *
 * Va desplegado bajo la cabecera y no en un diálogo: es una forma de mirar el mismo calendario, no
 * una tarea aparte que haya que aceptar o cancelar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthPicker(
    visibleMonth: YearMonth,
    monthsWithEntries: Set<YearMonth>,
    locale: Locale,
    onSelectMonth: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
) {
    // El año que se está mirando es del selector, no del calendario: se puede hojear 2024 sin
    // mover el mes de abajo hasta que se elige uno.
    var browsingYear by remember(visibleMonth.year) { mutableIntStateOf(visibleMonth.year) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShapeRadius.ExtraLarge),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            // El año, en un paso a paso de una sola pieza.
            //
            // Ha sido lo más difícil de acertar aquí. Con dos flechas sueltas eran cinco mandos
            // de flecha en la pantalla; con una lista de años tocables se llegaba a cualquiera
            // pero cuarenta y tres fichas para elegir un número recargaban la tarjeta entera.
            // Esto es lo mismo que la lista —cualquier año está a unos toques— ocupando lo que
            // ocupa un número.
            YearStepper(
                year = browsingYear,
                onPrevious = { browsingYear-- },
                onNext = { browsingYear++ },
                onSetYear = { browsingYear = it },
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                maxItemsInEachRow = 4,
            ) {
                Month.entries.forEach { month ->
                    val candidate = YearMonth.of(browsingYear, month)
                    MonthChip(
                        label = month
                            .getDisplayName(TextStyle.SHORT_STANDALONE, locale)
                            .replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(locale) else it.toString()
                            }
                            .trimEnd('.'),
                        isSelected = candidate == visibleMonth,
                        hasEntries = candidate in monthsWithEntries,
                        onClick = { onSelectMonth(candidate) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * Un año, y una flecha a cada lado.
 *
 * Las tres piezas van dentro de una sola pastilla en vez de sueltas: así se lee como un mando y
 * no como tres cosas puestas en fila, que es lo que hacía que un par de flechas más pareciera un
 * exceso de flechas.
 *
 * Tocar el año abre la ruleta. Las flechas van bien para el año pasado y son un castigo para
 * 2014; la ruleta cuesta lo mismo esté cerca o lejos.
 */
@Composable
private fun YearStepper(
    year: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSetYear: (Int) -> Unit,
) {
    var picking by remember { mutableStateOf(false) }
    val yearDescription = localized(
        "$year, tocar para elegir un año",
        "$year, tap to pick a year",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(ShapeRadius.Full),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious) {
                    Symbol(
                        icon = R.drawable.keyboard_arrow_down_24px,
                        desc = localized("Año anterior", "Previous year"),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.rotate(90f),
                    )
                }
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(ShapeRadius.Small))
                        .clickable { picking = true }
                        .widthIn(min = 64.dp)
                        .padding(vertical = Spacing.sm)
                        .clearAndSetSemantics { contentDescription = yearDescription },
                )
                IconButton(onClick = onNext) {
                    Symbol(
                        icon = R.drawable.keyboard_arrow_down_24px,
                        desc = localized("Año siguiente", "Next year"),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.rotate(-90f),
                    )
                }
            }
        }
    }

    if (picking) {
        YearPickerDialog(
            initialYear = year,
            onDismiss = { picking = false },
            onConfirm = {
                onSetYear(it)
                picking = false
            },
        )
    }
}

@Composable
private fun MonthChip(
    label: String,
    isSelected: Boolean,
    hasEntries: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    val container by animateColorAsState(
        targetValue = when {
            isSelected -> colorScheme.primary
            hasEntries -> colorScheme.secondaryContainer
            else -> colorScheme.surfaceContainerHighest
        },
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "month-chip-container",
    )
    val content by animateColorAsState(
        targetValue = when {
            isSelected -> colorScheme.onPrimary
            hasEntries -> colorScheme.onSecondaryContainer
            // Un mes sin nada no se desactiva: se puede mirar igual, solo que se anuncia que
            // está vacío antes de ir.
            else -> colorScheme.onSurfaceVariant
        },
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "month-chip-content",
    )

    val state = when {
        isSelected -> localized("mes mostrado", "shown month")
        hasEntries -> localized("con actividad", "has activity")
        else -> localized("sin actividad", "no activity")
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 48.dp)
            .clearAndSetSemantics { contentDescription = "$label, $state" },
        shape = RoundedCornerShape(ShapeRadius.Large),
        color = container,
        contentColor = content,
        border = if (isSelected) null else BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}
