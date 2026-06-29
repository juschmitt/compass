package de.juschmitt.compass.bridge

sealed interface BridgeResult {
    data class Success(val output: String) : BridgeResult
    data class Error(val message: String, val exitCode: Int? = null) : BridgeResult
    object Cancelled : BridgeResult
}
