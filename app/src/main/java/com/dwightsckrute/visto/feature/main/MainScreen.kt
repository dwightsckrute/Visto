package com.dwightsckrute.visto.feature.main

import com.dwightsckrute.visto.feature.home.components.homeGreeting
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection.Companion.Bottom
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dwightsckrute.visto.feature.books.BooksHomeScreen
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.TooltipIconBtn
import com.dwightsckrute.visto.core.ui.navigation.NavRoutes
import com.dwightsckrute.visto.core.ui.theme.AppMotion
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.feature.home.HomeScreen
import com.dwightsckrute.visto.feature.main.components.MainBottomBar
import com.dwightsckrute.visto.feature.movie.MovieHomeScreen
import com.dwightsckrute.visto.feature.search.SearchType
import com.dwightsckrute.visto.feature.tv.TvHomeScreen

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun MainScreen(
    navController: NavController
) {

    val viewModel: MainScreenNavViewModel = viewModel()

    val selectedDestination = viewModel.selectedDestination

    val scrollBehavior =
        FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = Bottom)

    val scrollBehaviorTopBar = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // El saludo de Inicio pasa a la barra al desplazar, como el título de Ajustes.
    //
    // Es un **relevo**, no dos piezas que aparecen y desaparecen por su cuenta: el mismo recorrido
    // que aparta el saludo grande trae el pequeño, así que mientras uno se va el otro llega y en
    // ningún momento se ven los dos enteros. El primer intento los animaba por separado con un
    // umbral, y había un tramo con saludo arriba y abajo a la vez.
    //
    // El recorrido sale del desplazamiento del contenido y no del plegado de la barra: esta barra
    // entra y sale entera en lugar de encoger, así que su fracción de plegado mide si la barra se
    // ve, no si el saludo sigue ahí — que es lo que hace falta saber.
    val homeListState = rememberLazyListState()
    val handover by remember {
        derivedStateOf {
            val first = homeListState.layoutInfo.visibleItemsInfo.firstOrNull()
            when {
                homeListState.firstVisibleItemIndex > 0 -> 1f
                first == null || first.size == 0 -> 0f
                else -> (homeListState.firstVisibleItemScrollOffset / first.size.toFloat())
                    .coerceIn(0f, 1f)
            }
        }
    }
    val greeting = homeGreeting()

    val stateHolder = rememberSaveableStateHolder()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehaviorTopBar,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                title = {
                    // En Inicio no: el saludo que hay justo debajo ya dice dónde estás, y
                    // "Inicio" encima de "Buenas tardes" son dos encabezados seguidos diciendo lo
                    // mismo. Las demás secciones sí lo necesitan, porque su contenido empieza
                    // directamente con una lista.
                    if (selectedDestination == MainDestination.Home) {
                        // El saludo, que aparece al desplazar. `graphicsLayer` y no un `if`: así
                        // el hueco ya está medido y la barra no da un salto cuando entra.
                        Text(
                            text = greeting,
                            // Llega desde abajo con el mismo recorrido: no aparece, sube.
                            modifier = Modifier.graphicsLayer {
                                alpha = handover
                                translationY = (1f - handover) * GREETING_RISE_PX
                            },
                        )
                    } else {
                        Text(selectedDestination.label)
                    }
                },
                actions = {
                    TooltipIconBtn(
                        onClick = { navController.navigate(NavRoutes.CALENDAR) },
                        icon = R.drawable.date_range_24px,
                        tooltipText = localized("Diario", "Diary")
                    )
                    TooltipIconBtn(
                        onClick = { navController.navigate(NavRoutes.LISTS_SCREEN) },
                        icon = R.drawable.lists_24px,
                        tooltipText = localized("Listas", "Lists")
                    )
                    TooltipIconBtn(
                        onClick = { navController.navigate(NavRoutes.SETTINGS) },
                        icon = R.drawable.settings_24px,
                        tooltipText = localized("Ajustes", "Settings")
                    )
                }
            )
        },
        bottomBar = {
            MainBottomBar(
                selected = selectedDestination,
                onSelect = viewModel::selectDestination,
                onSearchClick = {
                    navController.navigate(NavRoutes.search(SearchType.MULTI))
                },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        ) {
            AnimatedContent(
                targetState = selectedDestination,
                // Fade through, no deslizamiento. Los tres destinos son hermanos, no hay
                // jerarquía espacial entre ellos, y sobre todo: deslizar obliga a dibujar dos
                // pantallas completas a la vez mientras la entrante hace su primera
                // composición. Medido en dispositivo, ese solape disparaba el percentil 99 de
                // tiempo de frame. Con la salida y la entrada encadenadas, la pantalla nueva
                // se compone mientras aún está invisible.
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = AppMotion.DurationMedium,
                            delayMillis = AppMotion.DurationShort,
                        )
                    ) togetherWith fadeOut(
                        animationSpec = tween(durationMillis = AppMotion.DurationShort)
                    // sizeTransform a null: los destinos ocupan el mismo hueco, así que animar
                    // el tamaño solo añade una medida de layout por frame sin aportar nada.
                    ) using null
                },
                label = "main-destination",
            ) { destination ->
                // Conserva el estado propio de cada destino (posición de scroll, pestaña
                // interna) al ir y volver, en vez de reconstruirlo desde cero cada vez.
                stateHolder.SaveableStateProvider(destination.name) {
                    when (destination) {
                        MainDestination.Home -> HomeScreen(
                            navController,
                            listState = homeListState,
                            greetingAlpha = { 1f - handover },
                        )

                        MainDestination.Movies -> MovieHomeScreen(
                            navController,
                            scrollBehavior,
                            scrollBehaviorTopBar,
                        )

                        MainDestination.Series -> TvHomeScreen(
                            navController,
                            scrollBehavior,
                            scrollBehaviorTopBar,
                        )

                        MainDestination.Books -> BooksHomeScreen(
                            navController,
                            scrollBehavior,
                            scrollBehaviorTopBar,
                        )
                    }
                }
            }
        }
    }
}

/** Desde cuánto más abajo llega el saludo a la barra, en píxeles de capa gráfica. */
private const val GREETING_RISE_PX = 28f
