package com.dwightsckrute.visto.data.repository

import com.dwightsckrute.visto.core.network.TmdbApi
import com.dwightsckrute.visto.feature.person.PersonEntity

class PersonRepository(
    private val api: TmdbApi
) {

    suspend fun fetchPersonData(personId: Long): PersonEntity? {
        val response = api.getPersonData(personId)

        val body = response.body() ?: return null

        return body
    }

}