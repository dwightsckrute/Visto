package com.pranshulgg.watchmaster.feature.shared.media.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.ui.components.media.MediaSectionCard
import com.pranshulgg.watchmaster.core.ui.theme.ShapeRadius
import com.pranshulgg.watchmaster.core.ui.localization.localized

@Composable
fun NotesSection(isSeasonNoteNull: Boolean = true, action: () -> Unit, noteText: String) {
    MediaSectionCard(
        title = localized("Notas", "Notes"),
        titleIcon = R.drawable.sticky_note_2_24px,
        showAction = true,
        actionOnClick = {
            action()
        },
        actionText = localized("Editar nota", "Edit note")
    ) {
        if (!isSeasonNoteNull) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(ShapeRadius.Medium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                ) {
                    Text(
                        noteText,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}