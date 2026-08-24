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
 * fila: una lista de veinte libros descargaba veinte imágenes grandes para enseñarlas en
 * miniatura, y eso era la lentitud al abrir la sección.
 *
 * Lo que hay que saber para elegir bien es cuánto mide cada letra de verdad, porque no se parecen
 * a lo que sugiere su nombre. Medido sobre una portada cualquiera:
 *
 * ```
 *   S    38 x 58     2 KB
 *   M   180 x 275   24 KB
 *   L   326 x 500   71 KB
 * ```
 *
 * `S` no es una miniatura: es un icono. Aquí se pedía para las filas de 80 puntos —unos 240
 * píxeles en pantalla— y se dibujaba ampliada seis veces, que es exactamente el aspecto pastoso
 * que tienen las portadas en la lista de libros.
 *
 * De ahí que los cortes sean esos números y no otros redondos: se compara lo que se pide con lo
 * que cada letra entrega. Pedir 154 y recibir 38 no es "aproximadamente lo que querías".
 *
 * Solo toca las de Open Library, y solo si acaban como ella las nombra; cualquier otra URL pasa
 * intacta.
 */
private fun openLibrarySized(url: String, size: String): String {
    if (!url.contains("covers.openlibrary.org")) return url
    val wanted = when (size.removePrefix("w").toIntOrNull() ?: 0) {
        in 1..OPEN_LIBRARY_SMALL_WIDTH -> "S"
        in 1..OPEN_LIBRARY_MEDIUM_WIDTH -> "M"
        else -> "L"
    }
    return Regex("-[SML]\\.jpg$").replace(url, "-$wanted.jpg")
}

/** Ancho real en píxeles de la portada `-S` de Open Library. */
private const val OPEN_LIBRARY_SMALL_WIDTH = 38

/** Ancho real en píxeles de la portada `-M`. */
private const val OPEN_LIBRARY_MEDIUM_WIDTH = 180
