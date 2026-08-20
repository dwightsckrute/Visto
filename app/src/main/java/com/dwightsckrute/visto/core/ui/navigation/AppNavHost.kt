package com.dwightsckrute.visto.core.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dwightsckrute.visto.feature.books.BookDetailScreen
import com.dwightsckrute.visto.feature.calendar.CalendarScreen
import com.dwightsckrute.visto.feature.main.MainScreen
import com.dwightsckrute.visto.feature.movie.detail.MovieDetailPage
import com.dwightsckrute.visto.feature.lists.MovieListsScreen
import com.dwightsckrute.visto.feature.lists.listEntry.ListEntryScreen
import com.dwightsckrute.visto.feature.lists.view.ViewListScreen
import com.dwightsckrute.visto.feature.person.PersonScreen
import com.dwightsckrute.visto.feature.search.SearchScreen
import com.dwightsckrute.visto.feature.search.SearchType
import com.dwightsckrute.visto.feature.setting.SettingsScreen
import com.dwightsckrute.visto.feature.tv.detail.TvDetailsScreen

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    motionScheme: MotionScheme,
) {
    Box(
        Modifier.fillMaxSize()
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            Modifier
                .fillMaxWidth()
                .zIndex(1f)
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
        )
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavHost(
                    navController = navController,
            startDestination = NavRoutes.MAIN,
//            startDestination = NavRoutes.personScreen(287),
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer),
            enterTransition = { NavTransitions.enter() },
            exitTransition = { NavTransitions.exit() },
            popEnterTransition = { NavTransitions.popEnter() },
            popExitTransition = { NavTransitions.popExit() }
        ) {
            animatedComposable(
                NavRoutes.MAIN
            ) {
                MainScreen(navController)
            }
            animatedComposable(
                NavRoutes.SETTINGS
            ) {
                SettingsScreen(navController)
            }
            animatedComposable(
                NavRoutes.CALENDAR
            ) {
                CalendarScreen(navController)
            }
            animatedComposable(
                route = "${NavRoutes.MOVIE_DETAIL_SCREEN}/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments!!.getLong("id")
                MovieDetailPage(id = id, navController)
            }
            animatedComposable(
                route = "${NavRoutes.SEARCH}?searchType={searchType}",
                arguments = listOf(
                    navArgument("searchType") {
                        type = NavType.StringType
                        defaultValue = SearchType.MULTI.name
                    }
                )
            ) { backStackEntry ->

                val searchType = backStackEntry.arguments
                    ?.getString("searchType")
                    ?.let { SearchType.valueOf(it) }
                    ?: SearchType.MULTI

                SearchScreen(
                    navController = navController,
                    searchType = searchType
                )
            }
            animatedComposable(
                route = "${NavRoutes.TV_DETAIL_SCREEN}/{id}/{seasonNumber}/{seasonId}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType },
                    navArgument("seasonNumber") { type = NavType.IntType },
                    navArgument("seasonId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments!!.getLong("id")
                val seasonNumber = backStackEntry.arguments!!.getInt("seasonNumber")
                val seasonId = backStackEntry.arguments!!.getLong("seasonId")

                TvDetailsScreen(
                    id = id,
                    seasonNumber = seasonNumber,
                    navController = navController,
                    seasonId = seasonId
                )
            }
            animatedComposable(
                route = "${NavRoutes.BOOK_DETAIL_SCREEN}/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments!!.getLong("id")
                BookDetailScreen(id = id, navController = navController)
            }
            animatedComposable(
                NavRoutes.LISTS_SCREEN
            ) {
                MovieListsScreen(navController)
            }
            animatedComposable(
                route = "${NavRoutes.LISTS_ENTRY_SCREEN}/{id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: -1L
                ListEntryScreen(id, navController)

            }
            animatedComposable(
                route = "${NavRoutes.LISTS_VIEW_SCREEN}/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments!!.getLong("id")
                ViewListScreen(navController, id)

            }
            animatedComposable(
                route = "${NavRoutes.PERSON_SCREEN}/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments!!.getLong("id")
                PersonScreen(id = id, navController)
            }
            }
        }
        }
    }

}

/**
 * `composable` con el ámbito de animación puesto a disposición del subárbol.
 *
 * `NavHost` no ofrece ningún sitio donde envolver todos sus destinos a la vez, y el ámbito solo
 * existe dentro de la lambda de cada uno. Esto evita repetir el proveedor en cada destino, que
 * son otros tantos sitios donde olvidarlo.
 */
private fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit,
) = composable(route, arguments) { entry ->
    CompositionLocalProvider(LocalNavAnimatedScope provides this) {
        content(entry)
    }
}
