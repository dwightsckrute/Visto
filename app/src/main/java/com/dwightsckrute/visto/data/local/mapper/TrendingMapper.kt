package com.dwightsckrute.visto.data.local.mapper

import com.dwightsckrute.visto.core.model.Trending
import com.dwightsckrute.visto.core.network.TrendingDayDto
import com.dwightsckrute.visto.data.local.entity.TrendingEntity
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun TrendingDayDto.toDomain(): List<Trending> =
    List(results.size) {
        Trending(
            id = results[it].id,
            title = results[it].title,
            name = results[it].name,
            backdropPath = results[it].backdropPath,
            posterPath = results[it].posterPath,
            mediaType = results[it].mediaType,
            releaseDate = results[it].releaseDate,
            firstAirDate = results[it].firstAirDate,
            avgRating = results[it].avgRating,
            overview = results[it].overview,
            genreIds = results[it].genreIds,
            trendingType = "day"
        )
    }


@OptIn(ExperimentalUuidApi::class)
fun List<Trending>.toEntity(): List<TrendingEntity> =
    List(this.size) {
        TrendingEntity(
            mainUuid = Uuid.random().toString(),
            id = get(it).id,
            title = get(it).title,
            name = get(it).name,
            backdropPath = get(it).backdropPath,
            posterPath = get(it).posterPath,
            mediaType = get(it).mediaType,
            releaseDate = get(it).releaseDate,
            firstAirDate = get(it).firstAirDate,
            avgRating = get(it).avgRating,
            overview = get(it).overview,
            genreIds = get(it).genreIds,
            cachedAt = System.currentTimeMillis(),
            trendingType = get(it).trendingType
        )
    }


@OptIn(ExperimentalUuidApi::class)
fun List<TrendingEntity>.toDomain(): List<Trending> =
    List(this.size) {
        Trending(
            id = get(it).id,
            title = get(it).title,
            name = get(it).name,
            backdropPath = get(it).backdropPath,
            posterPath = get(it).posterPath,
            mediaType = get(it).mediaType,
            releaseDate = get(it).releaseDate,
            firstAirDate = get(it).firstAirDate,
            avgRating = get(it).avgRating,
            overview = get(it).overview,
            genreIds = get(it).genreIds,
            trendingType = get(it).trendingType
        )
    }
