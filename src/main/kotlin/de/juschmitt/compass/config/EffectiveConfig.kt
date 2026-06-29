package de.juschmitt.compass.config

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

object EffectiveConfig {
    fun resolve(project: Project): String? {
        val projectSettings = project.service<WorkflowProjectSettings>()
        if (projectSettings.overrideEnabled && projectSettings.bridgeCommand.isNotBlank()) {
            return projectSettings.bridgeCommand
        }
        val appSettings = service<WorkflowAppSettings>()
        return appSettings.bridgeCommand.takeIf { it.isNotBlank() }
    }
}
