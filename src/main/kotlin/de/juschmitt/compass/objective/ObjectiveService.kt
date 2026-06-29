package de.juschmitt.compass.objective

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class ObjectiveService(private val project: Project) {

    @Volatile
    private var _objective: String? = null

    val objective: String? get() = _objective

    fun setObjective(text: String?) {
        _objective = text?.takeIf { it.isNotBlank() }
        project.messageBus.syncPublisher(ObjectiveListener.TOPIC).objectiveChanged(_objective)
    }
}
