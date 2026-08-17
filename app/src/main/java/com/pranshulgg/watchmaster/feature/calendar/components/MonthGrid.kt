package com.pranshulgg.watchmaster.feature.calendar.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
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
 * Rejilla de un mes.
 *
 * La primera columna respeta el primer día de la semana del idioma (lunes en español, domingo en
 * inglés) en lugar de asumir uno fijo.
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
                    textAlign = TextAlign.Center,
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

/**
 * Un día.
 *
 * El estado lo lleva la forma, no un adorno: el día elegido deja de ser un círculo y se convierte
 * en la figura de nueve lóbulos de Material, que es justo lo que propone Material 3 Expressive
 * —que la silueta signifique algo— y lo que hace que se localice de un vistazo entre treinta y un
 * círculos iguales.
 *
 * Los días con algo visto se rellenan. Antes llevaban un punto de cuatro puntos sobre un fondo
 * casi del color de la tarjeta, y a un palmo de distancia el mes se veía vacío tanto si habías
 * visto diez cosas como ninguna. Rellenos, el mes se lee sin leer un solo número.
 */
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
    val hasEntries = count > 0

    val container by animateColorAsState(
        targetValue = when {
            isSelected -> colorScheme.primary
            hasEntries -> colorScheme.secondaryContainer
            else -> Color.Transparent
        },
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "calendar-day-container",
    )
    val content by animateColorAsState(
        targetValue = when {
            isSelected -> colorScheme.onPrimary
            hasEntries -> colorScheme.onSecondaryContainer
            isToday -> colorScheme.primary
            else -> colorScheme.onSurfaceVariant
        },
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "calendar-day-content",
    )
    // Un empujón de tamaño al elegir. El cambio de forma es instantáneo —morfear entre dos
    // siluetas es otra cosa y no compensa aquí—, y este crecimiento lo acompaña para que se lea
    // como un movimiento y no como un parpadeo.
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "calendar-day-scale",
    )

    val shape = if (isSelected) MaterialShapes.Cookie9Sided.toShape() else CircleShape

    // Una sola descripción por celda: el número suelto no dice nada con TalkBack, y el relleno
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
            .padding(2.dp)
            .clip(shape)
            .selectable(selected = isSelected, onClick = onClick)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = container,
            shape = shape,
            // Hoy va con aro y no con relleno: el relleno ya significa "aquí viste algo", y
            // gastarlo también en la fecha de hoy haría que un día vacío pareciera lleno.
            border = if (isToday && !isSelected) {
                BorderStroke(1.5.dp, colorScheme.primary)
            } else {
                null
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = content,
                )
            }
        }

        // Cuántas cosas, cuando fue más de una. Va fuera de la superficie para que el crecimiento
        // del día elegido no se lo lleve por delante.
        if (count > 1) {
            Surface(
                color = colorScheme.tertiary,
                contentColor = colorScheme.onTertiary,
                shape = CircleShape,
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Box(modifier = Modifier.size(15.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

private fun DayOfWeek.shortLabel(locale: Locale): String =
    getDisplayName(TextStyle.NARROW_STANDALONE, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
