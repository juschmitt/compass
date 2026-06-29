package de.juschmitt.compass.search.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Graphics
import java.awt.Rectangle

private val NOTE_FG = JBColor(
    java.awt.Color(0x6B7280),
    java.awt.Color(0x9CA3AF)
)

class SearchNoteRenderer(
    private val editor: Editor,
    private val note: String,
    private val onClear: () -> Unit
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
        g.color = NOTE_FG
        val label = "◈  $note"
        g.drawString(label, targetRegion.x + JBUI.scale(4), targetRegion.y + editor.lineHeight - JBUI.scale(4))
    }
}
