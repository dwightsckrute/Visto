package com.pranshulgg.watchmaster.feature.calendar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.components.media.PosterBox
import com.pranshulgg.watchmaster.core.ui.components.media.PosterPlaceholder
import com.pranshulgg.watchmaster.core.ui.localization.localized
import com.pranshulgg.watchmaster.core.ui.theme.ShapeRadius
import com.pranshulgg.watchmaster.core.ui.theme.Spacing
import com.pranshulgg.watchmaster.feature.calendar.WatchedEntry

@Composable
fun WatchedEntryRow(
    entry: WatchedEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isTv = entry.mediaType == "tv"
    val poster = entry.posterPath?.let { "https://image.tmdb.org/t/p/w154$it" }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(ShapeRadius.Medium),
        color = MaterialTheme.colorScheme.surfaceBright,
    ) {
        Row(
            modifier = Modifier.padding(end = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterBox(
                posterUrl = poster,
                apiPath = entry.posterPath,
                width = 48.dp,
                height = 72.dp,
                cornerRadius = ShapeRadius.None,
                placeholder = { PosterPlaceholder(size = 0.6f) },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (isTv) localized("Serie", "TV show") else localized("Película", "Movie"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            entry.userRating?.let { rating ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Symbol(
                        icon = R.drawable.star_24px,
                        // El texto contiguo ya da la cifra; describir el icono lo duplicaría.
                        desc = null,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 16.dp,
                    )
                    Text(
                        text = "%.1f".format(rating),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
