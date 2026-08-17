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
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .crossfade(true)
            .build()

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
