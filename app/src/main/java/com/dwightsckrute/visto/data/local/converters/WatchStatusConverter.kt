package com.dwightsckrute.visto.data.local.converters

import androidx.room.TypeConverter
import com.dwightsckrute.visto.core.model.WatchStatus

class WatchStatusConverter {

    @TypeConverter
    fun fromStatus(status: WatchStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): WatchStatus =
        WatchStatus.valueOf(value)
}
