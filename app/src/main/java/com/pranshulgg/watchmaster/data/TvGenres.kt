package com.pranshulgg.watchmaster.data

data class TvGenre(
    val id: Int,
    val name: String
)

val LOCAL_TV_GENRES = listOf(
    TvGenre(10759, "Acción y aventura"),
    TvGenre(16, "Animación"),
    TvGenre(35, "Comedia"),
    TvGenre(80, "Crimen"),
    TvGenre(99, "Documental"),
    TvGenre(18, "Drama"),
    TvGenre(10751, "Familia"),
    TvGenre(10762, "Infantil"),
    TvGenre(9648, "Misterio"),
    TvGenre(10763, "Noticias"),
    TvGenre(10764, "Telerrealidad"),
    TvGenre(10765, "Ciencia ficción y fantasía"),
    TvGenre(10766, "Telenovela"),
    TvGenre(10767, "Programa de entrevistas"),
    TvGenre(10768, "Guerra y política"),
    TvGenre(37, "Wéstern")
)

val TV_GENRE_MAP: Map<Int, String> =
    LOCAL_TV_GENRES.associate { it.id to it.name }

fun getTvGenreNames(genreIds: List<Int>): List<String> {
    return genreIds.mapNotNull { TV_GENRE_MAP[it] }
}
