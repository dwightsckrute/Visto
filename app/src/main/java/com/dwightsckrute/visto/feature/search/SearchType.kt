package com.dwightsckrute.visto.feature.search

enum class SearchType(val endpoint: String) {
    MULTI("search/multi"),
    MOVIE("search/movie"),
    TV("search/tv"),
    PERSON("search/person")
}
