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
    path.startsWith("http") -> openLibrarySized(path, size)
    else -> "https://image.tmdb.org/t/p/$size$path"
}

/**
 * Baja la portada de Open Library al tamaño que se va a dibujar.
 *
 * Sus portadas se guardan como `-L`, que es lo que quiere una ficha y un despilfarro para una
 * fila de 56 puntos: una lista de veinte libros descargaba veinte imágenes grandes para
 * enseñarlas en miniatura, y eso era la lentitud al abrir la sección. `S` y `M` pesan una
 * fracción y llegan antes.
 *
 * Solo toca las de Open Library, y solo si acaban como ella las nombra; cualquier otra URL pasa
 * intacta.
 */
private fun openLibrarySized(url: String, size: String): String {
    if (!url.contains("covers.openlibrary.org")) return url
    val wanted = when {
        size.removePrefix("w").toIntOrNull()?.let { it <= 200 } == true -> "S"
        size.removePrefix("w").toIntOrNull()?.let { it <= 400 } == true -> "M"
        else -> "L"
    }
    return Regex("-[SML]\\.jpg$").replace(url, "-$wanted.jpg")
}
