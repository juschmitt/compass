package de.juschmitt.compass.rewrite

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.util.TextRange
import de.juschmitt.compass.rewrite.ui.InlinePromptInput

class SelectionRewriteAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null &&
            editor != null &&
            editor.selectionModel.hasSelection()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (!editor.selectionModel.hasSelection()) {
            com.intellij.notification.NotificationGroupManager.getInstance()
                .getNotificationGroup("Compass")
                .createNotification(
                    "Please select text before running Selection Rewrite",
                    com.intellij.notification.NotificationType.WARNING
                )
                .notify(project)
            return
        }

        val selectionStart = editor.selectionModel.selectionStart
        val selectionEnd = editor.selectionModel.selectionEnd
        val selectionText = editor.selectionModel.selectedText ?: return
        val selectionRange = TextRange(selectionStart, selectionEnd)

        InlinePromptInput.show(editor, selectionEnd) { promptText ->
            if (promptText.isBlank()) return@show

            val request = RewriteRequest(
                editor = editor,
                virtualFile = virtualFile,
                selectionRange = selectionRange,
                selectionText = selectionText,
                promptText = promptText
            )

            project.service<RewriteService>().startRewrite(request)
        }
    }
}
