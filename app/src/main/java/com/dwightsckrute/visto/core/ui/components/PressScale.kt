package com.dwightsckrute.visto.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Gives setting rows the same short, physical press feedback as Expressive buttons. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.pressScale(interactionSource: InteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) PRESSED_SCALE else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "setting row press",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

private const val PRESSED_SCALE = 0.98f
