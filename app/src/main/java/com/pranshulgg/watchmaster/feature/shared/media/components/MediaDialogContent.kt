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
        title = "Añadir una nota",
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
                placeholder = { Text("Nota…") }
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
        title = if (isUpdateRating) "Actualizar valoración" else if (isTv) "Valorar esta temporada" else "Valorar esta película",
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

    val text = if (isTv) "temporada" else "película"

    val headline = if (status != null) "Estado de visualización" else "Eliminar $text"
    val message = status?.dialogMessage(isTv)
        ?: "¿Seguro que quieres eliminar esta $text? Esta acción no se puede deshacer."

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
