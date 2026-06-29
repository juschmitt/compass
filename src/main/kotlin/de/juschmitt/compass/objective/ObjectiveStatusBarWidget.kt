package de.juschmitt.compass.objective

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.TextPresentation
import com.intellij.util.Consumer
import java.awt.event.MouseEvent

class ObjectiveStatusBarWidget(private val project: Project) : StatusBarWidget, TextPresentation {

    private var statusBar: StatusBar? = null
    private var currentObjective: String? = null

    override fun ID() = "de.juschmitt.compass.objective"

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
        currentObjective = project.service<ObjectiveService>().objective
        project.messageBus.connect(this).subscribe(ObjectiveListener.TOPIC, ObjectiveListener { objective ->
            currentObjective = objective
            statusBar.updateWidget(ID())
        })
    }

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun getText(): String {
        val obj = currentObjective
        return if (obj.isNullOrBlank()) "⬡ No objective" else "⬡ $obj"
    }

    override fun getTooltipText(): String? {
        val obj = currentObjective
        return if (obj.isNullOrBlank()) "Compass: No objective set. Click to set."
        else "Compass objective: $obj"
    }

    override fun getAlignment() = 0f

    override fun getClickConsumer(): Consumer<MouseEvent> = Consumer {
        val objectiveService = project.service<ObjectiveService>()
        val current = objectiveService.objective ?: ""
        val result = com.intellij.openapi.ui.Messages.showInputDialog(
            project,
            "Enter objective (empty to clear):",
            "Set Current Objective",
            null,
            current,
            null
        ) ?: return@Consumer
        objectiveService.setObjective(result)
    }

    override fun dispose() {
        statusBar = null
    }
}
