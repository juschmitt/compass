package de.juschmitt.compass.search

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.impl.EditorEmbeddedComponentManager
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import de.juschmitt.compass.search.ui.SearchNoteRenderer
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.JButton
import javax.swing.JPanel

@Service(Service.Level.PROJECT)
class SearchAnnotationService(private val project: Project) : Disposable {

    // EDT-only — SearchService always publishes on Dispatchers.Main
    private val inlays: MutableList<Inlay<*>> = mutableListOf()
    private val connection = project.messageBus.connect(this)

    init {
        connection.subscribe(SearchListener.TOPIC, SearchListener { state ->
            when (state) {
                is SearchState.Running -> clearAll()
                is SearchState.Results -> install(state.results)
                else -> Unit
            }
        })
    }

    fun clearAll() {
        inlays.forEach { it.dispose() }
        inlays.clear()
    }

    fun install(results: List<SearchResult>) {
        clearAll()
        val fem = FileEditorManager.getInstance(project)
        for (result in results) {
            val vFile = LocalFileSystem.getInstance()
                .findFileByPath("${project.basePath}/${result.relativeFilePath}") ?: continue
            val editor = fem.getEditors(vFile)
                .filterIsInstance<TextEditor>()
                .firstOrNull()?.editor ?: continue
            addNoteInlay(editor, result)
        }
    }

    private fun addNoteInlay(editor: Editor, result: SearchResult) {
        val doc = editor.document
        val lineIndex = (result.startLine - 1).coerceIn(0, doc.lineCount - 1)
        val offset = doc.getLineStartOffset(lineIndex)
        var added = false

        if (editor is EditorImpl) {
            var selfInlay: Inlay<*>? = null
            val panel = buildNotePanel(result.note) {
                selfInlay?.dispose()
                inlays.remove(selfInlay)
            }
            val props = EditorEmbeddedComponentManager.Properties(
                EditorEmbeddedComponentManager.ResizePolicy.none(),
                null,
                false,
                true,
                10,
                offset
            )
            EditorEmbeddedComponentManager.getInstance()
                .addComponent(editor, panel, props)?.let {
                    selfInlay = it
                    inlays.add(it)
                    added = true
                }
        }

        if (!added) {
            var selfInlay: Inlay<*>? = null
            val renderer = SearchNoteRenderer(editor, result.note) {
                selfInlay?.dispose()
                inlays.remove(selfInlay)
            }
            editor.inlayModel.addBlockElement(offset, false, true, 10, renderer)?.let {
                selfInlay = it
                inlays.add(it)
            }
        }
    }

    private fun buildNotePanel(note: String, onClear: () -> Unit): JPanel =
        JPanel(FlowLayout(FlowLayout.LEFT, 8, 2)).apply {
            isOpaque = false
            add(JBLabel("◈  $note").apply {
                foreground = JBColor(0x6B7280, 0x9CA3AF)
                font = font.deriveFont(Font.ITALIC)
            })
            add(JButton("Clear ×").apply {
                addActionListener { onClear() }
                putClientProperty("JButton.buttonType", "roundRect")
                foreground = JBColor.GRAY
                toolTipText = "Clear this annotation"
                isBorderPainted = false
                isFocusPainted = false
                isContentAreaFilled = false
                font = font.deriveFont(font.size2D - 1f)
            })
        }

    override fun dispose() {
        clearAll()
    }
}
