package de.juschmitt.compass.config

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@State(
    name = "CompassProjectSettings",
    storages = [Storage("compass.xml")]
)
@Service(Service.Level.PROJECT)
class WorkflowProjectSettings(val project: Project) :
    SimplePersistentStateComponent<WorkflowProjectSettings.State>(State()) {

    class State : BaseState() {
        var overrideEnabled: Boolean by property(false)
        var bridgeCommand: String? by string("")
    }

    var overrideEnabled: Boolean
        get() = state.overrideEnabled
        set(value) { state.overrideEnabled = value }

    var bridgeCommand: String
        get() = state.bridgeCommand ?: ""
        set(value) { state.bridgeCommand = value }
}
