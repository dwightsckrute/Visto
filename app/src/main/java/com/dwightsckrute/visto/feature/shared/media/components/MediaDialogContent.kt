package com.dwightsckrute.visto.feature.shared.media.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.components.DialogBasic
import com.dwightsckrute.visto.core.ui.components.TextAlertDialog
import com.dwightsckrute.visto.core.ui.components.media.RateMediaDialogContent
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.feature.shared.media.ui.watchstatus.dialogMessage
import com.dwightsckrute.visto.core.ui.localization.localized

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
        confirmText = localized("Guardar", "Save"),
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
    isBook: Boolean = false,
) {
    DialogBasic(
        show = show,
        title = when {
            isUpdateRating -> localized("Actualizar valoración", "Update rating")
            isBook -> localized("Valorar este libro", "Rate this book")
            isTv -> localized("Valorar esta temporada", "Rate this season")
            else -> localized("Valorar esta película", "Rate this film")
        },
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
    isBook: Boolean = false,
    status: WatchStatus? = null,
    customHeadline: String? = null,
    customMessage: String? = null
) {

    // Cómo se llama esto que se va a borrar. Antes solo había dos respuestas posibles, así que
    // un libro se anunciaba como película: al copiar el flujo se copió también su vocabulario.
    val text = when {
        isBook -> localized("libro", "book")
        isTv -> localized("temporada", "season")
        else -> localized("película", "film")
    }

    val headline = if (status != null) {
        localized("Estado de visualización", "Watch status")
    } else {
        localized("Eliminar $text", "Delete $text")
    }
    val message = status?.dialogMessage(isTv = isTv, isBook = isBook)
        ?: localized(
            "¿Seguro que quieres eliminar esta $text? Esta acción no se puede deshacer.",
            "Delete this $text? This cannot be undone.",
        )

    TextAlertDialog(
        show = show,
        title = customHeadline ?: headline,
        message = customMessage ?: message,
        confirmText = localized("Confirmar", "Confirm"),
        onConfirm = {
            onConfirm()
        },
        onDismiss = {
            onDismiss()
        }
    )
}
