package com.dwightsckrute.visto.feature.home.stat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.ui.components.EmptyContainerPlaceholder
import com.dwightsckrute.visto.core.ui.components.LargeTopBarScaffold
import com.dwightsckrute.visto.core.ui.components.NavigateUpBtn
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.data.local.entity.SeasonEntity
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.data.repository.MEDIA_TYPE_BOOK
import com.dwightsckrute.visto.feature.shared.WatchlistViewModel
import com.dwightsckrute.visto.feature.shared.media.components.MovieWatchlistRow
import com.dwightsckrute.visto.feature.shared.media.components.tv.TvWatchlistRow

/**
 * Lo que hay detrás de una cifra del inicio.
 *
 * Las píldoras del resumen decían "39 guardado" y ahí se acababa: el número es la respuesta a una
 * pregunta —¿cuánto tengo?— que casi siempre lleva a la siguiente —¿cuáles?—, y no había forma de
 * llegar a la segunda desde la primera.
 *
 * Una sola pantalla para las cinco, porque lo único que cambia entre ellas es el filtro. Y sin
 * pestañas: aquí no se viene a explorar, se viene a ver una lista concreta que ya se ha pedido
 * por su nombre, así que separar películas de series obligaría a elegir otra vez algo que ya se
 * eligió.
 */
@Composable
fun LibraryStatScreen(navController: NavController, kind: LibraryStat) {
    val watchlistViewModel: WatchlistViewModel = hiltViewModel()
    val items by watchlistViewModel.watchlist.collectAsStateWithLifecycle()
    val seasons by watchlistViewModel.seasons.collectAsStateWithLifecycle()

    // Memorizado como en las demás listas: filtrar en cada recomposición entrega una List nueva
    // cada vez, y las filas se redibujan enteras aunque no haya cambiado nada.
    val movies = remember(items, kind) {
        items.filter { it.mediaType != "tv" && kind.matches(it) }
    }

    // Una serie no tiene estado: lo tienen sus temporadas, que es también como se cuentan arriba.
    // Así que la serie aparece si alguna temporada suya encaja, y al desplegarla se ven solo
    // esas: entrar desde "en curso" y encontrarse dentro las temporadas terminadas sería
    // enseñar otra cosa distinta de la que se pidió.
    val seasonsByShow = remember(seasons, kind) {
        seasons.filter(kind::matches).groupBy { it.showId }
    }
    val tv = remember(items, kind, seasonsByShow) {
        items.filter { item ->
            item.mediaType == "tv" &&
                kind.matches(item) &&
                seasonsByShow[item.id].orEmpty().isNotEmpty()
        }
    }
    val isEmpty = movies.isEmpty() && tv.isEmpty()

    LargeTopBarScaffold(
        title = kind.title(),
        navigationIcon = { NavigateUpBtn(navController) },
    ) { padding ->
        if (isEmpty) {
            EmptyContainerPlaceholder(
                icon = R.drawable.bookmark_24px,
                text = localized("Nada por aquí todavía", "Nothing here yet"),
            )
            return@LargeTopBarScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = 24.dp,
            ),
            // La misma separación que el resto de listas de la aplicación. Sin ella las filas se
            // tocan, y como cada una redondea sus esquinas según su sitio en la lista, dos
            // redondeos pegados se leen como tarjetas montadas una encima de otra.
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(movies, key = { _, item -> item.id }) { index, item ->
                MovieWatchlistRow(
                    item,
                    index,
                    movies,
                    navController,
                    onLongActionMovieRequest = {},
                )
            }

            itemsIndexed(tv, key = { _, item -> item.id }) { index, item ->
                val showSeasons = seasonsByShow[item.id].orEmpty()
                TvWatchlistRow(
                    item,
                    index,
                    tv,
                    navController,
                    onLongActionTvSeasonRequest = { _, _ -> },
                    seasons = showSeasons,
                    allSeasons = showSeasons,
                )
            }
        }
    }
}

/**
 * Cada cifra del inicio y qué cuenta.
 *
 * El filtro vive aquí y no en la pantalla de inicio porque es lo mismo que ya se usa para contar:
 * si el criterio de "en curso" viviera en dos sitios, el número y la lista podrían discrepar, y
 * un resumen que no cuadra con lo que enseña es peor que no tener resumen.
 */
enum class LibraryStat {
    SAVED,
    IN_PROGRESS,
    FINISHED,
    BOOKS,
    FAVOURITES;

    /**
     * Si una película, un libro o una serie entra en esta cifra.
     *
     * Una serie se deja pasar en "en curso" y "visto" aunque su propio estado no diga nada,
     * porque no es suyo: lo tienen las temporadas, y de eso se encarga la otra comprobación.
     * Filtrarla aquí por `item.status` la habría descartado antes de mirar dónde vive el dato.
     */
    fun matches(item: WatchlistItemEntity): Boolean {
        val isSeries = item.mediaType == "tv"
        return when (this) {
            SAVED -> true
            IN_PROGRESS -> isSeries || item.status == WatchStatus.WATCHING
            FINISHED -> isSeries || item.status == WatchStatus.FINISHED
            BOOKS -> item.mediaType == MEDIA_TYPE_BOOK
            FAVOURITES -> item.isFavorite
        }
    }

    /**
     * Y si una temporada entra.
     *
     * Separado del anterior porque el estado de una serie no está en la serie: la cifra de "en
     * curso" del inicio suma películas en curso **y temporadas** en curso, así que filtrar la
     * serie por su propio estado daría una lista que no cuadra con el número que la abrió.
     *
     * Un libro nunca es una temporada, de ahí el `false`.
     */
    fun matches(season: SeasonEntity): Boolean = when (this) {
        SAVED -> true
        IN_PROGRESS -> season.status == WatchStatus.WATCHING
        FINISHED -> season.status == WatchStatus.FINISHED
        BOOKS -> false
        // El favorito se marca en la serie, no en cada temporada, así que si la serie lo es,
        // entran todas.
        FAVOURITES -> true
    }

    @Composable
    fun title(): String = when (this) {
        SAVED -> localized("Guardado", "Saved")
        IN_PROGRESS -> localized("En curso", "Watching")
        FINISHED -> localized("Visto", "Watched")
        BOOKS -> localized("Libros", "Books")
        FAVOURITES -> localized("Favoritos", "Favourites")
    }
}
