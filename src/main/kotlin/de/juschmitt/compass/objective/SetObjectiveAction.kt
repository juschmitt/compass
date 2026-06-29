package de.juschmitt.compass.objective

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages

class SetObjectiveAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val objectiveService = project.service<ObjectiveService>()
        val current = objectiveService.objective ?: ""

        val result = Messages.showInputDialog(
            project,
            "Enter objective (empty to clear):",
            "Set Current Objective",
            null,
            current,
            null
        ) ?: return // user cancelled

        objectiveService.setObjective(result)
    }
}
