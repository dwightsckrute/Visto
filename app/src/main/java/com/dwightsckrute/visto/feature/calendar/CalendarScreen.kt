package com.dwightsckrute.visto.feature.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.EmptyContainerPlaceholder
import com.dwightsckrute.visto.core.ui.components.LargeTopBarScaffold
import com.dwightsckrute.visto.core.ui.components.NavigateUpBtn
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.components.Tooltip
import com.dwightsckrute.visto.core.ui.localization.AppLanguage
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.navigation.NavRoutes
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.theme.Spacing
import com.dwightsckrute.visto.feature.calendar.components.MonthGrid
import com.dwightsckrute.visto.feature.calendar.components.MonthPicker
import com.dwightsckrute.visto.feature.calendar.components.WatchedEntryRow
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Diario de lo visto: un calendario construido sobre `finishedDate`, que Visto ya guardaba al
 * marcar algo como terminado. No introduce entidades ni migraciones; solo lee.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalendarScreen(navController: NavController) {
    val viewModel: CalendarViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val locale = if (AppLanguage.code(LocalContext.current) == "en") {
        Locale.US
    } else {
        Locale.forLanguageTag("es-ES")
    }

    LargeTopBarScaffold(
        title = localized("Diario", "Diary"),
        navigationIcon = { NavigateUpBtn(navController) },
        actions = {
            Tooltip(localized("Ir a hoy", "Go to today")) {
                IconButton(onClick = viewModel::goToToday) {
                    Symbol(
                        icon = R.drawable.date_range_24px,
                        desc = localized("Ir a hoy", "Go to today"),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
    ) { padding ->
        CalendarContent(
            uiState = uiState,
            locale = locale,
            padding = padding,
            onPreviousMonth = viewModel::showPreviousMonth,
            onNextMonth = viewModel::showNextMonth,
            onSelectMonth = viewModel::showMonth,
            onToggleMonthPicker = viewModel::toggleMonthPicker,
            onSelectDate = viewModel::selectDate,
            onEntryClick = { entry ->
                if (entry.mediaType == "tv") {
                    navController.navigate(NavRoutes.tvDetail(entry.id, 1, -1))
                } else {
                    navController.navigate(NavRoutes.movieDetail(entry.id))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CalendarContent(
    uiState: CalendarUiState,
    locale: Locale,
    padding: PaddingValues,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onToggleMonthPicker: () -> Unit,
    onSelectDate: (java.time.LocalDate) -> Unit,
    onEntryClick: (WatchedEntry) -> Unit,
) {
    // transitionSpec no es ámbito @Composable: las specs se resuelven fuera.
    // Rápidas y no las de por defecto: cambiar de mes es hojear, y hojear no admite espera. Con
    // la spec normal el mes tardaba en asentarse y el gesto entero se sentía pesado.
    val slideSpec = motionScheme.fastSpatialSpec<IntOffset>()
    val fadeSpec = motionScheme.fastEffectsSpec<Float>()
    // El alto y el alpha del selector comparten reloj, para que no quede un hueco vacío mientras
    // se cierra.
    val pickerSizeSpec = motionScheme.defaultSpatialSpec<IntSize>()
    val pickerFadeSpec = motionScheme.defaultSpatialSpec<Float>()

    if (!uiState.isLoading && !uiState.hasAnyEntry) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            EmptyContainerPlaceholder(
                icon = R.drawable.date_range_24px,
                text = localized("Aún no hay nada visto", "Nothing watched yet"),
                description = localized("Cuando marques una película o serie como terminada, aparecerá aquí el día en que la viste.", "When you mark a movie or show as finished, the day you watched it shows up here."),
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            bottom = Spacing.xxl,
            start = Spacing.lg,
            end = Spacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item(key = "month-header") {
            MonthHeader(
                month = uiState.visibleMonth,
                movies = uiState.visibleMonthMovies,
                shows = uiState.visibleMonthShows,
                locale = locale,
                isPickingMonth = uiState.isPickingMonth,
                onTogglePicker = onToggleMonthPicker,
            )
        }

        item(key = "month-picker") {
            AnimatedVisibility(
                visible = uiState.isPickingMonth,
                enter = expandVertically(pickerSizeSpec) + fadeIn(pickerFadeSpec),
                exit = shrinkVertically(pickerSizeSpec) + fadeOut(pickerFadeSpec),
            ) {
                MonthPicker(
                    visibleMonth = uiState.visibleMonth,
                    monthsWithEntries = uiState.monthsWithEntries,
                    locale = locale,
                    onSelectMonth = onSelectMonth,
                    modifier = Modifier.padding(bottom = Spacing.sm),
                )
            }
        }

        item(key = "month-grid") {
            AnimatedContent(
                targetState = uiState.visibleMonth,
                transitionSpec = {
                    val forward = targetState > initialState
                    val offset = { width: Int -> if (forward) width / 5 else -width / 5 }
                    // Sin animación de tamaño: no todos los meses ocupan las mismas semanas, y
                    // el contenedor animando entre cinco y seis filas recorta la última.
                    (
                        (
                            slideInHorizontally(slideSpec, offset) + fadeIn(fadeSpec)
                            ) togetherWith (
                            slideOutHorizontally(slideSpec) { -offset(it) } + fadeOut(fadeSpec)
                            )
                        ) using null
                },
                label = "calendar-month",
                // Arrastrar de lado cambia de mes. Es el gesto que cualquiera prueba primero en
                // un calendario, y hasta ahora no hacía nada.
                modifier = Modifier.monthSwipe(
                    onPrevious = onPreviousMonth,
                    onNext = onNextMonth,
                ),
            ) { month ->
                MonthGrid(
                    month = month,
                    selectedDate = uiState.selectedDate,
                    entriesByDate = uiState.entriesByDate,
                    locale = locale,
                    onSelectDate = onSelectDate,
                )
            }
        }

        val entries = uiState.entriesForSelectedDate
        val selected = uiState.selectedDate
        if (selected != null) {
            item(key = "day-title") {
                Column(
                    modifier = Modifier.padding(
                        top = Spacing.lg,
                        bottom = Spacing.sm,
                        start = Spacing.xs,
                    ),
                ) {
                    // El día, escrito. "1 ese día" obligaba a mirar arriba para saber cuál era
                    // "ese"; puesto aquí, esta parte se sostiene sola y se lee como un diario.
                    Text(
                        text = selected
                            .format(
                                DateTimeFormatter.ofPattern(
                                    localized("EEEE, d 'de' MMMM", "EEEE, d MMMM"),
                                    locale,
                                )
                            )
                            .replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(locale) else it.toString()
                            },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (entries.isEmpty()) {
                            localized("Nada visto ese día", "Nothing watched that day")
                        } else if (entries.size == 1) {
                            localized("1 cosa vista", "1 thing watched")
                        } else {
                            localized(
                                "${entries.size} cosas vistas",
                                "${entries.size} things watched",
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(entries, key = { "${it.mediaType}-${it.id}" }) { entry ->
                WatchedEntryRow(entry = entry, onClick = { onEntryClick(entry) })
            }
        }
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    movies: Int,
    shows: Int,
    locale: Locale,
    isPickingMonth: Boolean,
    onTogglePicker: () -> Unit,
) {
    val name = month.month
        .getDisplayName(TextStyle.FULL_STANDALONE, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

    val pickerChevron by animateFloatAsState(
        targetValue = if (isPickingMonth) 180f else 0f,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "calendar-picker-chevron",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // El título abre el selector. Es donde se toca para cambiar de mes en cualquier
        // calendario, y aquí no hacía nada.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ShapeRadius.Large))
                .clickable(onClick = onTogglePicker)
                .padding(vertical = Spacing.xs, horizontal = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$name ${month.year}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = monthSummary(movies, shows),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // La flecha, en su sitio y con cuerpo. Pegada al final del texto quedaba flotando a
            // media frase y no parecía nada; al borde y dentro de un círculo tonal se lee como
            // el botón que es, y además cae donde la mano ya está.
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Symbol(
                        icon = R.drawable.keyboard_arrow_down_24px,
                        desc = if (isPickingMonth) {
                            localized("Cerrar el selector de mes", "Close the month picker")
                        } else {
                            localized("Elegir mes y año", "Pick month and year")
                        },
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.rotate(pickerChevron),
                    )
                }
            }
        }
    }
}

/** "2 películas · 3 series", y solo la mitad que tenga algo. */
@Composable
private fun monthSummary(movies: Int, shows: Int): String {
    if (movies == 0 && shows == 0) return localized("Sin actividad", "No activity")

    val moviePart = when (movies) {
        0 -> null
        1 -> localized("1 película", "1 movie")
        else -> localized("$movies películas", "$movies movies")
    }
    val showPart = when (shows) {
        0 -> null
        1 -> localized("1 serie", "1 show")
        else -> localized("$shows series", "$shows shows")
    }
    return listOfNotNull(moviePart, showPart).joinToString(" · ")
}

/** Cuánto hay que arrastrar para que cuente como cambio de mes. */
private const val SwipeThresholdPx = 60f

/**
 * Arrastrar de lado para cambiar de mes.
 *
 * El mes cambia en cuanto el dedo pasa del umbral, no al levantarlo. Esperando a soltar, el gesto
 * se sentía lento aunque la animación fuera la misma: se había hecho el movimiento y no pasaba
 * nada hasta terminar. Un cierre por gesto evita lo que motivaba aquella espera, que un arrastre
 * largo se comiera tres meses seguidos.
 */
private fun Modifier.monthSwipe(onPrevious: () -> Unit, onNext: () -> Unit): Modifier =
    this.pointerInput(onPrevious, onNext) {
        var dragged = 0f
        var fired = false
        detectHorizontalDragGestures(
            onDragStart = {
                dragged = 0f
                fired = false
            },
            onDragCancel = { fired = true },
        ) { change, amount ->
            dragged += amount
            if (!fired && kotlin.math.abs(dragged) > SwipeThresholdPx) {
                fired = true
                if (dragged > 0) onPrevious() else onNext()
            }
            change.consume()
        }
    }
