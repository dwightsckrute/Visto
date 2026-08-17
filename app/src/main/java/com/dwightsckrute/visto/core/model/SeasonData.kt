package com.dwightsckrute.visto.core.model

data class SeasonData(
    val seasonNumber: Int,
    val name: String,
    val episodeCount: Int,
    val airDate: String?,
    val posterPath: String?,
    val vote_average: Double?,
)