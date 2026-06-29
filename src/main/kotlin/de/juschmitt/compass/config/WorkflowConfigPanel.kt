package de.juschmitt.compass.config

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSeparator

class WorkflowConfigPanel(private val project: Project) {

    private val appSettings = service<WorkflowAppSettings>()
    private val projectSettings = project.service<WorkflowProjectSettings>()

    private val globalCommandField = JBTextField()
    private val timeoutField = JBTextField(6)
    private val projectOverrideCheckBox = JBCheckBox("Override global bridge command for this project")
    private val projectCommandField = JBTextField()

    val component: JComponent by lazy { buildPanel() }

    private fun buildPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = JBUI.insets(4)
        }

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0
        panel.add(JBLabel("<html><b>Global Default</b></html>"), gbc)

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.0
        panel.add(JBLabel("Bridge command:"), gbc)

        gbc.gridx = 1; gbc.weightx = 1.0
        panel.add(globalCommandField, gbc)

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0
        panel.add(JBLabel("<html><small>Full shell command string. Runs with project root as working directory.<br>Prompt is sent via stdin; plugin expects JSON on stdout.</small></html>"), gbc)

        gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0.0; gbc.gridx = 0
        panel.add(JBLabel("Timeout (seconds):"), gbc)

        gbc.gridx = 1; gbc.weightx = 1.0
        panel.add(timeoutField, gbc)

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 1.0
        panel.add(JBLabel("<html><small>0 = no timeout</small></html>"), gbc)

        gbc.gridy = 5
        panel.add(JSeparator(), gbc)

        gbc.gridy = 6
        panel.add(JBLabel("<html><b>Project Override</b></html>"), gbc)

        gbc.gridy = 7
        panel.add(projectOverrideCheckBox, gbc)

        gbc.gridy = 8; gbc.gridwidth = 1; gbc.weightx = 0.0; gbc.gridx = 0
        panel.add(JBLabel("Bridge command:"), gbc)

        gbc.gridx = 1; gbc.weightx = 1.0
        panel.add(projectCommandField, gbc)

        projectOverrideCheckBox.addChangeListener {
            projectCommandField.isEnabled = projectOverrideCheckBox.isSelected
        }

        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2; gbc.weighty = 1.0
        panel.add(JPanel(), gbc)

        reset()
        return panel
    }

    fun isModified(): Boolean {
        return globalCommandField.text != appSettings.bridgeCommand ||
            timeoutField.text.trim().toIntOrNull()?.coerceAtLeast(0) != appSettings.bridgeTimeoutSeconds ||
            projectOverrideCheckBox.isSelected != projectSettings.overrideEnabled ||
            projectCommandField.text != projectSettings.bridgeCommand
    }

    fun apply() {
        appSettings.bridgeCommand = globalCommandField.text.trim()
        appSettings.bridgeTimeoutSeconds = timeoutField.text.trim().toIntOrNull()?.coerceAtLeast(0) ?: 120
        projectSettings.overrideEnabled = projectOverrideCheckBox.isSelected
        projectSettings.bridgeCommand = projectCommandField.text.trim()
    }

    fun reset() {
        globalCommandField.text = appSettings.bridgeCommand
        timeoutField.text = appSettings.bridgeTimeoutSeconds.toString()
        projectOverrideCheckBox.isSelected = projectSettings.overrideEnabled
        projectCommandField.text = projectSettings.bridgeCommand
        projectCommandField.isEnabled = projectSettings.overrideEnabled
    }
}
