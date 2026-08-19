package com.dwightsckrute.visto.core.utils

/**
 * La URL de una carátula, venga del catálogo que venga.
 *
 * TMDB entrega rutas relativas ("/abc.jpg") que hay que componer con su servidor y un tamaño;
 * Open Library entrega la URL completa. Con dos catálogos, componer a mano en cada pantalla —que
 * es lo que había, en veinticinco sitios— significa que el primer libro que llegue a una lista de
 * películas pide "image.tmdb.org/t/p/w154https://covers.openlibrary.org/...".
 *
 * Distinguir por el propio valor y no por el tipo de medio es lo que permite arreglarlo sin tocar
 * los veinticinco: quien ya trae esquema es una URL y se deja pasar.
 */
fun posterUrl(path: String?, size: String = "w154"): String? = when {
    path.isNullOrBlank() -> null
    path.startsWith("http") -> path
    else -> "https://image.tmdb.org/t/p/$size$path"
}
