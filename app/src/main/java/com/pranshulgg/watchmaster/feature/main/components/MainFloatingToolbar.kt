package com.pranshulgg.watchmaster.feature.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.pranshulgg.watchmaster.R
import com.pranshulgg.watchmaster.core.ui.components.Tooltip
import com.pranshulgg.watchmaster.core.ui.navigation.NavRoutes
import com.pranshulgg.watchmaster.feature.search.SearchType
import com.pranshulgg.watchmaster.core.ui.components.Symbol
import com.pranshulgg.watchmaster.core.ui.localization.localized

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainFloatingToolbar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    navController: NavController,
) {
    val labelList = listOf(
        localized("Inicio", "Home"),
        localized("Películas", "Movies"),
        localized("Series", "TV shows"),
    )
    val unSelectedIcons = listOf(
        R.drawable.home_24px,
        R.drawable.movie_24px,
        R.drawable.tv_24px,
    )
    val selectedIcons = listOf(
        R.drawable.home_filled_24px,
        R.drawable.movie_filled_24px,
        R.drawable.tv_filled_24px,
    )

    val colorScheme = MaterialTheme.colorScheme

    val systemInsets = WindowInsets.systemBars.asPaddingValues()

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = ScreenOffset,
                    bottom = systemInsets.calculateBottomPadding() + ScreenOffset,
                )
                .align(Alignment.BottomCenter)
                .zIndex(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalFloatingToolbar(
                expandedShadowElevation = 1.dp,
                colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                expanded = true,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    labelList.forEachIndexed { index, label ->
                        Tooltip(
                            label,
                            preferredPosition = TooltipAnchorPosition.Above,
                            spacing = 10.dp
                        ) {
                            ToggleButton(
                                modifier = Modifier
                                    .animateContentSize(
                                        animationSpec = motionScheme.defaultSpatialSpec(),
                                    )
                                    .height(48.dp),
                                checked = selectedItem == index,
                                onCheckedChange = { onItemSelected(index) },
                                shapes = ToggleButtonDefaults.shapes(),
                                colors = ToggleButtonDefaults.toggleButtonColors(
                                    containerColor = Color.Transparent,
                                    checkedContainerColor = colorScheme.surfaceContainer
                                ),

                                ) {
                                Crossfade(
                                    targetState = selectedItem == index,
                                    animationSpec = motionScheme.fastEffectsSpec(),
                                    label = "main-navigation-icon",
                                ) {
                                    if (it) Symbol(
                                        selectedIcons[index],
                                        color = colorScheme.onSurface
                                    ) else Symbol(
                                        unSelectedIcons[index],
                                        color = colorScheme.onPrimaryContainer
                                    )
                                }
                                AnimatedVisibility(
                                    visible = selectedItem == index,
                                    enter = expandHorizontally(motionScheme.defaultSpatialSpec()),
                                    exit = shrinkHorizontally(motionScheme.defaultSpatialSpec())
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip,
                                        color = if (selectedItem == index) colorScheme.onSurface else colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Tooltip(
                localized("Buscar películas y series", "Search movies and TV shows"),
                preferredPosition = TooltipAnchorPosition.Above,
                spacing = 10.dp,
            ) {
                FloatingActionButton(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    containerColor = colorScheme.primaryContainer,
                    contentColor = colorScheme.onPrimaryContainer,
                    onClick = {
                        navController.navigate(NavRoutes.search(SearchType.MULTI))
                    },
                ) {
                    Symbol(
                        icon = R.drawable.search_24px,
                        color = colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}
