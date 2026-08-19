package com.dwightsckrute.visto.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.data.local.entity.SeasonEntity
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.data.repository.MEDIA_TYPE_BOOK
import com.dwightsckrute.visto.data.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.dwightsckrute.visto.core.ui.localization.AppLanguage

data class HomeMediaItem(
    val id: Long,
    val title: String,
    val subtitleEs: String,
    val subtitleEn: String,
    val posterPath: String?,
    val mediaType: String,
    val seasonNumber: Int? = null,
    val seasonId: Long? = null,
    val progress: Float? = null,
    val activityDate: Instant,
)

data class HomeUiState(
    val libraryCount: Int = 0,
    val inProgressCount: Int = 0,
    val finishedCount: Int = 0,
    val favoriteCount: Int = 0,
    val continueWatching: List<HomeMediaItem> = emptyList(),
    val recentlyWatched: List<HomeMediaItem> = emptyList(),
    val recentlyAdded: List<HomeMediaItem> = emptyList(),
    /**
     * Los libros van aparte de lo audiovisual.
     *
     * Comparten tabla y estados, pero mezclarlos en "continuar viendo" haría que una fila dijera
     * "viendo" de algo que se lee. Separados, cada mitad se lee con su propio vocabulario.
     */
    val bookCount: Int = 0,
    val readingBooks: List<HomeMediaItem> = emptyList(),
    val recentlyRead: List<HomeMediaItem> = emptyList(),
    /** Cosas terminadas dentro del mes natural en curso. */
    val watchedThisMonth: Int = 0,
) {
    val isEmpty: Boolean get() = libraryCount == 0
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    repository: WatchlistRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getWatchlist(),
        repository.getAllSeasons(),
    ) { watchlist, seasons ->
        buildHomeState(watchlist, seasons)
    }
        // Fuera del hilo de interfaz. `viewModelScope` colecta en Main, así que sin esto el
        // estado entero del inicio —filtrar la biblioteca, mapearla, ordenarla por fecha, tres
        // veces— se construía sobre el hilo que tiene que dibujar, y en cada cambio de la base de
        // datos. Es de los sitios donde más se nota, porque cualquier marca de visto lo dispara.
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )
}

