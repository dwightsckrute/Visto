package com.dwightsckrute.visto.data.repository

import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.network.OpenLibraryApi
import com.dwightsckrute.visto.core.network.OpenLibraryDoc
import com.dwightsckrute.visto.data.local.dao.WatchlistDao
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.feature.search.SearchItem
import java.time.Instant

/** El tipo con el que un libro vive en la misma tabla que las películas y las series. */
const val MEDIA_TYPE_BOOK = "book"

/**
 * Desde dónde empiezan los identificadores de los libros.
 *
 * Es el único punto donde los libros no encajaban en lo que ya había. La clave primaria de la
 * biblioteca es un `Long` porque TMDB numera así, y Open Library identifica sus obras como
 * "OL278437W". Cambiar el tipo de la clave habría obligado a migrar la tabla entera y todo lo que
 * la referencia; en su lugar se conserva el número de la clave de Open Library y se desplaza a un
 * tramo donde TMDB no llega —sus identificadores no pasan de ocho cifras—, así que ninguno de los
 * dos catálogos puede pisar al otro y la conversión es reversible en las dos direcciones.
 */
private const val BOOK_ID_OFFSET = 1_000_000_000_000L

/** "/works/OL278437W" -> 1_000_000_278_437. Devuelve null si la clave no tiene esa forma. */
fun openLibraryKeyToId(key: String): Long? =
    Regex("OL(\\d+)W").find(key)?.groupValues?.get(1)?.toLongOrNull()
        ?.let { BOOK_ID_OFFSET + it }

/** El camino de vuelta, para pedirle a Open Library la ficha de algo ya guardado. */
fun idToOpenLibraryKey(id: Long): String? =
    if (id > BOOK_ID_OFFSET) "OL${id - BOOK_ID_OFFSET}W" else null

fun isBookId(id: Long): Boolean = id > BOOK_ID_OFFSET

class BookRepository(
    private val api: OpenLibraryApi,
    private val dao: WatchlistDao,
) {

    /**
     * Busca libros.
     *
     * Descarta las obras cuya clave no se puede convertir a número: sin identificador estable no
     * hay forma de guardarlas ni de volver a ellas, y aparecer en la lista para fallar al tocarlas
     * sería peor que no aparecer.
     */
    suspend fun search(query: String): List<BookResult> {
        if (query.isBlank()) return emptyList()
        return api.search(query).docs.mapNotNull { doc ->
            val id = openLibraryKeyToId(doc.key) ?: return@mapNotNull null
            val title = doc.title ?: return@mapNotNull null
            BookResult(
                id = id,
                title = title,
                authors = doc.authorName.orEmpty(),
                year = doc.firstPublishYear,
                coverUrl = OpenLibraryApi.coverUrl(doc.coverId, 'M'),
                coverId = doc.coverId,
                pages = doc.pages,
            )
        }
    }

    /**
     * Los mismos resultados, con la forma que entiende la búsqueda general.
     *
     * Así un libro entra por el mismo camino que una película: misma lista, misma fila, misma
     * acción de añadir. Lo que lo distingue es su `mediaType`, que la fila usa para etiquetarlo.
     */
    suspend fun searchAsItems(query: String): List<SearchItem> = search(query).map { book ->
        SearchItem(
            id = book.id,
            mediaType = MEDIA_TYPE_BOOK,
            title = book.title,
            overview = null,
            // La portada ya viene como URL completa; `posterUrl` la deja pasar tal cual.
            posterPath = OpenLibraryApi.coverUrl(book.coverId, 'L'),
            backdropPath = "",
            releaseDate = listOfNotNull(book.authors.firstOrNull(), book.year?.toString())
                .joinToString(" · "),
        )
    }

    /**
     * Guarda un libro en la biblioteca.
     *
     * La sinopsis se pide aparte porque la búsqueda no la trae, y se acepta que falte: en Open
     * Library alrededor de la mitad de las obras no tienen. Un libro sin resumen sigue siendo
     * mejor que ningún libro.
     */
    suspend fun add(book: BookResult) {
        val overview = runCatching {
            idToOpenLibraryKey(book.id)?.let { api.work(it).descriptionText }
        }.getOrNull()

        dao.insert(
            WatchlistItemEntity(
                id = book.id,
                mediaType = MEDIA_TYPE_BOOK,
                title = book.title,
                overview = overview,
                posterPath = OpenLibraryApi.coverUrl(book.coverId, 'L'),
                backdropPath = null,
                genreIds = null,
                // El autor va donde iría la fecha de estreno porque es lo que se enseña junto al
                // título en cada fila, y es lo que se busca al mirar una lista de libros.
                releaseDate = book.authors.firstOrNull(),
                status = WatchStatus.WANT_TO_WATCH,
                addedDate = Instant.now(),
            )
        )
    }
}

data class BookResult(
    val id: Long,
    val title: String,
    val authors: List<String>,
    val year: Int?,
    val coverUrl: String?,
    val coverId: Long?,
    val pages: Int?,
)
