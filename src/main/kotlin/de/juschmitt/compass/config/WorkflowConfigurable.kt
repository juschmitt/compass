package de.juschmitt.compass.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class WorkflowConfigurable(private val project: Project) : Configurable {

    private var panel: WorkflowConfigPanel? = null

    override fun getDisplayName() = "Bearing"

    override fun createComponent(): JComponent {
        val p = WorkflowConfigPanel(project)
        panel = p
        return p.component
    }

    override fun isModified(): Boolean = panel?.isModified() ?: false

    override fun apply() {
        panel?.apply()
    }

    override fun reset() {
        panel?.reset()
    }

    override fun disposeUIResources() {
        panel = null
    }
}
