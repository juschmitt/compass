package de.juschmitt.compass.search

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val relativeFilePath: String,
    val startLine: Int,
    val endLine: Int,
    val note: String
)