private fun buildHomeState(
    watchlist: List<WatchlistItemEntity>,
    seasons: List<SeasonEntity>,
): HomeUiState {
    val showsById = watchlist.associateBy { it.id }
    val movieActivity = watchlist.filter { it.mediaType == "movie" }
    val bookActivity = watchlist.filter { it.mediaType == MEDIA_TYPE_BOOK }
    val seasonActivity = seasons.filter { it.seasonNumber > 0 }

    val continueWatching = buildList {
        movieActivity
            .filter { it.status == WatchStatus.WATCHING || it.status == WatchStatus.INTERRUPTED }
            .mapTo(this) { movie ->
                HomeMediaItem(
                    id = movie.id,
                    title = movie.title,
                    subtitleEs = if (movie.status == WatchStatus.INTERRUPTED) "Pausada" else AppLanguage.text("Película", "Movie"),
                    subtitleEn = if (movie.status == WatchStatus.INTERRUPTED) "Paused" else "Movie",
                    posterPath = movie.posterPath,
                    mediaType = "movie",
                    activityDate = movie.interruptedAt ?: movie.startedDate ?: movie.addedDate,
                )
            }

        seasonActivity
            .filter { it.status == WatchStatus.WATCHING || it.status == WatchStatus.INTERRUPTED }
            .mapNotNullTo(this) { season ->
                val show = showsById[season.showId] ?: return@mapNotNullTo null
                HomeMediaItem(
                    id = show.id,
                    title = show.title,
                    subtitleEs = "${season.name} · ${season.lastEpWatched ?: 0}/${season.episodeCount} episodios",
                    subtitleEn = "${season.name} · ${season.lastEpWatched ?: 0}/${season.episodeCount} episodes",
                    posterPath = season.posterPath ?: show.posterPath,
                    mediaType = "tv",
                    seasonNumber = season.seasonNumber,
                    seasonId = season.seasonId,
                    progress = if (season.episodeCount > 0) {
                        ((season.lastEpWatched ?: 0).toFloat() / season.episodeCount).coerceIn(0f, 1f)
                    } else null,
                    activityDate = season.seasonInterruptedAt
                        ?: season.seasonStartedDate
                        ?: season.seasonAddedDate,
                )
            }
    }.sortedByDescending { it.activityDate }.take(12)

    val recentlyWatched = buildList {
        movieActivity
            .filter { it.status == WatchStatus.FINISHED && it.finishedDate != null }
            .mapTo(this) { movie ->
                HomeMediaItem(
                    id = movie.id,
                    title = movie.title,
                    subtitleEs = AppLanguage.text("Película", "Movie"),
                    subtitleEn = "Movie",
                    posterPath = movie.posterPath,
                    mediaType = "movie",
                    activityDate = movie.finishedDate!!,
                )
            }

        seasonActivity
            .filter { it.status == WatchStatus.FINISHED && it.seasonFinishedDate != null }
            .mapNotNullTo(this) { season ->
                val show = showsById[season.showId] ?: return@mapNotNullTo null
                HomeMediaItem(
                    id = show.id,
                    title = show.title,
                    subtitleEs = season.name,
                    subtitleEn = season.name,
                    posterPath = season.posterPath ?: show.posterPath,
                    mediaType = "tv",
                    seasonNumber = season.seasonNumber,
                    seasonId = season.seasonId,
                    activityDate = season.seasonFinishedDate!!,
                )
            }
    }.sortedByDescending { it.activityDate }.take(12)

    val recentlyAdded = watchlist
        .sortedByDescending { it.addedDate }
        .take(12)
        .map { item ->
            val firstSeason = if (item.mediaType == "tv") {
                seasons.firstOrNull { it.showId == item.id && it.seasonNumber > 0 }
            } else null
            HomeMediaItem(
                id = item.id,
                title = item.title,
                // Un libro recién añadido se anunciaba como película: aquí solo había dos ramas.
                subtitleEs = when (item.mediaType) {
                    "tv" -> AppLanguage.text("Serie", "TV show")
                    MEDIA_TYPE_BOOK -> AppLanguage.text("Libro", "Book")
                    else -> AppLanguage.text("Película", "Movie")
                },
                subtitleEn = when (item.mediaType) {
                    "tv" -> "TV show"
                    MEDIA_TYPE_BOOK -> "Book"
                    else -> "Movie"
                },
                posterPath = item.posterPath,
                mediaType = item.mediaType,
                seasonNumber = firstSeason?.seasonNumber,
                seasonId = firstSeason?.seasonId,
                activityDate = item.addedDate,
            )
        }

    // Mes natural, no últimos 30 días: es lo que se corresponde con lo que enseña el Diario.
    val startOfMonth = java.time.YearMonth.now(java.time.ZoneId.systemDefault())
        .atDay(1)
        .atStartOfDay(java.time.ZoneId.systemDefault())
        .toInstant()
    val watchedThisMonth = recentlyWatched.count { !it.activityDate.isBefore(startOfMonth) }

    val readingBooks = bookActivity
        .filter { it.status == WatchStatus.WATCHING || it.status == WatchStatus.INTERRUPTED }
        .map { book ->
            HomeMediaItem(
                id = book.id,
                title = book.title,
                subtitleEs = if (book.status == WatchStatus.INTERRUPTED) "Aparcado" else "Leyendo",
                subtitleEn = if (book.status == WatchStatus.INTERRUPTED) "Paused" else "Reading",
                posterPath = book.posterPath,
                mediaType = MEDIA_TYPE_BOOK,
                activityDate = book.interruptedAt ?: book.startedDate ?: book.addedDate,
            )
        }
        .sortedByDescending { it.activityDate }
        .take(12)

    val recentlyRead = bookActivity
        .filter { it.status == WatchStatus.FINISHED && it.finishedDate != null }
        .map { book ->
            HomeMediaItem(
                id = book.id,
                title = book.title,
                subtitleEs = book.releaseDate.orEmpty(),
                subtitleEn = book.releaseDate.orEmpty(),
                posterPath = book.posterPath,
                mediaType = MEDIA_TYPE_BOOK,
                activityDate = book.finishedDate!!,
            )
        }
        .sortedByDescending { it.activityDate }
        .take(12)

    return HomeUiState(
        libraryCount = watchlist.size,
        bookCount = bookActivity.size,
        readingBooks = readingBooks,
        recentlyRead = recentlyRead,
        inProgressCount = movieActivity.count { it.status == WatchStatus.WATCHING } +
            seasonActivity.count { it.status == WatchStatus.WATCHING },
        finishedCount = movieActivity.count { it.status == WatchStatus.FINISHED } +
            seasonActivity.count { it.status == WatchStatus.FINISHED },
        favoriteCount = watchlist.count { it.isFavorite },
        continueWatching = continueWatching,
        recentlyWatched = recentlyWatched,
        recentlyAdded = recentlyAdded,
        watchedThisMonth = watchedThisMonth,
    )
}
