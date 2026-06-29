package de.juschmitt.compass.rewrite

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.vfs.VirtualFile
import de.juschmitt.compass.rewrite.ui.ThinkingIndicator
import java.util.UUID

data class ActiveRewrite(
    val id: String = UUID.randomUUID().toString(),
    val editor: Editor,
    val virtualFile: VirtualFile,
    val rangeMarker: RangeMarker,
    val thinkingIndicator: ThinkingIndicator? = null
)
