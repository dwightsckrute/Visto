package com.dwightsckrute.visto.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * La conversión entre las claves de Open Library y la clave primaria de la biblioteca.
 *
 * Se prueba porque es lo único de los libros que no encajaba en lo que ya había, y porque un fallo
 * aquí no se vería: un identificador mal convertido no rompe nada, simplemente guarda el libro
 * encima de otra cosa o lo deja imposible de recuperar.
 */
class BookIdMappingTest {

    @Test
    fun `una clave de obra se convierte y se recupera igual`() {
        val id = openLibraryKeyToId("/works/OL278437W")
        assertEquals(1_000_000_278_437L, id)
        assertEquals("OL278437W", idToOpenLibraryKey(id!!))
    }

    @Test
    fun `una clave sin la forma esperada no produce identificador`() {
        assertNull(openLibraryKeyToId("/authors/OL34184A"))
        assertNull(openLibraryKeyToId("basura"))
    }

    @Test
    fun `ningun identificador de TMDB cae en el tramo de los libros`() {
        // El mayor identificador plausible de TMDB tiene ocho cifras; el tramo de libros empieza
        // en el billón. Si algún día se acercaran, esta prueba avisaría antes que los usuarios.
        val mayorDeTmdb = 99_999_999L
        assertFalse(isBookId(mayorDeTmdb))
        assertTrue(isBookId(openLibraryKeyToId("/works/OL1W")!!))
    }

    @Test
    fun `una obra con clave muy larga sigue siendo reversible`() {
        val id = openLibraryKeyToId("/works/OL987654321W")!!
        assertEquals("OL987654321W", idToOpenLibraryKey(id))
    }
}
