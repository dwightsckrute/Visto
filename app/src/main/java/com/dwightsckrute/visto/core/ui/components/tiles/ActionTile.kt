package com.dwightsckrute.visto.core.ui.components.tiles

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.components.pressScale

@Composable
fun ActionTile(
    headline: String,
    description: String? = null,
    leading: @Composable (() -> Unit)? = null,
    shapes: RoundedCornerShape,
    onClick: () -> Unit,
    colorDesc: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    danger: Boolean = false,
    itemBgColor: Color,
    selected: Boolean = false,
    trailing: @Composable (() -> Unit)? = null
) {
    val interactions = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactions),
        shape = if (selected) RoundedCornerShape(ShapeRadius.Large) else shapes,
    ) {
        ListItem(
            modifier = Modifier.clickable(
                interactionSource = interactions,
                indication = LocalIndication.current,
                onClick = onClick
            ),
            leadingContent = leading,
            colors = ListItemDefaults.colors(
                containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else if (danger) MaterialTheme.colorScheme.errorContainer else itemBgColor
            ),
            headlineContent = {
                Text(
                    headline,
                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else if (danger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            supportingContent = {
                if (description != null) {
                    Text(
                        description,
                        color = colorDesc,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            trailingContent = trailing
        )
    }
}
