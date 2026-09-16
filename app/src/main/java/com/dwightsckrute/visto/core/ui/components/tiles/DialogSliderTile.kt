package com.dwightsckrute.visto.core.ui.components.tiles

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.dwightsckrute.visto.core.ui.components.ActionBottomSheet
import com.dwightsckrute.visto.core.ui.components.pressScale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogSliderTile(
    headline: String,
    description: String? = null,
    initialValue: Float = 0.5f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueSubmitted: (Float) -> Unit,
    leading: @Composable (() -> Unit)? = null,
    shapes: RoundedCornerShape,
    labelFormatter: (Float) -> String = { it.toString() },
    dialogTitle: String,
    isDescriptionAsValue: Boolean = false,
    itemBgColor: Color,
) {
    var showSheet by remember { mutableStateOf(false) }
    var sliderValue by remember(initialValue) { mutableFloatStateOf(initialValue) }
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
            colors = ListItemDefaults.colors(containerColor = itemBgColor),
            leadingContent = leading,
            headlineContent = { Text(headline) },
            supportingContent = {
                if (description != null) {
                    Text(
                        text = description,
                        color = if (isDescriptionAsValue) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                } else {
                    Text(labelFormatter(sliderValue), color = MaterialTheme.colorScheme.tertiary)
                }
            },
        )
    }

    if (showSheet) {
        val sheetState = rememberModalBottomSheetState()
        ActionBottomSheet(
            sheetState = sheetState,
            onCancel = { showSheet = false },
            onConfirm = { onValueSubmitted(sliderValue) },
        ) { _ ->
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = labelFormatter(sliderValue),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(8.dp))
                Box(Modifier.padding(horizontal = 24.dp)) {
                    LabeledSlider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = valueRange,
                        steps = steps,
                        labelFormatter = labelFormatter,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LabeledSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    labelFormatter: (Float) -> String = { it.toString() },
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        var sliderWidth by remember { mutableIntStateOf(0) }
        var showLabel by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { sliderWidth = it.width },
        ) {
            Slider(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    showLabel = true
                },
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier.fillMaxWidth(),
            )

            val range = valueRange.endInclusive - valueRange.start
            val fraction = if (range == 0f) 0f else (value - valueRange.start) / range
            val thumbOffsetPx = (fraction * sliderWidth).coerceIn(0f, sliderWidth.toFloat())
            val scale by animateFloatAsState(
                targetValue = if (showLabel) 1f else 0.8f,
                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                label = "slider label scale",
            )
            val alpha by animateFloatAsState(
                targetValue = if (showLabel) 1f else 0f,
                label = "slider label alpha",
            )

            LaunchedEffect(value) {
                showLabel = true
                delay(1_000)
                showLabel = false
            }

            Popup(offset = IntOffset((thumbOffsetPx - 48).toInt(), -90)) {
                Surface(
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shape = RoundedCornerShape(50.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale.coerceIn(0f, 1f)
                        scaleY = scale.coerceIn(0f, 1f)
                        this.alpha = alpha.coerceIn(0f, 1f)
                    },
                ) {
                    Text(
                        text = labelFormatter(value),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            }
        }
    }
}
