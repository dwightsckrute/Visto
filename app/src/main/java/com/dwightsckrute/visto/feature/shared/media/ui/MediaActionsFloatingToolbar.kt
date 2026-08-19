package com.dwightsckrute.visto.feature.shared.media.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.components.Symbol
import com.dwightsckrute.visto.core.ui.theme.floatingBarLabel
import com.dwightsckrute.visto.core.ui.components.TextAlertDialog
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.components.media.DatePickerSheet
import com.dwightsckrute.visto.core.ui.snackbar.SnackbarManager
import com.dwightsckrute.visto.core.ui.theme.Elevation
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.feature.shared.media.ui.watchstatus.buttonIcon
import com.dwightsckrute.visto.feature.shared.media.ui.watchstatus.actionLabel
import com.dwightsckrute.visto.feature.shared.media.ui.watchstatus.confirmAction
import com.dwightsckrute.visto.feature.shared.media.ui.watchstatus.dialogMessage
import java.time.Instant


private data class MenuItemOptionList(
    val title: String,
    val leading: Int,
    val action: () -> Unit,
    val isInterruptOption: Boolean = false,
    val isMarkWatchedOption: Boolean = false,
)

private data class UiState(
    val showDialog: Boolean = false,
    val expanded: Boolean = false,
    val showWatchedDatePicker: Boolean = false,
)


@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun MediaActionsFloatingToolbar(
    scrollBehavior: FloatingToolbarScrollBehavior,
    itemStatus: WatchStatus,
    actions: FloatingToolbarMediaActionsParams,
    isPinned: Boolean = false,
    isTv: Boolean = false,
) {

    val systemInsets = WindowInsets.systemBars.asPaddingValues()
    val menuItemContentColor = MaterialTheme.colorScheme.onTertiaryContainer
    val menuItemContentTextStyle = MaterialTheme.typography.labelLarge
    val pinConfirmation = if (!isPinned) localized("Contenido fijado", "Content pinned")
    else localized("Contenido desfijado", "Content unpinned")
    var uiState by remember { mutableStateOf(UiState()) }

    val menuItemOptionList = listOf(
        MenuItemOptionList(
            localized("Marcar como vista…", "Mark as watched…"),
            R.drawable.check_24px,
            { uiState = uiState.copy(showWatchedDatePicker = true) },
            isMarkWatchedOption = true,
        ),
        MenuItemOptionList(
            localized("Interrumpir", "Pause"),
            R.drawable.pause_24px,
            { actions.interruptWatching() }, isInterruptOption = true
        ),
        MenuItemOptionList(
            if (isPinned) localized("Desfijar", "Unpin") else localized("Fijar", "Pin"),
            R.drawable.keep_24px,
            {
                actions.togglePin()
                SnackbarManager.show(pinConfirmation)
            }),
        MenuItemOptionList(
            localized("Compartir", "Share"),
            R.drawable.share_24px,
            actions.share,
        ),
        MenuItemOptionList(
            localized("Eliminar", "Delete"),
            R.drawable.delete_24px,
            { actions.delete() })
    )

    Box(
        Modifier
            .fillMaxWidth(),
    ) {
        HorizontalFloatingToolbar(
            expandedShadowElevation = Elevation.floating,
            scrollBehavior = scrollBehavior,
            modifier = Modifier
                .padding(
                    top = ScreenOffset,
                    bottom = systemInsets.calculateBottomPadding()
                            + ScreenOffset
                )
                .align(Alignment.BottomCenter)
                .zIndex(1f),
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
            expanded = true,
            content = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    StatusMainActionBtn(
                        onClick = {
                            if (itemStatus == WatchStatus.WATCHING) {
                                actions.finishWatching()
                            } else {
                                uiState = uiState.copy(showDialog = true)
                            }
                        },
                        itemStatus = itemStatus
                    )
                    Box {
                        FilledIconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = { uiState = uiState.copy(expanded = true) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                        ) {
                            Symbol(
                                R.drawable.more_vert_24px,
                                color = MaterialTheme.colorScheme.primaryContainer
                            )
                        }

                        DropdownMenu(
                            expanded = uiState.expanded,
                            onDismissRequest = { uiState = uiState.copy(expanded = false) },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(ShapeRadius.Large)
                        ) {
                            menuItemOptionList.forEach { option ->

                                if (option.isInterruptOption && itemStatus != WatchStatus.WATCHING) {
                                    return@forEach
                                }

                                // Ya está vista: volver a marcarla no aporta nada, y la fecha
                                // se corrige desde el propio listado o el Diario.
                                if (option.isMarkWatchedOption && itemStatus == WatchStatus.FINISHED) {
                                    return@forEach
                                }

                                DropdownMenuItem(
                                    modifier = Modifier.widthIn(min = if (option.isInterruptOption) 130.dp else 112.dp),
                                    leadingIcon = {
                                        Symbol(option.leading, color = menuItemContentColor)
                                    },
                                    text = {
                                        Text(
                                            option.title,
                                            color = menuItemContentColor,
                                            style = menuItemContentTextStyle
                                        )
                                    },
                                    onClick = {
                                        uiState = uiState.copy(expanded = false)
                                        option.action()
                                    }
                                )
                            }
                        }
                    }

                }
            },
        )
    }


    TextAlertDialog(
        show = uiState.showDialog,
        title = localized("Estado de visualización", "Watch status"),
        message = itemStatus.dialogMessage(isTv = isTv),
        confirmText = localized("Confirmar", "Confirm"),
        onConfirm = {
            itemStatus.confirmAction(
                start = { actions.startWatching() },
                reset = { actions.resetWatching() },
                finish = { actions.finishWatching() }
            )
        },
        onDismiss = {
            uiState = uiState.copy(showDialog = false)
        }
    )

    // Se fija al abrir para que recomponer no reinicie el día elegido.
    val todayMillis = remember { System.currentTimeMillis() }
    DatePickerSheet(
        show = uiState.showWatchedDatePicker,
        initialDate = todayMillis,
        // DatePickerSheet exige un id para poder reutilizarse desde los listados; aquí solo
        // interesa la fecha devuelta, así que se ignora.
        id = MARK_WATCHED_SHEET_ID,
        onDateSelected = { _, millis ->
            millis?.let { actions.markWatchedOn(Instant.ofEpochMilli(it)) }
            uiState = uiState.copy(showWatchedDatePicker = false, expanded = false)
        },
        onDismiss = { uiState = uiState.copy(showWatchedDatePicker = false) },
    )
}

private const val MARK_WATCHED_SHEET_ID = 0L

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatusMainActionBtn(onClick: () -> Unit, itemStatus: WatchStatus) {
    Button(
        modifier = Modifier
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = {
            onClick()
        },
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
    ) {
        Symbol(
            itemStatus.buttonIcon,
            color = MaterialTheme.colorScheme.onSurface,
            // El mismo icono de 20dp que lleva la barra de navegación: con los 24 por defecto,
            // la acción pesaba más que el destino activo estando las dos en el mismo borde.
            size = 20.dp,
        )
        Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(48.dp)))
        Text(
            itemStatus.actionLabel,
            style = floatingBarLabel,
        )
    }
}
