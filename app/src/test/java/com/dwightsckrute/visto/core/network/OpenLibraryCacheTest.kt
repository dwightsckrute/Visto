package com.dwightsckrute.visto.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.nio.file.Files

/**
 * Que la caché del catálogo guarda de verdad.
 *
 * Se prueba porque ya falló una vez sin avisar: había una caché de OkHttp declarada y no guardaba
 * nada, porque Open Library no manda cabeceras de caché y sin ellas OkHttp no almacena. Eso no da
 * ningún error, solo lentitud, y la lentitud se atribuye a la red.
 *
 * El servidor simulado responde igual que el real —sin `Cache-Control`— y la prueba comprueba que
 * la segunda llamada no llega a salir.
 */
class OpenLibraryCacheTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `la segunda peticion identica no vuelve a salir a la red`() = runBlocking {
        val body = """{"numFound":1,"docs":[{"key":"/works/OL1W","title":"Prueba"}]}"""
        repeat(2) {
            server.enqueue(MockResponse().setBody(body).setHeader("Content-Type", "application/json"))
        }

        val cacheDir = Files.createTempDirectory("openlibrary-cache-test").toFile()
        val api = OpenLibraryApi.createForTesting(server.url("/").toString(), cacheDir)

        api.search("prueba")
        api.search("prueba")

        assertEquals(
            "la segunda búsqueda debería servirse de la caché",
            1,
            server.requestCount,
        )
    }
}
