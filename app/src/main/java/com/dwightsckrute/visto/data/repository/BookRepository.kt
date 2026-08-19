package com.dwightsckrute.visto.data.repository

import com.dwightsckrute.visto.core.model.WatchStatus
import com.dwightsckrute.visto.core.network.GoogleBooksApi
import com.dwightsckrute.visto.core.network.GoogleBooksApiKey
import com.dwightsckrute.visto.core.ui.localization.AppLanguage
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

/** Lo que se sabe del autor de un libro, cuando se sabe. */
data class AuthorInfo(
    val id: String,
    val name: String,
    val years: String?,
    val photoUrl: String?,
    val bio: String?,
    val works: List<BookResult>,
    val totalWorks: Int,
)

class BookRepository(
    private val api: OpenLibraryApi,
    private val googleBooks: GoogleBooksApi,
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
     * La sinopsis, en el idioma de quien lee si se puede.
     *
     * Open Library primero, porque es la fuente del catálogo y no cuesta nada. Si lo que devuelve
     * está en otro idioma o no existe, y hay clave de Google Books configurada, se pregunta allí
     * acotando por idioma. Sin clave no se intenta: la cuota anónima de Google está agotada y
     * responde 429.
     *
     * Que falle cualquiera de las dos no impide guardar el libro. Una ficha sin resumen sigue
     * siendo una ficha; no poder añadir el libro sería perderlo.
     */
    private suspend fun fetchOverview(book: BookResult): String? {
        val fromOpenLibrary = runCatching {
            idToOpenLibraryKey(book.id)?.let { api.work(it).descriptionText }
        }.getOrNull()

        val wantedLanguage = AppLanguage.currentCode()
        if (!GoogleBooksApiKey.isConfigured()) return fromOpenLibrary

        val fromGoogle = runCatching {
            val key = GoogleBooksApiKey.userKey() ?: return@runCatching null
            val terms = listOfNotNull(
                book.title,
                book.authors.firstOrNull()?.let { "inauthor:$it" },
            ).joinToString(" ")
            googleBooks.volumes(query = terms, language = wantedLanguage, key = key)
                .items
                ?.firstNotNullOfOrNull { it.volumeInfo?.description?.takeIf(String::isNotBlank) }
        }.getOrNull()

        return fromGoogle ?: fromOpenLibrary
    }

    /**
     * El autor de un libro y lo demás que ha escrito.
     *
     * Sale de Open Library, que lo da gratis y sin clave, y que para autores en español trae
     * hasta la biografía en español. Se resuelve a partir de la obra en lugar de guardarse al
     * añadir el libro: guardarlo habría pedido una columna nueva para algo que solo hace falta
     * cuando se abre la ficha.
     */
    suspend fun authorOf(bookId: Long): AuthorInfo? {
        val workId = idToOpenLibraryKey(bookId) ?: return null
        val authorId = runCatching { api.work(workId).authorId }.getOrNull() ?: return null
        val author = runCatching { api.author(authorId) }.getOrNull() ?: return null
        val works = runCatching { api.authorWorks(authorId) }.getOrNull()

        val others = works?.entries.orEmpty().mapNotNull { entry ->
            val id = entry.key?.let(::openLibraryKeyToId) ?: return@mapNotNull null
            if (id == bookId) return@mapNotNull null
            BookResult(
                id = id,
                title = entry.title ?: return@mapNotNull null,
                authors = listOfNotNull(author.name),
                year = null,
                coverUrl = OpenLibraryApi.coverUrl(entry.covers?.firstOrNull(), 'M'),
                coverId = entry.covers?.firstOrNull(),
                pages = null,
            )
        }

        return AuthorInfo(
            id = authorId,
            name = author.name ?: return null,
            years = listOfNotNull(author.birthDate, author.deathDate)
                .takeIf { it.isNotEmpty() }?.joinToString(" – "),
            photoUrl = OpenLibraryApi.authorPhotoUrl(author.photos?.firstOrNull()),
            bio = author.bioText,
            works = others,
            totalWorks = works?.size ?: others.size,
        )
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
        val overview = fetchOverview(book)

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
