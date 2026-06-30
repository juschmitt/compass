package de.juschmitt.compass.rewrite.ui

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.editor.impl.EditorEmbeddedComponentManager
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel

object InlineDiffPresenter {

    fun show(
        project: Project,
        editor: com.intellij.openapi.editor.Editor,
        rangeMarker: RangeMarker,
        originalText: String,
        replacementText: String,
        onAccept: () -> Unit,
        onReject: () -> Unit
    ) {
        if (!rangeMarker.isValid) {
            onReject()
            return
        }

        val originalLines = originalText.lines()
        val replacementLines = replacementText.lines()
        val insertOffset = rangeMarker.endOffset

        if (editor !is EditorImpl) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Bearing")
                .createNotification("Bearing: could not display rewrite diff in this editor", NotificationType.ERROR)
                .notify(project)
            onReject()
            return
        }

        var diffInlay: com.intellij.openapi.editor.Inlay<*>? = null
        var buttonsInlay: com.intellij.openapi.editor.Inlay<*>? = null

        fun disposeInlays() {
            diffInlay?.dispose()
            buttonsInlay?.dispose()
            diffInlay = null
            buttonsInlay = null
        }

        val renderer = InlineDiffRenderer(editor, originalLines, replacementLines)
        diffInlay = editor.inlayModel.addBlockElement(
            insertOffset,
            true,
            false,
            1,
            renderer
        )

        if (diffInlay == null) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Bearing")
                .createNotification("Bearing: could not display rewrite diff in this editor", NotificationType.ERROR)
                .notify(project)
            onReject()
            return
        }

        val buttonsPanel = buildButtonsPanel(
            onAccept = {
                disposeInlays()
                onAccept()
            },
            onReject = {
                disposeInlays()
                onReject()
            }
        )

        val props = EditorEmbeddedComponentManager.Properties(
            EditorEmbeddedComponentManager.ResizePolicy.none(),
            null,
            true,
            false,
            2,
            insertOffset
        )

        buttonsInlay = EditorEmbeddedComponentManager.getInstance()
            .addComponent(editor, buttonsPanel, props)

        if (buttonsInlay == null) {
            diffInlay?.dispose()
            diffInlay = null
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Bearing")
                .createNotification("Bearing: could not display rewrite diff in this editor", NotificationType.ERROR)
                .notify(project)
            onReject()
        }
    }

    private fun buildButtonsPanel(onAccept: () -> Unit, onReject: () -> Unit): JPanel =
        JPanel(FlowLayout(FlowLayout.LEFT, 8, 4)).apply {
            border = BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border())
            val label = JBLabel("Rewrite diff — review and accept or reject:")
            val acceptBtn = JButton("✓ Accept").apply {
                foreground = JBColor(java.awt.Color(0, 128, 0), java.awt.Color(0x80FF80))
            }
            val rejectBtn = JButton("✗ Reject").apply {
                foreground = JBColor(java.awt.Color(180, 0, 0), java.awt.Color(0xFF8080))
            }
            add(label)
            add(acceptBtn)
            add(rejectBtn)
            acceptBtn.addActionListener { onAccept() }
            rejectBtn.addActionListener { onReject() }
        }

}
