package com.dwightsckrute.visto.data.repository

import android.util.Log
import com.dwightsckrute.visto.core.model.Trending
import com.dwightsckrute.visto.core.network.TmdbApi
import com.dwightsckrute.visto.data.local.dao.TrendingDao
import com.dwightsckrute.visto.data.local.mapper.toDomain
import com.dwightsckrute.visto.data.local.mapper.toEntity
import jakarta.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class TrendingRepository @Inject constructor(
    private val api: TmdbApi,
    private val dao: TrendingDao
) {

    suspend fun fetchTrending(
        mediaType: String,
        page: Int,
        trendingType: String
    ): List<Trending> {


        dao.getTrending(trendingType).let {
            if (it.isNotEmpty()) {
                return it.toDomain()
            }
        }


        val response = api.getTrendingDay(mediaType, page)

        val trending = response.body() ?: return emptyList()

        val domain = trending.toDomain()

        dao.insertTrending(domain.toEntity())

        return domain

    }

    suspend fun clearTrending(trendingType: String) {
        dao.clearTrending(trendingType)
    }


}