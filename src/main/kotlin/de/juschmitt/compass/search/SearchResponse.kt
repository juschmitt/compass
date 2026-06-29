package de.juschmitt.compass.search

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val results: List<SearchResult>
)
