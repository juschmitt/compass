package de.juschmitt.compass.rewrite.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Graphics
import java.awt.Rectangle

private val FENCE_FG = JBColor(
    java.awt.Color(0x888888),
    java.awt.Color(0x777777)
)

class ThinkingFenceRenderer(
    private val editor: Editor,
    private val label: String
) : EditorCustomElementRenderer {

    override fun calcWidthInPixels(inlay: Inlay<*>): Int = editor.component.width

    override fun calcHeightInPixels(inlay: Inlay<*>): Int = editor.lineHeight

    override fun paint(
        inlay: Inlay<*>,
        g: Graphics,
        targetRegion: Rectangle,
        textAttributes: TextAttributes
    ) {
        val font = editor.colorsScheme
            .getFont(com.intellij.openapi.editor.colors.EditorFontType.PLAIN)
            .deriveFont(java.awt.Font.ITALIC)
        g.font = font
        g.color = FENCE_FG
        g.drawString(label, targetRegion.x + JBUI.scale(4), targetRegion.y + editor.lineHeight - JBUI.scale(4))
    }
}
