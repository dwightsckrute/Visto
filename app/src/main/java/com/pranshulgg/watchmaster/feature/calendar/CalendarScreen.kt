package com.pranshulgg.watchmaster.feature.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.ui.components.EmptyContainerPlaceholder
import com.pranshulgg.watchmaster.core.ui.components.LargeTopBarScaffold
import com.pranshulgg.watchmaster.core.ui.components.NavigateUpBtn
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.components.Tooltip
import com.pranshulgg.watchmaster.core.ui.localization.AppLanguage
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.navigation.NavRoutes
import com.pranshulgg.watchmaster.core.ui.theme.Spacing
import com.pranshulgg.watchmaster.feature.calendar.components.MonthGrid
import com.pranshulgg.watchmaster.feature.calendar.components.WatchedEntryRow
import java.time.YearMonth
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
    onSelectDate: (java.time.LocalDate) -> Unit,
    onEntryClick: (WatchedEntry) -> Unit,
) {
    // transitionSpec no es ámbito @Composable: las specs se resuelven fuera.
    val slideSpec = motionScheme.defaultSpatialSpec<IntOffset>()
    val fadeSpec = motionScheme.fastEffectsSpec<Float>()

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
                count = uiState.visibleMonthCount,
                locale = locale,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
            )
        }

        item(key = "month-grid") {
            AnimatedContent(
                targetState = uiState.visibleMonth,
                transitionSpec = {
                    val forward = targetState > initialState
                    val offset = { width: Int -> if (forward) width / 5 else -width / 5 }
                    (
                        slideInHorizontally(slideSpec, offset) + fadeIn(fadeSpec)
                        ) togetherWith (
                        slideOutHorizontally(slideSpec) { -offset(it) } + fadeOut(fadeSpec)
                        )
                },
                label = "calendar-month",
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
        if (uiState.selectedDate != null) {
            item(key = "day-title") {
                Text(
                    text = if (entries.isEmpty()) {
                        localized("Nada visto ese día", "Nothing watched that day")
                    } else {
                        localized(
                            "${entries.size} ese día",
                            "${entries.size} that day",
                        )
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        top = Spacing.md,
                        bottom = Spacing.xs,
                        start = Spacing.xs,
                    ),
                )
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
    count: Int,
    locale: Locale,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val name = month.month
        .getDisplayName(TextStyle.FULL_STANDALONE, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$name ${month.year}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (count == 0) {
                    localized("Sin actividad", "No activity")
                } else {
                    localized("$count este mes", "$count this month")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onPrevious) {
            Symbol(
                icon = R.drawable.keyboard_arrow_down_24px,
                desc = localized("Mes anterior", "Previous month"),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.rotate(90f),
            )
        }
        IconButton(onClick = onNext) {
            Symbol(
                icon = R.drawable.keyboard_arrow_down_24px,
                desc = localized("Mes siguiente", "Next month"),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.rotate(-90f),
            )
        }
    }
}
