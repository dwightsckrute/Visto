package com.dwightsckrute.visto.feature.legal

import com.dwightsckrute.visto.core.ui.components.rememberBounceOverscrollFactory
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.dwightsckrute.visto.BuildConfig
import com.dwightsckrute.visto.R
import com.dwightsckrute.visto.core.ui.components.LargeTopBarScaffold
import com.dwightsckrute.visto.core.ui.components.NavigateUpBtn
import com.dwightsckrute.visto.core.ui.localization.localized
import com.dwightsckrute.visto.core.ui.snackbar.SnackbarManager
import com.dwightsckrute.visto.core.ui.theme.ShapeRadius
import com.dwightsckrute.visto.core.ui.theme.Spacing

/** Privacy, attribution and licence information adapted to Visto's actual data flows. */
@Composable
fun LegalScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val gpl = remember(context) { context.readAsset(GPL_FILE) }
    val ofl = remember(context) { context.readAsset(OFL_FILE) }

    LargeTopBarScaffold(
        title = localized("Privacidad y avisos", "Privacy and notices"),
        navigationIcon = { NavigateUpBtn(onNavigateUp) },
    ) { padding ->
            // El rebote de Palmo en lugar del estirado de serie de Android: al llegar al final
            // el contenido se separa del borde y vuelve con un muelle. Se provee por
            // `LocalOverscrollFactory` y no con un modificador porque `verticalScroll` le pide su
            // efecto a ese local, así que basta con envolver lo que se desplaza.
        CompositionLocalProvider(LocalOverscrollFactory provides rememberBounceOverscrollFactory()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            LegalSection(title = localized("Acerca de Visto", "About Visto")) {
                LegalBody(
                    localized(
                        "Visto lo desarrolla Atalaya Software.",
                        "Visto is developed by Atalaya Software.",
                    )
                )
                // Este párrafo no es promocional y no puede quitarse: la GPL v3 obliga a conservar
                // los avisos de autoría del trabajo del que parte el código. Dejó de encabezar la
                // pantalla —Visto ya no se presenta como una bifurcación— pero el crédito sigue
                // donde tiene que estar, en el aviso legal.
                LegalBody(
                    localized(
                        "Parte del código procede de WatchMaster, de Pranshul Gupta, bajo la misma " +
                            "licencia. El trabajo original y las modificaciones pertenecen a sus " +
                            "respectivos autores.",
                        "Some of the code comes from WatchMaster, by Pranshul Gupta, under the same " +
                            "licence. The original work and the modifications belong to their " +
                            "respective authors.",
                    )
                )
                LegalBody(
                    localized(
                        "Versión ${BuildConfig.VERSION_NAME}. El código se distribuye bajo la " +
                            "Licencia Pública General de GNU, versión 3.",
                        "Version ${BuildConfig.VERSION_NAME}. The source code is distributed under " +
                            "the GNU General Public License, version 3.",
                    )
                )
                val openError = localized(
                    "No se ha podido abrir el enlace",
                    "The link could not be opened",
                )
                TextButton(
                    onClick = {
                        if (!context.openExternal(SOURCE_URL)) SnackbarManager.show(openError)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(localized("Código fuente e incidencias", "Source code and issues"))
                }
                TextButton(
                    onClick = {
                        if (!context.openExternal(ATALAYA_URL)) SnackbarManager.show(openError)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("atalayasoftware.com")
                }
            }

            LegalSection(title = localized("Tus datos", "Your data")) {
                LegalBody(
                    localized(
                        "Tu biblioteca, listas, valoraciones, notas y preferencias se guardan en el " +
                            "almacenamiento privado de la aplicación. Visto no exige una cuenta y no " +
                            "incluye publicidad ni analítica propia.",
                        "Your library, lists, ratings, notes and preferences are stored in the " +
                            "app's private storage. Visto requires no account and includes neither " +
                            "advertising nor its own analytics.",
                    )
                )
                LegalBody(
                    localized(
                        "Las claves de API que introduzcas se guardan también en ese almacenamiento " +
                            "privado. Se envían únicamente al servicio correspondiente cuando haces " +
                            "una consulta.",
                        "API keys you enter are also kept in that private storage. They are sent " +
                            "only to the corresponding service when you make a request.",
                    )
                )
                LegalBody(
                    localized(
                        "Android puede incluir los datos privados de Visto en las copias de seguridad " +
                            "del sistema según la configuración de tu dispositivo. Las exportaciones " +
                            "y copias automáticas de Visto se escriben sólo en el archivo que eliges " +
                            "con el selector del sistema; si eliges un proveedor en la nube, ese " +
                            "proveedor recibirá el archivo.",
                        "Android may include Visto's private data in system backups depending on your " +
                            "device settings. Visto exports and scheduled backups are written only " +
                            "to the file you choose through the system picker; if you choose a cloud " +
                            "provider, that provider will receive the file.",
                    )
                )
            }

            LegalSection(title = localized("Conexiones a Internet", "Internet connections")) {
                LegalBody(
                    localized(
                        "Visto consulta TMDB para películas, series y personas; Open Library para " +
                            "libros; y Google Books únicamente si configuras una clave para completar " +
                            "sinopsis. Las búsquedas, identificadores solicitados y datos técnicos " +
                            "normales de una conexión web —como la dirección IP— llegan al proveedor " +
                            "consultado.",
                        "Visto queries TMDB for movies, shows and people; Open Library for books; " +
                            "and Google Books only when you configure a key to enrich synopses. " +
                            "Search terms, requested identifiers and ordinary web-connection data " +
                            "such as the IP address reach the provider being queried.",
                    )
                )
                LegalBody(
                    localized(
                        "Las imágenes de carteles, fondos, portadas y personas se descargan desde " +
                            "los servidores indicados por esos catálogos y pueden quedar en la caché " +
                            "local de imágenes.",
                        "Poster, backdrop, book-cover and people images are downloaded from the " +
                            "servers indicated by those catalogues and may remain in the local image " +
                            "cache.",
                    )
                )
            }

            LegalSection(title = localized("Avisos de contenido", "Content notices")) {
                Image(
                    painter = painterResource(R.drawable.tmdb_logo),
                    contentDescription = "TMDB",
                    modifier = Modifier.width(144.dp),
                )
                LegalBody(
                    localized(
                        "Este producto utiliza la API de TMDB, pero TMDB no lo respalda ni lo " +
                            "certifica. Los datos e imágenes de TMDB pertenecen a sus respectivos " +
                            "titulares.",
                        "This product uses the TMDB API but is not endorsed or certified by TMDB. " +
                            "TMDB data and images belong to their respective rights holders.",
                    )
                )
                LegalBody(
                    localized(
                        "Los metadatos bibliográficos y portadas proceden de Open Library, un " +
                            "proyecto de Internet Archive. Las sinopsis opcionales pueden proceder " +
                            "de Google Books. Visto no está afiliada con estos servicios y no es " +
                            "propietaria de su contenido.",
                        "Bibliographic metadata and covers come from Open Library, an Internet " +
                            "Archive project. Optional synopses may come from Google Books. Visto is " +
                            "not affiliated with these services and does not own their content.",
                    )
                )
                LegalBody(
                    localized(
                        "Logotipo oficial de TMDB diseñado por Travis Bell; copia obtenida de " +
                            "Wikimedia Commons bajo CC BY-SA 4.0, sin modificaciones.",
                        "Official TMDB logo designed by Travis Bell; unmodified copy obtained from " +
                            "Wikimedia Commons under CC BY-SA 4.0.",
                    )
                )
            }

            LegalSection(title = localized("Software de terceros", "Third-party software")) {
                LegalBody(
                    localized(
                        "AndroidX, Jetpack Compose, Material 3, Room, WorkManager, Hilt, Kotlin y " +
                            "coroutines se distribuyen bajo Apache License 2.0.",
                        "AndroidX, Jetpack Compose, Material 3, Room, WorkManager, Hilt, Kotlin and " +
                            "coroutines are distributed under the Apache License 2.0.",
                    )
                )
                LegalBody(
                    localized(
                        "Retrofit, OkHttp, Gson y Coil se distribuyen bajo Apache License 2.0. " +
                            "MaterialKolor se distribuye bajo licencia MIT.",
                        "Retrofit, OkHttp, Gson and Coil are distributed under the Apache License " +
                            "2.0. MaterialKolor is distributed under the MIT licence.",
                    )
                )
                LegalBody(
                    localized(
                        "La tipografía Figtree, de Erik D. Kennedy, se distribuye bajo SIL Open " +
                            "Font License 1.1.",
                        "The Figtree typeface by Erik D. Kennedy is distributed under the SIL Open " +
                            "Font License 1.1.",
                    )
                )
            }

            if (gpl.isNotBlank()) {
                LicenceSection(
                    title = localized("Licencia de Visto", "Visto licence"),
                    text = gpl,
                )
            }
            if (ofl.isNotBlank()) {
                LicenceSection(
                    title = localized("Licencia de Figtree", "Figtree licence"),
                    text = ofl,
                )
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
        }
    }
}

@Composable
private fun LegalSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        shape = RoundedCornerShape(ShapeRadius.ExtraLarge),
        color = MaterialTheme.colorScheme.surfaceBright,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun LegalBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun LicenceSection(title: String, text: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    LegalSection(title = title) {
        TextButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (expanded) localized("Ocultar texto completo", "Hide full text")
                else localized("Ver texto completo", "View full text")
            )
        }
        if (expanded) {
            Text(
                text = text.trim(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun Context.readAsset(path: String): String = runCatching {
    assets.open(path).bufferedReader().use { it.readText() }
}.getOrDefault("")

private fun Context.openExternal(url: String): Boolean = runCatching {
    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
}.isSuccess

private const val SOURCE_URL = "https://github.com/dwightsckrute/Visto"
private const val ATALAYA_URL = "https://atalayasoftware.com"
private const val GPL_FILE = "licenses/gpl-3.0.txt"
private const val OFL_FILE = "licenses/figtree-OFL.txt"
