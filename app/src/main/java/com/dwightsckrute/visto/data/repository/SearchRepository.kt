package com.dwightsckrute.visto.data.repository

import com.dwightsckrute.visto.core.network.TmdbApi
import com.dwightsckrute.visto.core.network.TvSeasonDto
import com.dwightsckrute.visto.feature.search.SearchItem
import com.dwightsckrute.visto.feature.search.SearchType

class SearchRepository(
    private val api: TmdbApi,
    private val bookRepository: BookRepository,
) {
    suspend fun search(
        query: String,
        type: SearchType = SearchType.MULTI
    ): List<SearchItem> {

        // Los libros vienen de otro catálogo. Se resuelven antes de tocar TMDB, y en la búsqueda
        // general se añaden a lo que devuelva TMDB en vez de sustituirlo: buscar "dune" tiene que
        // seguir dando la película, con el libro al lado.
        if (type == SearchType.BOOK) return bookRepository.searchAsItems(query)

        val resp = api.search(type.endpoint, query)


        if (!resp.isSuccessful) return emptyList()

        val books = if (type == SearchType.MULTI) {
            runCatching { bookRepository.searchAsItems(query) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        val fromTmdb = resp.body()?.results
            ?.mapNotNull { r ->

                val mediaType = r.media_type ?: when (type) {
                    SearchType.MOVIE -> "movie"
                    SearchType.TV -> "tv"
                    SearchType.PERSON -> "person"
                    else -> "unknown"
                }

                if (type == SearchType.MULTI && mediaType !in setOf("movie", "tv")) {
                    return@mapNotNull null
                }

                val title = r.title ?: r.name
                ?: r.known_for?.firstOrNull()?.title
                ?: r.known_for?.firstOrNull()?.name
                ?: return@mapNotNull null

                val knownFor = r.known_for?.firstOrNull()

                val poster = when (mediaType) {
                    "movie", "tv" -> r.poster_path
                    "person" -> r.profile_path ?: knownFor?.poster_path
                    else -> null
                }

                val genreIds = when (mediaType) {
                    "movie", "tv" -> r.genreIds
                    "person" -> knownFor?.genreIds
                    else -> null
                }

                val releaseDate = when (mediaType) {
                    "movie" -> r.releaseDate
                    "tv" -> r.firstAirDate
                    "person" -> knownFor?.releaseDate ?: knownFor?.firstAirDate
                    else -> null
                }

                val originalLanguage = when (mediaType) {
                    "movie", "tv" -> r.originalLanguage
                    "person" -> knownFor?.originalLanguage
                    else -> null
                }

                SearchItem(
                    id = r.id,
                    mediaType = mediaType,
                    title = title,
                    overview = r.overview,
                    posterPath = poster,
                    backdropPath = r.backdrop_path ?: "",
                    genreIds = genreIds,
                    releaseDate = releaseDate,
                    originalLanguage = originalLanguage,
                    avg_rating = r.vote_average,
                )
            } ?: emptyList()
        return books + fromTmdb
    }


    suspend fun getSeasonData(tvId: Long): List<TvSeasonDto> {
        val resp = api.getTvSeasons(tvId)
        if (!resp.isSuccessful) return emptyList()
        return resp.body()?.seasons ?: emptyList()
    }


}
