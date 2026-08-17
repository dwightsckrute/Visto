package com.pranshulgg.watchmaster.data

import com.pranshulgg.watchmaster.core.ui.localization.AppLanguage

/**
 * Nombres de género locales.
 *
 * TMDB devuelve solo los ids en los listados, así que los nombres se resuelven aquí. Al llevar
 * las dos variantes, cambiar el idioma de la aplicación también cambia los géneros; antes se
 * quedaban en español aunque el resto de la interfaz estuviera en inglés.
 */
data class MovieGenre(
    val id: Int,
    val name: String,
    val nameEn: String,
)

val LOCAL_MOVIE_GENRES = listOf(
    MovieGenre(28, "Acción", "Action"),
    MovieGenre(12, "Aventura", "Adventure"),
    MovieGenre(10759, "Acción y aventura", "Action & Adventure"),

    MovieGenre(16, "Animación", "Animation"),
    MovieGenre(35, "Comedia", "Comedy"),
    MovieGenre(80, "Crimen", "Crime"),
    MovieGenre(99, "Documental", "Documentary"),
    MovieGenre(18, "Drama", "Drama"),

    MovieGenre(10751, "Familia", "Family"),
    MovieGenre(10762, "Infantil", "Kids"),
    MovieGenre(14, "Fantasía", "Fantasy"),
    MovieGenre(10765, "Ciencia ficción y fantasía", "Sci-Fi & Fantasy"),
    MovieGenre(878, "Ciencia ficción", "Science Fiction"),
    MovieGenre(36, "Historia", "History"),
    MovieGenre(27, "Terror", "Horror"),
    MovieGenre(9648, "Misterio", "Mystery"),
    MovieGenre(10402, "Música", "Music"),
    MovieGenre(10749, "Romance", "Romance"),
    MovieGenre(53, "Suspense", "Thriller"),
    MovieGenre(10752, "Bélica", "War"),
    MovieGenre(10768, "Guerra y política", "War & Politics"),

    MovieGenre(37, "Wéstern", "Western"),

    MovieGenre(10763, "Noticias", "News"),
    MovieGenre(10764, "Telerrealidad", "Reality"),
    MovieGenre(10766, "Telenovela", "Soap"),
    MovieGenre(10767, "Programa de entrevistas", "Talk"),

    MovieGenre(10770, "Película para televisión", "TV Movie"),
)

val GENRE_MAP: Map<Int, MovieGenre> =
    LOCAL_MOVIE_GENRES.associateBy { it.id }

fun getMovieGenreNames(genreIds: List<Int>): List<String> {
    return genreIds.mapNotNull { id ->
        GENRE_MAP[id]?.let { AppLanguage.text(it.name, it.nameEn) }
    }
}
