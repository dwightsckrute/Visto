package com.dwightsckrute.visto.data.repository

import com.dwightsckrute.visto.core.network.GoogleBookItem
import com.dwightsckrute.visto.core.network.GoogleBookVolumeInfo
import com.dwightsckrute.visto.core.network.GoogleBooksApi
import com.dwightsckrute.visto.core.network.GoogleBooksResponse
import com.dwightsckrute.visto.core.network.OpenLibraryApi
import com.dwightsckrute.visto.core.network.OpenLibraryAuthor
import com.dwightsckrute.visto.core.network.OpenLibraryAuthorWorks
import com.dwightsckrute.visto.core.network.OpenLibrarySearchResponse
import com.dwightsckrute.visto.core.network.OpenLibraryWork
import com.dwightsckrute.visto.data.local.dao.WatchlistDao
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.feature.search.SearchItem
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookRepositoryTest {

    @Test
    fun `a book added from global search is enriched with Google Books`() = runBlocking {
        var googleWasCalled = false
        var inserted: WatchlistItemEntity? = null
        val dao = Proxy.newProxyInstance(
            WatchlistDao::class.java.classLoader,
            arrayOf(WatchlistDao::class.java),
        ) { _, method, args ->
            if (method.name == "insert") inserted = args?.first() as WatchlistItemEntity
            Unit
        } as WatchlistDao
        val openLibrary = object : OpenLibraryApi {
            override suspend fun search(query: String, limit: Int, fields: String) =
                OpenLibrarySearchResponse()

            override suspend fun work(workId: String) = OpenLibraryWork(
                key = "/works/$workId",
                title = "Patria",
                description = null,
                subjects = null,
            )

            override suspend fun author(authorId: String): OpenLibraryAuthor =
                error("Not used")

            override suspend fun authorWorks(authorId: String, limit: Int): OpenLibraryAuthorWorks =
                error("Not used")
        }
        val googleBooks = object : GoogleBooksApi {
            override suspend fun volumes(
                query: String,
                language: String,
                maxResults: Int,
                key: String,
            ): GoogleBooksResponse {
                googleWasCalled = true
                assertEquals("es", language)
                assertEquals("test-key", key)
                return GoogleBooksResponse(
                    items = listOf(
                        GoogleBookItem(
                            GoogleBookVolumeInfo(
                                title = "Patria",
                                authors = listOf("Fernando Aramburu"),
                                description = "Sinopsis en español",
                                pageCount = 648,
                                language = "es",
                            )
                        )
                    )
                )
            }
        }
        val repository = BookRepository(openLibrary, googleBooks, dao) { "test-key" }

        repository.addFromSearch(
            SearchItem(
                id = openLibraryKeyToId("/works/OL1W")!!,
                mediaType = MEDIA_TYPE_BOOK,
                title = "Patria",
                overview = null,
                posterPath = "https://covers.openlibrary.org/b/id/1-L.jpg",
                backdropPath = "",
                releaseDate = "Fernando Aramburu · 2016",
            )
        )

        assertTrue(googleWasCalled)
        assertEquals("Sinopsis en español", inserted?.overview)
        assertEquals("Fernando Aramburu", inserted?.releaseDate)
    }
}
