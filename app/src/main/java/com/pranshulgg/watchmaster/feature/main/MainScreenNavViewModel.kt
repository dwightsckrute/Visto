package com.pranshulgg.watchmaster.feature.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pranshulgg.watchmaster.core.utils.PreferencesHelper

class MainScreenNavViewModel : ViewModel() {

    var selectedDestination by mutableStateOf(
        MainDestination.fromPreference(PreferencesHelper.getString("default_tab"))
    )
        private set

    fun selectDestination(destination: MainDestination) {
        selectedDestination = destination
    }
}
