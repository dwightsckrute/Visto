package com.dwightsckrute.visto.feature.search

enum class SearchType(val endpoint: String) {
    MULTI("search/multi"),
    MOVIE("search/movie"),
    TV("search/tv"),
    PERSON("search/person"),

    /**
     * Los libros no salen de TMDB, así que este `endpoint` no se usa: `SearchRepository` desvía
     * la llamada a Open Library. Vive aquí de todos modos para que buscar libros sea otro tipo de
     * búsqueda y no otra pantalla.
     */
    BOOK("openlibrary")
}
