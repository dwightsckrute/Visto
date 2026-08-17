package com.pranshulgg.watchmaster.core.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.pranshulgg.watchmaster.core.ui.theme.AppMotion

object NavTransitions {

    fun enter(): EnterTransition =
        slideInHorizontally(
            initialOffsetX = { 1 * it }
        ) + fadeIn(tween(AppMotion.DurationMedium))

    fun exit(): ExitTransition =
        slideOutHorizontally(
            targetOffsetX = { 1 * -it / 4 }
        ) + fadeOut(tween(AppMotion.DurationShort))

    fun popEnter(): EnterTransition =
        slideInHorizontally(initialOffsetX = { 1 * -it / 4 }) + fadeIn(tween(AppMotion.DurationMedium))

    fun popExit(): ExitTransition =
        slideOutHorizontally(targetOffsetX = { 1 * it }) + fadeOut(tween(AppMotion.DurationShort))
}
