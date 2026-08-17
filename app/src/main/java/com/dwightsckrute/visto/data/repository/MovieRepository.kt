package com.dwightsckrute.visto.data.repository

import android.util.Log
import com.dwightsckrute.visto.core.network.MovieBundleDto
import com.dwightsckrute.visto.data.local.dao.MovieBundleDao
import com.dwightsckrute.visto.data.local.entity.MovieBundle
import com.dwightsckrute.visto.data.local.entity.toDomain
import com.dwightsckrute.visto.data.local.mapper.toDomain
import com.dwightsckrute.visto.data.local.mapper.toEntity
import com.dwightsckrute.visto.core.network.TmdbApi
import com.dwightsckrute.visto.core.ui.snackbar.SnackbarManager
import kotlin.math.log

class MovieRepository(
    private val api: TmdbApi,
    private val dao: MovieBundleDao
) {

    suspend fun getWholeMovieData(movieId: Long, forceFetch: Boolean = false): MovieBundle {

        if (!forceFetch) {
            dao.getById(movieId)?.let {
                return it.toDomain()
            }
        }

        val response = api.getWholeMovieData(movieId)
        val body = response.body() ?: throw Exception("Movie not found")

        val domain = body.toDomain()

        dao.insert(domain.toEntity())

        return domain
    }

    suspend fun deleteCachedMovie(id: Long) {
        dao.deleteById(id)
    }
}
