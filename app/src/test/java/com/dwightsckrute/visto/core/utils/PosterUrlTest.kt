package com.dwightsckrute.visto.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * El tamaño que se pide tiene que llegar entero a la pantalla.
 *
 * Esto existe porque el fallo que arregla no se ve en ninguna traza ni rompe ninguna prueba: las
 * portadas simplemente salían borrosas. La `-S` de Open Library mide 38 píxeles de ancho y se
 * estaba pidiendo para filas de 240, así que se dibujaban ampliadas seis veces. Un desajuste de
 * este tipo solo se nota mirando, y mirando es justo lo que no hace una suite.
 */
class PosterUrlTest {

    private val olCover = "https://covers.openlibrary.org/b/id/8739161-L.jpg"

    @Test
    fun `una fila no recibe el icono de 38 pixeles`() {
        // 154 es lo que piden las filas por defecto. Antes caía en -S.
        assertEquals(
            "https://covers.openlibrary.org/b/id/8739161-M.jpg",
            posterUrl(olCover, "w154"),
        )
    }

    @Test
    fun `una ficha recibe la portada grande`() {
        assertEquals(
            "https://covers.openlibrary.org/b/id/8739161-L.jpg",
            posterUrl(olCover, "w342"),
        )
    }

    @Test
    fun `solo se pide S cuando de verdad cabe en 38 pixeles`() {
        assertEquals(
            "https://covers.openlibrary.org/b/id/8739161-S.jpg",
            posterUrl(olCover, "w32"),
        )
    }

    @Test
    fun `una ruta de TMDB se compone con su servidor y su tamaño`() {
        assertEquals(
            "https://image.tmdb.org/t/p/w342/abc.jpg",
            posterUrl("/abc.jpg", "w342"),
        )
    }

    @Test
    fun `una URL que no es de Open Library pasa intacta`() {
        val other = "https://example.com/portada-L.jpg"
        assertEquals(other, posterUrl(other, "w154"))
    }

    @Test
    fun `sin ruta no hay URL`() {
        assertEquals(null, posterUrl(null))
        assertEquals(null, posterUrl("  "))
    }
}
