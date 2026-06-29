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
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.UIManager

class SearchToolWindowPanel(private val project: Project, parentDisposable: Disposable) :
    JPanel(BorderLayout()), Disposable {

    private val objectiveLabel = JBLabel("No objective set").apply {
        font = font.deriveFont(Font.ITALIC)
        foreground = JBColor.GRAY
        border = BorderFactory.createEmptyBorder(4, 8, 4, 8)
    }

    private val statusLabel = JBLabel("").apply {
        border = BorderFactory.createEmptyBorder(2, 8, 2, 8)
        foreground = JBColor(0xC07000, 0xFFAA00)
    }

    private val cancelButton = JButton("Cancel").apply {
        addActionListener {
            project.service<SearchService>().cancelCurrentSearch()
        }
    }

    private val bannerPanel = JPanel(BorderLayout()).apply {
        border = BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border())
        add(statusLabel, BorderLayout.CENTER)
        add(cancelButton, BorderLayout.EAST)
        isVisible = false
    }

    private val listModel = DefaultListModel<SearchResult>()
    private val resultsList = JBList(listModel).apply {
        cellRenderer = SearchResultCellRenderer()
        selectionMode = javax.swing.ListSelectionModel.SINGLE_SELECTION
    }

    private val promptFooter = JTextField("").apply {
        isEditable = false
        background = UIManager.getColor("Panel.background")
        border = BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border())
        foreground = JBColor.GRAY
        isVisible = false
    }

    private var lastPrompt: String = ""

    private val messageBusConnection = project.messageBus.connect(this)

    init {
        val headerPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border())
            add(JBLabel("Objective: ").apply {
                border = BorderFactory.createEmptyBorder(4, 8, 4, 0)
            }, BorderLayout.WEST)
            add(objectiveLabel, BorderLayout.CENTER)
        }
        add(headerPanel, BorderLayout.NORTH)

        val centerPanel = JPanel(BorderLayout()).apply {
            add(bannerPanel, BorderLayout.NORTH)
            add(JBScrollPane(resultsList), BorderLayout.CENTER)
        }
        add(centerPanel, BorderLayout.CENTER)
        add(promptFooter, BorderLayout.SOUTH)

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
                bannerPanel.isVisible = false
            }
            is SearchState.Running -> {
                lastPrompt = state.prompt
                statusLabel.text = "Searching…"
                statusLabel.foreground = JBColor(0xC07000, 0xFFAA00)
                bannerPanel.isVisible = true
                cancelButton.isVisible = true
                promptFooter.text = lastPrompt
                promptFooter.isVisible = true
            }
            is SearchState.Results -> {
                lastPrompt = state.prompt
                bannerPanel.isVisible = false
                listModel.clear()
                state.results.forEach { listModel.addElement(it) }
                promptFooter.text = lastPrompt
                promptFooter.isVisible = true
            }
            is SearchState.Error -> {
                lastPrompt = state.prompt
                statusLabel.text = "Error: ${state.message}"
                statusLabel.foreground = JBColor.RED
                bannerPanel.isVisible = true
                cancelButton.isVisible = false
                if (state.previousResults.isEmpty()) {
                    listModel.clear()
                }
                promptFooter.text = lastPrompt
                promptFooter.isVisible = true
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
