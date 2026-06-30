package de.juschmitt.compass.search

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.impl.EditorEmbeddedComponentManager
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.createLifetime
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.JButton
import javax.swing.JPanel

@Service(Service.Level.PROJECT)
class SearchAnnotationService(private val project: Project) : Disposable {

    private val searchResultState: MutableStateFlow<List<SearchResult>> = MutableStateFlow(emptyList())
    private val inlays: MutableList<Inlay<*>> = mutableListOf()
    private val connection = project.messageBus.connect(this)

    init {
        this.createLifetime().coroutineScope.launch {
            searchResultState.collect { results ->
                if (results.isEmpty()) {
                    clearAll()
                } else {
                    install(results)
                }
            }
        }
        connection.subscribe(SearchListener.TOPIC, SearchListener { state ->
            when (state) {
                is SearchState.Running -> searchResultState.tryEmit(emptyList())
                is SearchState.Results -> searchResultState.tryEmit(state.results)
                else -> Unit
            }
        })
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                val currentResults = searchResultState.value
                if (currentResults.isEmpty()) return

                val matchingResults = currentResults.filter { result ->
                    val expectedPath = "${project.basePath}/${result.relativeFilePath}"
                    file.path == expectedPath
                }

                if (matchingResults.isNotEmpty()) {
                    val editor = (source.getEditors(file).filterIsInstance<TextEditor>().firstOrNull()?.editor) ?: return
                    matchingResults.forEach { result ->
                        addNoteInlay(editor, result)
                    }
                }
            }
        })
    }

    fun clearAll() {
        searchResultState.value = emptyList()
        inlays.forEach { it.dispose() }
        inlays.clear()
    }

    fun install(results: List<SearchResult>) {
        clearAll()
        for (result in results) {
            val vFile = LocalFileSystem.getInstance()
                .findFileByPath("${project.basePath}/${result.relativeFilePath}") ?: continue
            addInlayToFile(vFile, result)
        }
    }

    private fun addInlayToFile(file: VirtualFile, result: SearchResult) {
        val editor = FileEditorManager.getInstance(project).getEditors(file).filterIsInstance<TextEditor>().firstOrNull()?.editor ?: return
        addNoteInlay(editor, result)
    }

    private fun addNoteInlay(editor: Editor, result: SearchResult) {
        if (editor !is EditorImpl) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Bearing")
                .createNotification("Bearing: annotation inlay is not supported in this editor type", NotificationType.ERROR)
                .notify(this.project)
            return
        }

        val doc = editor.document
        val lineIndex = (result.startLine - 1).coerceIn(0, doc.lineCount - 1)
        val offset = doc.getLineStartOffset(lineIndex)

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
