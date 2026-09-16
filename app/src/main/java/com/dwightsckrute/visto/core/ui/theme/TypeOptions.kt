package com.dwightsckrute.visto.core.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified

/** An app-level multiplier applied on top of Android's accessibility font scale. */
enum class AppTextScale(val factor: Float) {
    COMPACT(0.9f),
    NORMAL(1f),
    LARGE(1.15f),
    HUGE(1.3f),
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun appTypography(
    base: Typography = AppTypography,
    scale: AppTextScale = AppTextScale.NORMAL,
): Typography {
    if (scale == AppTextScale.NORMAL) return base

    fun TextStyle.scaled(): TextStyle = copy(
        fontSize = fontSize.scaleBy(scale.factor),
        lineHeight = lineHeight.scaleBy(scale.factor),
    )

    return base.copy(
        displayLarge = base.displayLarge.scaled(),
        displayMedium = base.displayMedium.scaled(),
        displaySmall = base.displaySmall.scaled(),
        headlineLarge = base.headlineLarge.scaled(),
        headlineMedium = base.headlineMedium.scaled(),
        headlineSmall = base.headlineSmall.scaled(),
        titleLarge = base.titleLarge.scaled(),
        titleMedium = base.titleMedium.scaled(),
        titleSmall = base.titleSmall.scaled(),
        bodyLarge = base.bodyLarge.scaled(),
        bodyMedium = base.bodyMedium.scaled(),
        bodySmall = base.bodySmall.scaled(),
        labelLarge = base.labelLarge.scaled(),
        labelMedium = base.labelMedium.scaled(),
        labelSmall = base.labelSmall.scaled(),
        displayLargeEmphasized = base.displayLargeEmphasized.scaled(),
        displayMediumEmphasized = base.displayMediumEmphasized.scaled(),
        displaySmallEmphasized = base.displaySmallEmphasized.scaled(),
        headlineLargeEmphasized = base.headlineLargeEmphasized.scaled(),
        headlineMediumEmphasized = base.headlineMediumEmphasized.scaled(),
        headlineSmallEmphasized = base.headlineSmallEmphasized.scaled(),
        titleLargeEmphasized = base.titleLargeEmphasized.scaled(),
        titleMediumEmphasized = base.titleMediumEmphasized.scaled(),
        titleSmallEmphasized = base.titleSmallEmphasized.scaled(),
        bodyLargeEmphasized = base.bodyLargeEmphasized.scaled(),
        bodyMediumEmphasized = base.bodyMediumEmphasized.scaled(),
        bodySmallEmphasized = base.bodySmallEmphasized.scaled(),
        labelLargeEmphasized = base.labelLargeEmphasized.scaled(),
        labelMediumEmphasized = base.labelMediumEmphasized.scaled(),
        labelSmallEmphasized = base.labelSmallEmphasized.scaled(),
    )
}

private fun TextUnit.scaleBy(factor: Float): TextUnit = if (isSpecified) this * factor else this
