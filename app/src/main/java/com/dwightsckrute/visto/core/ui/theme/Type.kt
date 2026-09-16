package com.dwightsckrute.visto.core.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.sp
import com.dwightsckrute.visto.R


/** Figtree, distributed under SIL Open Font License 1.1. */
@OptIn(ExperimentalTextApi::class)
val BrandRegular = FontFamily(
    Font(
        resId = R.font.figtree,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("wght", 400f),
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val BrandMedium = FontFamily(
    Font(
        resId = R.font.figtree,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("wght", 500f),
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val BrandBold = FontFamily(
    Font(
        R.font.figtree,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("wght", 900f),
        )
    )
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val AppTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(
            fontFamily = BrandRegular,
            fontSize = 57.sp,
            lineHeight = 64.sp
        ),
        displayMedium = displayMedium.copy(
            fontFamily = BrandRegular,
            fontSize = 45.sp,
            lineHeight = 52.sp
        ),
        displaySmall = displaySmall.copy(
            fontFamily = BrandRegular,
            fontSize = 36.sp,
            lineHeight = 44.sp
        ),
        headlineLarge = headlineLarge.copy(
            fontFamily = BrandRegular,
            fontSize = 32.sp,
            lineHeight = 40.sp
        ),
        headlineMedium = headlineMedium.copy(
            fontFamily = BrandRegular,
            fontSize = 28.sp,
            lineHeight = 36.sp
        ),
        headlineSmall = headlineSmall.copy(
            fontFamily = BrandRegular,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        titleLarge = titleLarge.copy(
            fontFamily = BrandRegular,
            fontSize = 22.sp,
            lineHeight = 28.sp
        ),
        titleMedium = titleMedium.copy(
            fontFamily = BrandMedium,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        titleSmall = titleSmall.copy(
            fontFamily = BrandMedium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodyLarge = bodyLarge.copy(
            fontFamily = BrandRegular,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        bodyMedium = bodyMedium.copy(
            fontFamily = BrandRegular,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodySmall = bodySmall.copy(
            fontFamily = BrandRegular,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelLarge = labelLarge.copy(
            fontFamily = BrandMedium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelMedium = labelMedium.copy(
            fontFamily = BrandMedium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelSmall = labelSmall.copy(
            fontFamily = BrandMedium,
            fontSize = 11.sp,
            lineHeight = 16.sp
        ),
    )
}
