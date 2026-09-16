package com.dwightsckrute.visto.core.ui.components.tiles

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dwightsckrute.visto.core.ui.components.ActionBottomSheet
import com.dwightsckrute.visto.core.ui.components.pressScale

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DialogTextFieldTile(
    headline: String,
    description: String? = null,
    initialText: String = "",
    onTextSubmitted: (String) -> Unit,
    leading: @Composable (() -> Unit)? = null,
    placeholder: String,
    placeholderTextField: String,
    shapes: RoundedCornerShape,
    itemBgColor: Color
) {
    var showSheet by remember { mutableStateOf(false) }
    var textFieldValue by remember(initialText) { mutableStateOf(initialText) }
    val interactions = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactions),
        shape = shapes,
    ) {
        ListItem(
            modifier = Modifier.clickable(
                interactionSource = interactions,
                indication = LocalIndication.current,
            ) { showSheet = true },
            colors = ListItemDefaults.colors(
                containerColor = itemBgColor
            ),
            leadingContent = leading,
            headlineContent = { Text(headline) },
            supportingContent = {
                if (description != null) Text(description)
                else if (textFieldValue.isNotEmpty()) Text(
                    textFieldValue,
                    color = MaterialTheme.colorScheme.tertiary
                ) else {
                    Text(
                        placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
        )
    }

    if (showSheet) {
        val sheetState = rememberModalBottomSheetState()
        ActionBottomSheet(
            sheetState = sheetState,
            onCancel = { showSheet = false },
            onConfirm = { onTextSubmitted(textFieldValue) },
        ) { _ ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    placeholder = { Text(placeholderTextField) },
                    singleLine = true,
                )
            }
        }
    }
}
