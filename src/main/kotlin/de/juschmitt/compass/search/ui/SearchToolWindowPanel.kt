package de.juschmitt.compass.search.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import de.juschmitt.compass.objective.ObjectiveListener
import de.juschmitt.compass.objective.ObjectiveService
import de.juschmitt.compass.search.SearchListener
import de.juschmitt.compass.search.SearchResult
import de.juschmitt.compass.search.SearchService
import de.juschmitt.compass.search.SearchState
import java.awt.BorderLayout
import java.awt.Font
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JPanel

class SearchToolWindowPanel(private val project: Project, parentDisposable: Disposable) :
    JPanel(BorderLayout()), Disposable {

    private val objectiveLabel = JBLabel("No objective set").apply {
        font = font.deriveFont(Font.ITALIC)
        foreground = JBColor.GRAY
        border = javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
    }

    private val statusLabel = JBLabel("").apply {
        border = javax.swing.BorderFactory.createEmptyBorder(2, 8, 2, 8)
        foreground = JBColor.GRAY
    }

    private val cancelButton = JButton("Cancel").apply {
        isVisible = false
        addActionListener {
            project.service<SearchService>().cancelCurrentSearch()
        }
    }

    private val listModel = DefaultListModel<SearchResult>()
    private val resultsList = JBList(listModel).apply {
        cellRenderer = SearchResultCellRenderer()
        selectionMode = javax.swing.ListSelectionModel.SINGLE_SELECTION
    }

    private val messageBusConnection = project.messageBus.connect(this)

    init {
        val headerPanel = JPanel(BorderLayout()).apply {
            border = javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border())
            add(JBLabel("Objective: ").apply {
                border = javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 0)
            }, BorderLayout.WEST)
            add(objectiveLabel, BorderLayout.CENTER)
        }
        add(headerPanel, BorderLayout.NORTH)
        add(JBScrollPane(resultsList), BorderLayout.CENTER)
        val statusPanel = JPanel(BorderLayout()).apply {
            add(statusLabel, BorderLayout.CENTER)
            add(cancelButton, BorderLayout.EAST)
        }
        add(statusPanel, BorderLayout.SOUTH)

        val currentObjective = project.service<ObjectiveService>().objective
        updateObjectiveLabel(currentObjective)

        messageBusConnection.subscribe(ObjectiveListener.TOPIC, ObjectiveListener { objective ->
            updateObjectiveLabel(objective)
        })

        messageBusConnection.subscribe(SearchListener.TOPIC, SearchListener { state ->
            updateForState(state)
        })

        resultsList.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) navigateToSelected()
            }
        })

        resultsList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) navigateToSelected()
            }
        })

        com.intellij.openapi.util.Disposer.register(parentDisposable, this)
    }

    override fun dispose() {
        messageBusConnection.disconnect()
    }

    private fun updateObjectiveLabel(objective: String?) {
        if (objective.isNullOrBlank()) {
            objectiveLabel.text = "No objective set"
            objectiveLabel.foreground = JBColor.GRAY
            objectiveLabel.font = objectiveLabel.font.deriveFont(Font.ITALIC)
        } else {
            objectiveLabel.text = objective
            objectiveLabel.foreground = JBColor.foreground()
            objectiveLabel.font = objectiveLabel.font.deriveFont(Font.PLAIN)
        }
    }

    private fun updateForState(state: SearchState) {
        when (state) {
            is SearchState.Idle -> {
                statusLabel.text = ""
                cancelButton.isVisible = false
            }
            is SearchState.Running -> {
                statusLabel.text = "Searching…"
                cancelButton.isVisible = true
            }
            is SearchState.Results -> {
                statusLabel.text = "${state.results.size} result(s)"
                cancelButton.isVisible = false
                listModel.clear()
                state.results.forEach { listModel.addElement(it) }
            }
            is SearchState.Error -> {
                statusLabel.text = "Error: ${state.message}"
                cancelButton.isVisible = false
                if (state.previousResults.isEmpty()) {
                    listModel.clear()
                }
            }
        }
    }

    private fun navigateToSelected() {
        val result = resultsList.selectedValue ?: return
        val vFile = LocalFileSystem.getInstance()
            .findFileByPath("${project.basePath}/${result.relativeFilePath}")
            ?: return
        OpenFileDescriptor(project, vFile, result.startLine - 1, 0).navigate(true)
    }
}
