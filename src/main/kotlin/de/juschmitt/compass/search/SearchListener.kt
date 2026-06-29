package de.juschmitt.compass.search

import com.intellij.util.messages.Topic

fun interface SearchListener {
    fun searchStateChanged(state: SearchState)

    companion object {
        val TOPIC: Topic<SearchListener> = Topic.create("WorkflowSearch", SearchListener::class.java)
    }
}
