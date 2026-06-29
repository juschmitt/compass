package de.juschmitt.compass.search

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages

class RunSearchAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val prompt = Messages.showInputDialog(
            project,
            "Enter search prompt:",
            "Run Compass Search",
            null
        )?.trim() ?: return

        if (prompt.isBlank()) return

        project.service<SearchService>().runSearch(prompt)

        val toolWindowManager = com.intellij.openapi.wm.ToolWindowManager.getInstance(project)
        toolWindowManager.getToolWindow("Compass")?.show()
    }
}
