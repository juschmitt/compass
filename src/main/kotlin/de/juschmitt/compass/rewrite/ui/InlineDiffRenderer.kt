package de.juschmitt.compass.rewrite.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle

private val REMOVED_BG = JBColor(Color(0xFFE8E8E8.toInt(), true), Color(0xFF4B1818.toInt(), true))
private val ADDED_BG = JBColor(Color(0xFFE8FFE8.toInt(), true), Color(0xFF1A3A1A.toInt(), true))
private val REMOVED_FG = JBColor(Color(0xFFCC0000.toInt(), true), Color(0xFFFF8080.toInt(), true))
private val ADDED_FG = JBColor(Color(0xFF006600.toInt(), true), Color(0xFF80FF80.toInt(), true))

class InlineDiffRenderer(
    private val editor: Editor,
    private val originalLines: List<String>,
    private val replacementLines: List<String>
) : EditorCustomElementRenderer {

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        return editor.component.width
    }

    override fun calcHeightInPixels(inlay: Inlay<*>): Int {
        val lineHeight = editor.lineHeight
        return (originalLines.size + replacementLines.size) * lineHeight + JBUI.scale(8)
    }

    override fun paint(inlay: Inlay<*>, g: Graphics, targetRegion: Rectangle, textAttributes: TextAttributes) {
        val lineHeight = editor.lineHeight
        val font = editor.colorsScheme.getFont(com.intellij.openapi.editor.colors.EditorFontType.PLAIN)
        g.font = font

        var y = targetRegion.y + JBUI.scale(4)

        for (line in originalLines) {
            g.color = REMOVED_BG
            g.fillRect(targetRegion.x, y, targetRegion.width, lineHeight)
            g.color = REMOVED_FG
            g.drawString("- $line", targetRegion.x + JBUI.scale(4), y + lineHeight - JBUI.scale(4))
            y += lineHeight
        }

        for (line in replacementLines) {
            g.color = ADDED_BG
            g.fillRect(targetRegion.x, y, targetRegion.width, lineHeight)
            g.color = ADDED_FG
            g.drawString("+ $line", targetRegion.x + JBUI.scale(4), y + lineHeight - JBUI.scale(4))
            y += lineHeight
        }
    }
}
