package com.pranshulgg.watchmaster

import android.app.Application
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.util.Log
import com.pranshulgg.watchmaster.core.utils.PreferencesHelper
import com.pranshulgg.watchmaster.core.ui.localization.AppLanguage
import com.pranshulgg.watchmaster.feature.setting.MetadataSync
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WatchMasterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferencesHelper.init(this)
        AppLanguage.initialize(this)
        MetadataSync.ensureCurrentLanguage(this)
        logLauncherIconSupport()
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
