package com.dwightsckrute.visto.feature.home.stat

import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
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
    val films = remember(items, kind) {
        items.filter { it.mediaType == "movie" && kind.matches(it) }
    }
    val books = remember(items, kind) {
        items.filter { it.mediaType == MEDIA_TYPE_BOOK && kind.matches(it) }
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
    val nothingSaved = films.isEmpty() && tv.isEmpty() && books.isEmpty()

    // Con cabecera por tipo, y solo de los que hay.
    //
    // Sin ella la lista mezclaba películas, series y libros sin decirlo: una fila de
    // serie se distingue por el chevrón que despliega temporadas, y una de libro por la
    // forma de la portada, que son señales que hay que saber leer. El título de la
    // sección lo dice y ya está.
    //
    // Y solo si hay más de un tipo: una cabecera "Películas" sobre una lista que solo
    // tiene películas no informa de nada, es ruido con aspecto de estructura.
    val sections = listOfNotNull(
        films.takeIf { it.isNotEmpty() }
            ?.let { localized("Películas", "Films") to it },
        tv.takeIf { it.isNotEmpty() }
            ?.let { localized("Series", "Series") to it },
        books.takeIf { it.isNotEmpty() }
            ?.let { localized("Libros", "Books") to it },
    )
    // Qué tipos se están mirando. Arranca en todos, que es lo que dice la cifra que abrió la
    // pantalla; el filtro está para estrechar, no para tener que elegir antes de ver nada.
    var filter by rememberSaveable { mutableStateOf(StatFilter.ALL) }

    val visible = sections.filter { (_, rows) -> filter.matches(rows.first()) }

    // La cabecera de sección sobra si el filtro ya ha dejado un solo tipo: lo diría dos veces.
    val labelled = visible.size > 1


    LargeTopBarScaffold(
        title = kind.title(),
        navigationIcon = { NavigateUpBtn(navController) },
    ) { padding ->
        if (nothingSaved) {
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

            if (sections.size > 1) {
                item(key = "filter") {
                    StatTypeFilter(
                        selected = filter,
                        available = sections.map { it.second.first() },
                        onSelect = { filter = it },
                    )
                }
            }

            visible.forEach { (heading, rows) ->
                if (labelled) {
                    item(key = "heading-$heading") {
                        Text(
                            text = heading,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                top = if (heading == visible.first().first) 0.dp else 20.dp,
                                bottom = 6.dp,
                                start = 4.dp,
                            ),
                        )
                    }
                }

                itemsIndexed(rows, key = { _, item -> item.id }) { index, item ->
                    if (item.mediaType == "tv") {
                        val showSeasons = seasonsByShow[item.id].orEmpty()
                        TvWatchlistRow(
                            item,
                            index,
                            rows,
                            navController,
                            onLongActionTvSeasonRequest = { _, _ -> },
                            seasons = showSeasons,
                            allSeasons = showSeasons,
                        )
                    } else {
                        MovieWatchlistRow(
                            item,
                            index,
                            rows,
                            navController,
                            onLongActionMovieRequest = {},
                        )
                    }
                }
            }
            if (visible.isEmpty()) {
                item(key = "vacio") {
                    Text(
                        text = localized(
                            "Nada de ese tipo en esta lista",
                            "Nothing of that kind in this list",
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp, start = 4.dp),
                    )
                }
            }
        }
    }
}

/** Los tipos que se pueden mirar por separado dentro de una lista. */
enum class StatFilter {
    ALL,
    FILMS,
    SERIES,
    BOOKS;

    fun matches(item: WatchlistItemEntity): Boolean = when (this) {
        ALL -> true
        FILMS -> item.mediaType == "movie"
        SERIES -> item.mediaType == "tv"
        BOOKS -> item.mediaType == MEDIA_TYPE_BOOK
    }
}

/**
 * Los tipos que hay en esta lista, para elegir cuál mirar.
 *
 * `available` son ejemplares de lo que realmente hay: ofrecer "Libros" en una lista sin ninguno
 * es ofrecer una pantalla vacía.
 */
@Composable
private fun StatTypeFilter(
    selected: StatFilter,
    available: List<WatchlistItemEntity>,
    onSelect: (StatFilter) -> Unit,
) {
    val options = listOfNotNull(
        StatFilter.ALL to localized("Todo", "All"),
        (StatFilter.FILMS to localized("Películas", "Films"))
            .takeIf { available.any { item -> StatFilter.FILMS.matches(item) } },
        (StatFilter.SERIES to localized("Series", "Series"))
            .takeIf { available.any { item -> StatFilter.SERIES.matches(item) } },
        (StatFilter.BOOKS to localized("Libros", "Books"))
            .takeIf { available.any { item -> StatFilter.BOOKS.matches(item) } },
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEach { (type, label) ->
            FilterChip(
                selected = type == selected,
                onClick = { onSelect(type) },
                label = { Text(label) },
            )
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
