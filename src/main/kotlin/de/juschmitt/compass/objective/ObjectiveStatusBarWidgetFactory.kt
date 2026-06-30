package de.juschmitt.compass.objective

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class ObjectiveStatusBarWidgetFactory : StatusBarWidgetFactory {

    override fun getId() = "de.juschmitt.compass.objective"

    override fun getDisplayName() = "Bearing Objective"

    override fun createWidget(project: Project): StatusBarWidget =
        ObjectiveStatusBarWidget(project)

    override fun isAvailable(project: Project) = true

    override fun canBeEnabledOn(statusBar: com.intellij.openapi.wm.StatusBar) = true
}
