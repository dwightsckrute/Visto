package com.dwightsckrute.visto

import android.app.Application
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.util.Log
import com.dwightsckrute.visto.core.utils.PreferencesHelper
import com.dwightsckrute.visto.core.ui.localization.AppLanguage
import com.dwightsckrute.visto.feature.setting.MetadataSync
import dagger.hilt.android.HiltAndroidApp
import coil.ImageLoader
import coil.memory.MemoryCache
import coil.disk.DiskCache
import coil.ImageLoaderFactory

@HiltAndroidApp
class VistoApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        PreferencesHelper.init(this)
        AppLanguage.initialize(this)
        MetadataSync.ensureCurrentLanguage(this)
        logLauncherIconSupport()
    }

    /**
     * Las imágenes entran con un fundido en vez de aparecer de golpe.
     *
     * Sin esto, una ficha se dibujaba entera y el fondo se plantaba encima al terminar de
     * descargarse, que es lo que se percibía como un tirón al abrir. Coil no hace fundido por
     * defecto, así que hay que declararlo.
     */
    /**
     * El cargador de imágenes.
     *
     * Las cachés se declaran en vez de dejarse por defecto porque ahora hay dos catálogos con
     * costumbres distintas: TMDB sirve sus carátulas con cabeceras generosas y Open Library las
     * suyas con tres horas de validez. Con un disco propio y holgado, volver a una lista ya
     * vista no vuelve a pedir nada, que es donde más se nota.
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(IMAGE_CACHE_BYTES)
                    .build()
            }
            // Respeta la caducidad que manda el servidor; sin esto Coil revalida por su cuenta.
            .respectCacheHeaders(true)
            .build()

    private companion object {
        /** 250 MB de portadas. Una biblioteca de mil títulos no llega. */
        const val IMAGE_CACHE_BYTES = 250L * 1024 * 1024
    }

    private fun logLauncherIconSupport() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val icon = packageManager.getApplicationIcon(applicationInfo)
        val adaptiveIcon = icon as? AdaptiveIconDrawable
        Log.i(
            "VistoIcon",
            "adaptive=${adaptiveIcon != null}, monochrome=${adaptiveIcon?.monochrome != null}",
        )
    }
}
