package com.pranshulgg.watchmaster.feature.calendar.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.theme.ShapeRadius
import com.pranshulgg.watchmaster.core.ui.theme.Spacing
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Elegir mes y año de una vez.
 *
 * Antes solo se podía avanzar de mes en mes con dos flechas, así que volver a lo que viste el año
 * pasado eran doce toques y ninguna pista de dónde había algo. Aquí los años con diario y los doce
 * meses están todos a un toque, con los que tienen algo marcados, de modo que se navega hacia
 * donde hay contenido en vez de a ciegas.
 *
 * Va desplegado bajo la cabecera y no en un diálogo: es una forma de mirar el mismo calendario, no
 * una tarea aparte que haya que aceptar o cancelar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthPicker(
    visibleMonth: YearMonth,
    monthsWithEntries: Set<YearMonth>,
    selectableYears: List<Int>,
    locale: Locale,
    onSelectMonth: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
) {
    // El año que se está mirando es del selector, no del calendario: se puede hojear 2024 sin
    // mover el mes de abajo hasta que se elige uno.
    var browsingYear by remember(visibleMonth.year) { mutableIntStateOf(visibleMonth.year) }
    val years = remember(selectableYears, browsingYear) {
        (selectableYears + browsingYear).distinct().sortedDescending()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShapeRadius.ExtraLarge),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            // Los años, tocables de una vez. Antes eran dos flechas alrededor de un número, y
            // entre ellas y las del mes había cinco controles distintos para moverse por lo
            // mismo. Aquí se ve de un vistazo desde cuándo hay diario y se salta directo.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                years.forEach { year ->
                    YearChip(
                        year = year,
                        isSelected = year == browsingYear,
                        onClick = { browsingYear = year },
                    )
                }
            }

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

@Composable
private fun YearChip(
    year: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    val container by animateColorAsState(
        targetValue = if (isSelected) colorScheme.primary else colorScheme.surfaceContainerHighest,
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "year-chip-container",
    )
    val content by animateColorAsState(
        targetValue = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "year-chip-content",
    )

    Surface(
        onClick = onClick,
        modifier = Modifier.heightIn(min = 40.dp),
        shape = RoundedCornerShape(ShapeRadius.Large),
        color = container,
        contentColor = content,
        border = if (isSelected) null else BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = Spacing.md),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = year.toString(),
                style = MaterialTheme.typography.labelLarge,
            )
        }
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
