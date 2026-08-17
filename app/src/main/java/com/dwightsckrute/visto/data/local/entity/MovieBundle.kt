package com.dwightsckrute.visto.data.local.entity

import com.dwightsckrute.visto.data.CreditsDto
import com.dwightsckrute.visto.data.ImagesDto
import com.dwightsckrute.visto.data.MovieListDto
import com.dwightsckrute.visto.data.ReviewsDto
import com.dwightsckrute.visto.data.WatchProvidersDto
import com.dwightsckrute.visto.core.network.MovieBundleDto
import com.dwightsckrute.visto.data.MovieGenre

data class MovieBundle(
    val id: Long,
    val title: String,
    val overview: String,
    val runtime: Int?,
    val poster_path: String?,
    val backdrop_path: String?,
    val genres: List<MovieGenre>,
    val credits: CreditsDto,
    val images: ImagesDto,
    val watchProviders: WatchProvidersDto?,
    val similar: MovieListDto,
    val recommendations: MovieListDto,
    val reviews: ReviewsDto,
    val cachedAt: Long
)

// mapper extension
fun MovieBundleDto.toDomain(): MovieBundle = MovieBundle(
    id = id,
    title = title,
    overview = overview,
    runtime = runtime,
    poster_path = poster_path,
    backdrop_path = backdrop_path,
    genres = genres,
    credits = credits,
    images = images,
    watchProviders = watchProviders,
    similar = similar,
    recommendations = recommendations,
    reviews = reviews,
    cachedAt = System.currentTimeMillis()
)
