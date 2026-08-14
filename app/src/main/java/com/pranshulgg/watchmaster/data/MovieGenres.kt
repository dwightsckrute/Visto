package com.pranshulgg.watchmaster.data

data class MovieGenre(
    val id: Int,
    val name: String
)

val LOCAL_MOVIE_GENRES = listOf(
    MovieGenre(28, "Acción"),
    MovieGenre(12, "Aventura"),
    MovieGenre(10759, "Acción y aventura"),

    MovieGenre(16, "Animación"),
    MovieGenre(35, "Comedia"),
    MovieGenre(80, "Crimen"),
    MovieGenre(99, "Documental"),
    MovieGenre(18, "Drama"),

    MovieGenre(10751, "Familia"),
    MovieGenre(10762, "Infantil"),

    MovieGenre(14, "Fantasía"),
    MovieGenre(10765, "Ciencia ficción y fantasía"),
    MovieGenre(878, "Ciencia ficción"),

    MovieGenre(36, "Historia"),
    MovieGenre(27, "Terror"),
    MovieGenre(10402, "Música"),

    MovieGenre(9648, "Misterio"),
    MovieGenre(10749, "Romance"),

    MovieGenre(53, "Suspense"),
    MovieGenre(10752, "Bélica"),
    MovieGenre(10768, "Guerra y política"),

    MovieGenre(37, "Wéstern"),

    MovieGenre(10763, "Noticias"),
    MovieGenre(10764, "Telerrealidad"),
    MovieGenre(10766, "Telenovela"),
    MovieGenre(10767, "Programa de entrevistas"),

    MovieGenre(10770, "Película para televisión")
)

val GENRE_MAP: Map<Int, String> =
    LOCAL_MOVIE_GENRES.associate { it.id to it.name }

fun getMovieGenreNames(genreIds: List<Int>): List<String> {
    return genreIds.mapNotNull { GENRE_MAP[it] }
}
