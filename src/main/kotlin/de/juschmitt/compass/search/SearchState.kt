package de.juschmitt.compass.search

sealed interface SearchState {
    object Idle : SearchState
    data class Running(val previousResults: List<SearchResult> = emptyList()) : SearchState
    data class Results(val results: List<SearchResult>) : SearchState
    data class Error(val message: String, val previousResults: List<SearchResult> = emptyList()) : SearchState
}
