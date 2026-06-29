package de.juschmitt.compass.objective

import com.intellij.util.messages.Topic

fun interface ObjectiveListener {
    fun objectiveChanged(objective: String?)

    companion object {
        val TOPIC: Topic<ObjectiveListener> = Topic.create("WorkflowObjective", ObjectiveListener::class.java)
    }
}
