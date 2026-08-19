package com.dwightsckrute.visto.core.network

import com.google.gson.annotations.SerializedName
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Open Library, el catálogo del Internet Archive.
 *
 * Hace para los libros lo que TMDB para el cine, con una diferencia que decidió la elección: no
 * pide clave. Probado contra títulos en español —Patria, El infinito en un junco, La mala
 * costumbre— los encuentra todos y todos traen portada.
 *
 * Su punto flaco es la sinopsis: de cada cuatro obras, alrededor de dos no la tienen. Se asume a
 * cambio de no exigirle al usuario darse de alta en ningún sitio. Google Books las tiene mejores,
 * pero sin clave devuelve 429 —su cuota anónima está agotada— y con clave son mil peticiones al
 * día y unas condiciones más estrictas.
 */
interface OpenLibraryApi {

    @GET("search.json")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        // Pedir solo lo que se usa: la respuesta completa de Open Library trae decenas de campos
        // por obra y en una búsqueda de veinte resultados eso es medio megabyte de más.
        @Query("fields") fields: String = SEARCH_FIELDS,
    ): OpenLibrarySearchResponse

    /** La ficha de una obra: es donde vive la sinopsis, que la búsqueda no devuelve. */
    @GET("works/{workId}.json")
    suspend fun work(@Path("workId") workId: String): OpenLibraryWork

    companion object {
        private const val BASE = "https://openlibrary.org/"
        private const val SEARCH_FIELDS =
            "key,title,author_name,first_publish_year,cover_i,language,number_of_pages_median,subject"
        private const val HTTP_CACHE_BYTES = 10L * 1024 * 1024

        /** La portada, por su identificador. `L` es la grande; hay `S` y `M` para listas. */
        fun coverUrl(coverId: Long?, size: Char = 'L'): String? =
            coverId?.let { "https://covers.openlibrary.org/b/id/$it-$size.jpg" }

        fun create(cacheDir: File?): OpenLibraryApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .apply {
                    if (cacheDir != null) {
                        cache(Cache(File(cacheDir, "openlibrary-http"), HTTP_CACHE_BYTES))
                    }
                }
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenLibraryApi::class.java)
        }
    }
}

data class OpenLibrarySearchResponse(
    @SerializedName("numFound") val numFound: Int = 0,
    @SerializedName("docs") val docs: List<OpenLibraryDoc> = emptyList(),
)

data class OpenLibraryDoc(
    /** Viene como "/works/OL278437W". */
    @SerializedName("key") val key: String,
    @SerializedName("title") val title: String?,
    @SerializedName("author_name") val authorName: List<String>?,
    @SerializedName("first_publish_year") val firstPublishYear: Int?,
    @SerializedName("cover_i") val coverId: Long?,
    @SerializedName("language") val language: List<String>?,
    @SerializedName("number_of_pages_median") val pages: Int?,
    @SerializedName("subject") val subjects: List<String>?,
)

data class OpenLibraryWork(
    @SerializedName("key") val key: String?,
    @SerializedName("title") val title: String?,
    /**
     * Open Library devuelve la sinopsis como texto suelto en unas obras y como objeto
     * `{"type": ..., "value": ...}` en otras. Gson no puede con las dos formas en un solo campo,
     * así que se deserializa sin tipo y se resuelve en [descriptionText].
     */
    @SerializedName("description") val description: Any?,
    @SerializedName("subjects") val subjects: List<String>?,
) {
    val descriptionText: String?
        get() = when (val d = description) {
            is String -> d
            is Map<*, *> -> d["value"] as? String
            else -> null
        }
}
