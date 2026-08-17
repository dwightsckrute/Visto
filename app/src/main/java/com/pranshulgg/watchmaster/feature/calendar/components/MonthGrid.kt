package com.pranshulgg.watchmaster.feature.calendar.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.theme.Spacing
import com.pranshulgg.watchmaster.feature.calendar.WatchedEntry
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Rejilla de un mes. Los días con algo visto llevan un punto; el seleccionado se rellena.
 *
 * La primera columna respeta el primer día de la semana del idioma (lunes en español,
 * domingo en inglés) en lugar de asumir uno fijo.
 */
@Composable
fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate?,
    entriesByDate: Map<LocalDate, List<WatchedEntry>>,
    locale: Locale,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
    val weekDays = (0..6).map { firstDayOfWeek.plus(it.toLong()) }
    val today = LocalDate.now()

    val lengthOfMonth = month.lengthOfMonth()
    val firstOfMonth = month.atDay(1)
    // Cuántas celdas vacías van antes del día 1 para que caiga en su columna.
    val leadingBlanks = ((firstOfMonth.dayOfWeek.value - firstDayOfWeek.value) + 7) % 7
    val cells = leadingBlanks + lengthOfMonth
    val rows = (cells + 6) / 7

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day.shortLabel(locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = Spacing.xs)
                        .clearAndSetSemantics { },
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }

        repeat(rows) { rowIndex ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { columnIndex ->
                    val dayNumber = rowIndex * 7 + columnIndex - leadingBlanks + 1
                    if (dayNumber !in 1..lengthOfMonth) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else {
                        val date = month.atDay(dayNumber)
                        DayCell(
                            date = date,
                            count = entriesByDate[date]?.size ?: 0,
                            isSelected = date == selectedDate,
                            isToday = date == today,
                            onClick = { onSelectDate(date) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    count: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val container by animateColorAsState(
        targetValue = when {
            isSelected -> colorScheme.primary
            count > 0 -> colorScheme.surfaceContainerHighest
            else -> Color.Transparent
        },
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "calendar-day-container",
    )
    val content by animateColorAsState(
        targetValue = when {
            isSelected -> colorScheme.onPrimary
            isToday -> colorScheme.primary
            count > 0 -> colorScheme.onSurface
            else -> colorScheme.onSurfaceVariant
        },
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "calendar-day-content",
    )

    // Una sola descripción por celda: el número suelto no dice nada con TalkBack, y el punto
    // no debe anunciarse como un elemento aparte.
    val description = buildString {
        append(date.dayOfMonth)
        if (count > 0) {
            append(", ")
            append(
                if (count == 1) {
                    localized("1 visto", "1 watched")
                } else {
                    localized("$count vistos", "$count watched")
                }
            )
        }
        if (isToday) {
            append(", ")
            append(localized("hoy", "today"))
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(Spacing.xs)
            .clip(CircleShape)
            .selectable(selected = isSelected, onClick = onClick)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = container,
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(vertical = Spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = content,
                )
                Box(
                    modifier = Modifier.size(4.dp).clip(CircleShape),
                ) {
                    if (count > 0) {
                        Surface(
                            color = if (isSelected) colorScheme.onPrimary else colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier.size(4.dp),
                        ) {}
                    }
                }
            }
        }
    }
}

private fun DayOfWeek.shortLabel(locale: Locale): String =
    getDisplayName(TextStyle.NARROW_STANDALONE, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
