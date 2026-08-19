package com.dwightsckrute.visto.core.network

import com.dwightsckrute.visto.core.utils.PreferencesHelper
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * La clave de Google Books, que es opcional.
 *
 * Sin ella la aplicación funciona igual que antes de que existiera: Open Library da el catálogo
 * entero. Lo que aporta Google Books es la sinopsis en el idioma de quien lee, que Open Library
 * no puede dar —guarda una sola descripción por obra, en el idioma en que la escribieran, y sus
 * ediciones en español no traen ninguna propia—.
 *
 * Sin clave no se llama siquiera: la cuota anónima compartida de Google está agotada y responde
 * 429, así que intentarlo solo serviría para esperar a un error.
 */
object GoogleBooksApiKey {
    const val PREF_KEY = "google_books_api_key"

    fun userKey(): String? =
        PreferencesHelper.getString(PREF_KEY)?.trim()?.takeIf { it.isNotEmpty() }

    fun isConfigured(): Boolean = userKey() != null

    fun set(value: String) = PreferencesHelper.setString(PREF_KEY, value.trim())

    fun clear() = PreferencesHelper.setString(PREF_KEY, "")
}

/**
 * Google Books, usado solo para rellenar lo que a Open Library le falta.
 *
 * No sustituye al catálogo: los libros se buscan, identifican y guardan con Open Library, cuyas
 * claves son las que usa la biblioteca. Esto se consulta por título y autor, que es lo que hay
 * cuando ya se tiene el libro delante.
 */
interface GoogleBooksApi {

    @GET("books/v1/volumes")
    suspend fun volumes(
        @Query("q") query: String,
        @Query("langRestrict") language: String,
        @Query("maxResults") maxResults: Int = 3,
        @Query("key") key: String,
    ): GoogleBooksResponse

    companion object {
        private const val BASE = "https://www.googleapis.com/"

        fun create(): GoogleBooksApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GoogleBooksApi::class.java)
        }
    }
}

data class GoogleBooksResponse(
    @SerializedName("items") val items: List<GoogleBookItem>? = null,
)

data class GoogleBookItem(
    @SerializedName("volumeInfo") val volumeInfo: GoogleBookVolumeInfo?,
)

data class GoogleBookVolumeInfo(
    @SerializedName("title") val title: String?,
    @SerializedName("authors") val authors: List<String>?,
    @SerializedName("description") val description: String?,
    @SerializedName("pageCount") val pageCount: Int?,
    @SerializedName("language") val language: String?,
)
