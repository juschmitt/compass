package de.juschmitt.compass.config

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "CompassAppSettings",
    storages = [Storage("compass.xml")]
)
@Service(Service.Level.APP)
class WorkflowAppSettings : SimplePersistentStateComponent<WorkflowAppSettings.State>(State()) {
    class State : BaseState() {
        var bridgeCommand: String? by string("")
        var bridgeTimeoutSeconds: Int by property(120)
    }

    var bridgeCommand: String
        get() = state.bridgeCommand ?: ""
        set(value) { state.bridgeCommand = value }

    var bridgeTimeoutSeconds: Int
        get() = state.bridgeTimeoutSeconds
        set(value) { state.bridgeTimeoutSeconds = value }
}
