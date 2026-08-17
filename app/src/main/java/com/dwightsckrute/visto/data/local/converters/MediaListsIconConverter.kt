package com.dwightsckrute.visto.data.local.converters

import androidx.room.TypeConverter
import com.dwightsckrute.visto.core.model.MediaListsIcons


class MediaListsIconConverter {

    @TypeConverter
    fun fromListsIcons(listsIcons: MediaListsIcons): String = listsIcons.name

    @TypeConverter
    fun toListsIcons(value: String): MediaListsIcons =
        MediaListsIcons.valueOf(value)
}
