package de.juschmitt.compass.rewrite

import kotlinx.serialization.Serializable

@Serializable
data class RewriteResponse(
    val replacementText: String
)
