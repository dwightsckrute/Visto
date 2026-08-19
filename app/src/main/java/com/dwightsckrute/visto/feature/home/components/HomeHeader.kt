package com.dwightsckrute.visto.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.dwightsckrute.visto.core.ui.components.AppPill
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.theme.Spacing
import java.time.LocalTime

/**
 * Saludo de bienvenida según la hora, con una línea de contexto real.
 *
 * Sustituye a la tarjeta con borde que abría la pantalla. Va suelto sobre el fondo a propósito:
 * la jerarquía la marca la tipografía y no un contenedor más, que es lo que pide Material 3
 * Expressive y lo que evita apilar tarjetas dentro de tarjetas.
 */
@Composable
fun HomeGreeting(
    inProgress: Int,
    watchedThisMonth: Int,
    modifier: Modifier = Modifier,
) {
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 6 -> localized("Buenas noches", "Good night")
        hour < 13 -> localized("Buenos días", "Good morning")
        hour < 21 -> localized("Buenas tardes", "Good afternoon")
        else -> localized("Buenas noches", "Good evening")
    }

    val context = when {
        inProgress > 0 && watchedThisMonth > 0 -> localized(
            "Tienes $inProgress en curso y llevas $watchedThisMonth este mes",
            "$inProgress in progress, and $watchedThisMonth finished this month",
        )

        inProgress > 0 -> localized(
            "Tienes $inProgress en curso",
            "$inProgress in progress",
        )

        watchedThisMonth > 0 -> localized(
            "Llevas $watchedThisMonth este mes",
            "$watchedThisMonth finished this month",
        )

        else -> localized(
            "Buen momento para empezar algo",
            "A good moment to start something",
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = context,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Una cifra de la biblioteca, con su pareja de color semántica. */
data class HomeStat(
    val label: String,
    val value: Int,
    val icon: Int,
    val container: Color,
    val onContainer: Color,
)

/**
 * Cifras de la biblioteca, todas a la vista.
 *
 * Iban en una fila desplazable, y desplazar para leer un resumen es contradictorio: el resumen
 * existe para no tener que buscar. En dos columnas caben las cinco sin desplazar y sin comprimir
 * nada, que era el motivo por el que se puso en horizontal.
 *
 * Cada cifra sigue siendo su propia píldora tonal. La versión anterior a la fila las metía en una
 * tarjeta con borde, y el borde va contra lo que dice `docs/DESIGN_SYSTEM.md`.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeStatsRow(
    stats: List<HomeStat>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        maxItemsInEachRow = 2,
    ) {
        stats.forEach { stat ->
            AppPill(
                label = stat.label,
                value = stat.value.toString(),
                icon = stat.icon,
                container = stat.container,
                onContainer = stat.onContainer,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
