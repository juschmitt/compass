package de.juschmitt.compass.rewrite

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile

data class RewriteRequest(
    val editor: Editor,
    val virtualFile: VirtualFile,
    val selectionRange: TextRange,
    val selectionText: String,
    val promptText: String
)
