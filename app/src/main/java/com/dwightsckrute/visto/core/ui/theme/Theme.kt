package com.dwightsckrute.visto.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme


val LocalStatusColors = staticCompositionLocalOf {
    MediaStatusColors(
        success = MediaStatusColor(Color(0xFF16520E), Color(0xFFB1F49D)),
        pending = MediaStatusColor(Color(0xFFD4D4D4), Color(0xFF000000)),
        warning = MediaStatusColor(Color(0xFF5D4200), Color(0xFFFFDEA4)),
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VistoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    seedColor: Color = Color.Green,
    themeVariantType: ThemeVariantType,
    dynamicColor: Boolean = false,
    amoledBlack: Boolean = false,
    applySystemUi: Boolean = true,
    content: @Composable () -> Unit
) {
    val generatedColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> {
            rememberDynamicColorScheme(
                seedColor = seedColor,
                isDark = darkTheme,
                specVersion = ColorSpec.SpecVersion.SPEC_2025,
                style = themeVariantType.paletteStyle,
            )
        }
    }

    val colorScheme = if (darkTheme && amoledBlack) {
        generatedColorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceDim = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerHigh = Color(0xFF080808),
            surfaceContainerHighest = Color(0xFF101010),
            surfaceBright = Color(0xFF151515),
        )
    } else {
        generatedColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        if (applySystemUi) {
            SideEffect {
                val window = (view.context as Activity).window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    !darkTheme
            }
        }
    }

    val statusColors = MediaStatusColors(
        success = MediaStatusColor(
            bg = colorScheme.tertiaryContainer,
            on = colorScheme.onTertiaryContainer,
        ),
        pending = MediaStatusColor(
            bg = colorScheme.surfaceContainerHighest,
            on = colorScheme.onSurface,
        ),
        warning = MediaStatusColor(
            bg = colorScheme.secondaryContainer,
            on = colorScheme.onSecondaryContainer,
        ),
    )


    CompositionLocalProvider(
        LocalStatusColors provides statusColors
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            shapes = AppShapes,
            typography = AppTypography,
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }

}
