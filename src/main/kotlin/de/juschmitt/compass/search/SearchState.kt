package de.juschmitt.compass.search

sealed interface SearchState {
    object Idle : SearchState
    data class Running(val prompt: String, val previousResults: List<SearchResult> = emptyList()) : SearchState
    data class Results(val prompt: String, val results: List<SearchResult>) : SearchState
    data class Error(val prompt: String, val message: String, val previousResults: List<SearchResult> = emptyList()) : SearchState
}
