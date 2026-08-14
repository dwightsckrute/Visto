package com.pranshulgg.watchmaster

import android.app.Application
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
    }
}
