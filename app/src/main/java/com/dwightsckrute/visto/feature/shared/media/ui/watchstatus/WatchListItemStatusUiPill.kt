package com.dwightsckrute.visto.feature.shared.media.ui.watchstatus

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.theme.LocalStatusColors
import com.dwightsckrute.visto.core.utils.formatDate
import com.dwightsckrute.visto.data.local.entity.SeasonEntity
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import java.time.Instant
import com.dwightsckrute.visto.core.ui.localization.AppLanguage


data class WatchListItemStatusUiPill(
    val statusLabel: String,
    val containerColor: Color,
    val contentColor: Color
)

interface WatchStatusDates {
    val startedDate: Instant?
    val finishedDate: Instant?
    val interruptedDate: Instant?
    val addedDate: Instant
}

fun SeasonEntity.asStatusDates() = object : WatchStatusDates {
    override val startedDate = seasonStartedDate
    override val finishedDate = seasonFinishedDate
    override val interruptedDate = seasonInterruptedAt
    override val addedDate = seasonAddedDate
}

fun WatchlistItemEntity.asStatusDates(): WatchStatusDates {
    val item = this
    return object : WatchStatusDates {
        override val startedDate: Instant? = item.startedDate
        override val finishedDate: Instant? = item.finishedDate
        override val interruptedDate: Instant? = item.interruptedAt
        override val addedDate: Instant = item.addedDate
    }
}


@Composable
fun WatchStatus.toWatchListItemStatusUiPill(item: WatchStatusDates): WatchListItemStatusUiPill {
    val statusColor = LocalStatusColors.current

    return when (this) {
        WatchStatus.WATCHING -> {
            WatchListItemStatusUiPill(
                statusLabel = AppLanguage.text("Empezada • ${item.startedDate?.formatDate()}", "Started • ${item.startedDate?.formatDate()}"),
                containerColor = statusColor.warning.bg,
                contentColor = statusColor.warning.on
            )
        }

        WatchStatus.FINISHED -> {
            WatchListItemStatusUiPill(
                statusLabel = AppLanguage.text("Terminada • ${item.finishedDate?.formatDate()}", "Finished • ${item.finishedDate?.formatDate()}"),
                containerColor = statusColor.success.bg,
                contentColor = statusColor.success.on
            )
        }

        WatchStatus.INTERRUPTED -> {
            WatchListItemStatusUiPill(
                statusLabel = AppLanguage.text("Interrumpida • ${item.interruptedDate?.formatDate()}", "Paused • ${item.interruptedDate?.formatDate()}"),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        else -> {
            WatchListItemStatusUiPill(
                statusLabel = AppLanguage.text("Añadida • ${item.addedDate.formatDate()}", "Added • ${item.addedDate.formatDate()}"),
                containerColor = statusColor.pending.bg,
                contentColor = statusColor.pending.on
            )
        }
    }
}
