package com.pranshulgg.watchmaster.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.pranshulgg.watchmaster.core.ui.localization.localized

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActionBottomSheet(
    sheetState: SheetState,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String = localized("Guardar", "Save"),
    cancelText: String = localized("Cancelar", "Cancel"),
    showActions: Boolean = true,
    confirmBtnMaxWidth: Boolean = false,
    isConfirmDisabled: Boolean = false,
    enableHandle: Boolean = true,
    hideConfirmBtn: Boolean = false,
    removeBottomInset: Boolean = false,
    content: @Composable (hide: () -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()

    fun hide() {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onCancel()
            }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onCancel,
        contentWindowInsets = {
            if (removeBottomInset) BottomSheetDefaults.modalWindowInsets
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top) else BottomSheetDefaults.modalWindowInsets
        },
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
        dragHandle = {
            if (enableHandle) {
                Surface(
                    Modifier
                        .padding(top = 22.dp, bottom = 12.dp)
                        .height(4.dp)
                        .width(32.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape
                ) {}
            } else {
                null
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 800.dp)
        ) {

            content { hide() }

            if (showActions) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp, start = 16.dp, bottom = 10.dp),
                    horizontalArrangement = if (hideConfirmBtn) Arrangement.End
                    else Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = if (hideConfirmBtn) Modifier.defaultMinSize(minWidth = 112.dp, minHeight = 48.dp)
                        else Modifier.weight(1f).heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        onClick = {
                            hide()
                        },
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(
                            cancelText,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 16.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (!hideConfirmBtn) {
                        Button(
                            onClick = {
                                onConfirm()
                                hide()
                            },
                            enabled = !isConfirmDisabled,
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier
                                .weight(if (confirmBtnMaxWidth) 1.6f else 1f)
                                .heightIn(min = 48.dp),
                        ) {
                            Text(
                                confirmText,
                                fontSize = 16.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

        }
    }
}
