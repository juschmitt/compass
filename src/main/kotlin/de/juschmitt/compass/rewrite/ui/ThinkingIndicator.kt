package de.juschmitt.compass.rewrite.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.impl.EditorEmbeddedComponentManager
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.JButton
import javax.swing.JPanel

class ThinkingIndicator(
    private val editor: Editor,
    startOffset: Int,
    endOffset: Int,
    onCancel: () -> Unit
) : Disposable {

    private val inlays: MutableList<com.intellij.openapi.editor.Inlay<*>> = mutableListOf()


    private val highlighter: RangeHighlighter = editor.markupModel.addRangeHighlighter(
        EditorColors.LIVE_TEMPLATE_ATTRIBUTES,
        startOffset,
        endOffset,
        HighlighterLayer.SELECTION - 1,
        HighlighterTargetArea.EXACT_RANGE
    )

    init {
        val doc = editor.document
        val endLine = doc.getLineNumber(endOffset)
        val normalizedEnd = normalizeOffset(endLine, doc, endOffset)

        if (editor is EditorImpl) {
            val startPanel = buildPanel(onCancel)
            val startProps = EditorEmbeddedComponentManager.Properties(
                EditorEmbeddedComponentManager.ResizePolicy.none(),
                null,
                false,
                true,
                10,
                startOffset
            )
            EditorEmbeddedComponentManager.getInstance()
                .addComponent(editor, startPanel, startProps)?.let { inlays.add(it) }

            val endPanel = buildPanel(onCancel)
            val endProps = EditorEmbeddedComponentManager.Properties(
                EditorEmbeddedComponentManager.ResizePolicy.none(),
                null,
                false,
                false,
                10,
                normalizedEnd
            )
            EditorEmbeddedComponentManager.getInstance()
                .addComponent(editor, endPanel, endProps)?.let { inlays.add(it) }
        }

    }

    override fun dispose() {
        inlays.forEach { it.dispose() }
        editor.markupModel.removeHighlighter(highlighter)
    }

    private fun buildPanel(onCancel: (() -> Unit)?): JPanel =
        JPanel(FlowLayout(FlowLayout.LEFT, 8, 2)).apply {
            add(JBLabel("✨ Rewriting...").apply {
                foreground = JBColor(0x8B5CF6, 0xA78BFA)
                font = font.deriveFont(Font.BOLD or Font.ITALIC)
            })
            onCancel?.let {
                add(JButton("✕").apply {
                    addActionListener { onCancel() }
                    putClientProperty("JButton.buttonType", "roundRect")
                    foreground = JBColor.GRAY
                    toolTipText = "Cancel rewrite"
                    isBorderPainted = false
                    isFocusPainted = false
                    isContentAreaFilled = false
                })
            }
        }
}

private fun normalizeOffset(endLine: Int, doc: Document, endOffset: Int): Int =
    if (endLine > 0 && doc.getLineStartOffset(endLine) == endOffset) {
        endOffset - 1
    } else {
        endOffset
    }
