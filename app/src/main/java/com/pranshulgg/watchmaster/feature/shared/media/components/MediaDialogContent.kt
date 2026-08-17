package com.pranshulgg.watchmaster.feature.shared.media.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pranshulgg.watchmaster.core.model.WatchStatus
import com.pranshulgg.watchmaster.core.ui.components.DialogBasic
import com.pranshulgg.watchmaster.core.ui.components.TextAlertDialog
import com.pranshulgg.watchmaster.core.ui.components.media.RateMediaDialogContent
import com.pranshulgg.watchmaster.core.ui.theme.ShapeRadius
import com.pranshulgg.watchmaster.feature.shared.media.ui.watchstatus.dialogMessage
import com.pranshulgg.watchmaster.core.ui.localization.localized

@Composable
fun MediaNoteDialogContent(
    show: Boolean,
    note: String,
    initialNote: String,
    onNoteChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    DialogBasic(
        show = show,
        title = localized("Añadir una nota", "Add a note"),
        showDefaultActions = true,
        onDismiss = {
            onDismiss()
            onNoteChange(initialNote)
        },
        onConfirm = {
            onConfirm(note)
        },
        confirmText = "Guardar",
        content = {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(ShapeRadius.Large),
                value = note,
                onValueChange = onNoteChange,
                placeholder = { Text(localized("Nota…", "Note…")) }
            )
        }
    )
}

@Composable
fun MediaRatingDialogContent(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    isUpdateRating: Boolean = false,
    originalRating: Float = 0f,
    isTv: Boolean = false,
) {
    DialogBasic(
        show = show,
        title = if (isUpdateRating) localized("Actualizar valoración", "Update rating") else if (isTv) "Valorar esta temporada" else "Valorar esta película",
        showDefaultActions = false,
        onDismiss = {
            onDismiss()
        },
        content = {
            RateMediaDialogContent(
                updateRating = isUpdateRating,
                originalRating = originalRating,
                onCancel = {
                    onDismiss()
                },
                onConfirm = { rating ->
                    onConfirm(rating)
                }
            )
        }
    )
}


@Composable
fun MediaConfirmationDialogContent(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isTv: Boolean = false,
    status: WatchStatus? = null,
    customHeadline: String? = null,
    customMessage: String? = null
) {

    val text = if (isTv) {
        localized("temporada", "season")
    } else {
        localized("película", "movie")
    }

    val headline = if (status != null) {
        localized("Estado de visualización", "Watch status")
    } else {
        localized("Eliminar $text", "Delete $text")
    }
    val message = status?.dialogMessage(isTv)
        ?: localized(
            "¿Seguro que quieres eliminar esta $text? Esta acción no se puede deshacer.",
            "Delete this $text? This cannot be undone.",
        )

    TextAlertDialog(
        show = show,
        title = customHeadline ?: headline,
        message = customMessage ?: message,
        confirmText = "Confirmar",
        onConfirm = {
            onConfirm()
        },
        onDismiss = {
            onDismiss()
        }
    )
}
