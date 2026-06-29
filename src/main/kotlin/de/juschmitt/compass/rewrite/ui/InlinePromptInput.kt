package de.juschmitt.compass.rewrite.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.impl.EditorEmbeddedComponentManager
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel

object InlinePromptInput {

    fun show(editor: Editor, offset: Int, onSubmit: (String) -> Unit) {
        if (editor !is EditorImpl) {
            val result = com.intellij.openapi.ui.Messages.showInputDialog(
                editor.project,
                "Enter rewrite prompt:",
                "Selection Rewrite",
                null
            ) ?: return
            if (result.isBlank()) return
            onSubmit(result)
            return
        }

        var inlay: Inlay<*>? = null

        val panel = JPanel(BorderLayout(4, 0)).apply {
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, com.intellij.ui.JBColor.border()),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
            )

            val label = JBLabel("Rewrite: ")
            val textField = JBTextField().apply {
                preferredSize = Dimension(400, preferredSize.height)
            }
            val submitBtn = JButton("Rewrite")
            val cancelBtn = JButton("Cancel")

            add(label, BorderLayout.WEST)
            add(textField, BorderLayout.CENTER)

            val buttons = JPanel().apply {
                add(submitBtn)
                add(cancelBtn)
            }
            add(buttons, BorderLayout.EAST)

            fun submit() {
                val text = textField.text.trim()
                inlay?.dispose()
                if (text.isNotBlank()) onSubmit(text)
            }

            fun cancel() {
                inlay?.dispose()
            }

            textField.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    when (e.keyCode) {
                        KeyEvent.VK_ENTER -> submit()
                        KeyEvent.VK_ESCAPE -> cancel()
                    }
                }
            })
            submitBtn.addActionListener { submit() }
            cancelBtn.addActionListener { cancel() }

            ApplicationManager.getApplication().invokeLater {
                textField.requestFocusInWindow()
            }
        }

        val properties = EditorEmbeddedComponentManager.Properties(
            EditorEmbeddedComponentManager.ResizePolicy.none(),
            null,
            true,
            true,
            0,
            offset
        )

        inlay = EditorEmbeddedComponentManager.getInstance()
            .addComponent(editor, panel, properties)
    }
}
