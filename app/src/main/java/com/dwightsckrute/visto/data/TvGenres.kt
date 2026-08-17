package com.dwightsckrute.visto.data

import com.dwightsckrute.visto.core.ui.localization.AppLanguage

/** Ver la nota de [MovieGenre]: los nombres se resuelven aquí porque TMDB solo manda ids. */
data class TvGenre(
    val id: Int,
    val name: String,
    val nameEn: String,
)

val LOCAL_TV_GENRES = listOf(
    TvGenre(10759, "Acción y aventura", "Action & Adventure"),
    TvGenre(16, "Animación", "Animation"),
    TvGenre(35, "Comedia", "Comedy"),
    TvGenre(80, "Crimen", "Crime"),
    TvGenre(99, "Documental", "Documentary"),
    TvGenre(18, "Drama", "Drama"),
    TvGenre(10751, "Familia", "Family"),
    TvGenre(10762, "Infantil", "Kids"),
    TvGenre(9648, "Misterio", "Mystery"),
    TvGenre(10763, "Noticias", "News"),
    TvGenre(10764, "Telerrealidad", "Reality"),
    TvGenre(10765, "Ciencia ficción y fantasía", "Sci-Fi & Fantasy"),
    TvGenre(10766, "Telenovela", "Soap"),
    TvGenre(10767, "Programa de entrevistas", "Talk"),
    TvGenre(10768, "Guerra y política", "War & Politics"),
    TvGenre(37, "Wéstern", "Western"),
)

val TV_GENRE_MAP: Map<Int, TvGenre> =
    LOCAL_TV_GENRES.associateBy { it.id }

fun getTvGenreNames(genreIds: List<Int>): List<String> {
    return genreIds.mapNotNull { id ->
        TV_GENRE_MAP[id]?.let { AppLanguage.text(it.name, it.nameEn) }
    }
}
