package de.juschmitt.compass.rewrite

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import de.juschmitt.compass.bridge.BridgeClient
import de.juschmitt.compass.bridge.BridgeResult
import de.juschmitt.compass.bridge.extractJson
import de.juschmitt.compass.objective.ObjectiveService
import de.juschmitt.compass.prompt.PromptBuilder
import de.juschmitt.compass.rewrite.ui.InlineDiffPresenter
import de.juschmitt.compass.rewrite.ui.ThinkingIndicator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Service(Service.Level.PROJECT)
class RewriteService(private val project: Project, private val cs: CoroutineScope) {

    private val activeRewrites = mutableListOf<ActiveRewrite>()
    private val json = Json { ignoreUnknownKeys = true }

    fun hasOverlap(request: RewriteRequest): Boolean {
        ApplicationManager.getApplication().assertIsDispatchThread()
        return activeRewrites.any { active ->
            active.virtualFile == request.virtualFile &&
                active.rangeMarker.isValid &&
                rangesIntersect(
                    TextRange(active.rangeMarker.startOffset, active.rangeMarker.endOffset),
                    request.selectionRange
                )
        }
    }

    private fun rangesIntersect(a: TextRange, b: TextRange): Boolean =
        a.startOffset < b.endOffset && b.startOffset < a.endOffset

    private fun release(active: ActiveRewrite) {
        active.thinkingIndicator?.dispose()
        active.rangeMarker.dispose()
        activeRewrites.remove(active)
    }

    fun startRewrite(request: RewriteRequest) {
        ApplicationManager.getApplication().assertIsDispatchThread()

        if (request.editor !is EditorImpl) {
            showError("Rewrite is not supported in this editor type")
            return
        }

        if (request.selectionRange.isEmpty) {
            showError("Selection is empty — please select text before running Selection Rewrite")
            return
        }

        if (hasOverlap(request)) {
            showError("An overlapping rewrite is already running in this file region")
            return
        }

        val rangeMarker = request.editor.document.createRangeMarker(
            request.selectionRange.startOffset,
            request.selectionRange.endOffset
        ).apply {
            isGreedyToLeft = false
            isGreedyToRight = false
        }

        var cancelJob: (() -> Unit)? = null

        val thinkingIndicator = ThinkingIndicator(
            editor = request.editor,
            startOffset = rangeMarker.startOffset,
            endOffset = rangeMarker.endOffset,
            onCancel = { cancelJob?.invoke() }
        )

        val active = ActiveRewrite(
            editor = request.editor,
            virtualFile = request.virtualFile,
            rangeMarker = rangeMarker,
            thinkingIndicator = thinkingIndicator
        )
        activeRewrites.add(active)

        val job = cs.launch {
            try {
                val objective = project.service<ObjectiveService>().objective
                val filePath = request.virtualFile.path
                val relativePath = project.basePath?.let { base ->
                    filePath.removePrefix(base).trimStart('/')
                } ?: filePath

                val prompt = PromptBuilder.buildRewrite(
                    selectionText = request.selectionText,
                    filePath = relativePath,
                    userPrompt = request.promptText,
                    objective = objective
                )

                val bridgeClient = BridgeClient(project)
                val result = bridgeClient.execute(prompt)

                withContext(Dispatchers.Main) {
                    when (result) {
                        is BridgeResult.Success -> {
                            try {
                                val response = json.decodeFromString<RewriteResponse>(extractJson(result.output))
                                if (!rangeMarker.isValid) {
                                    release(active)
                                    showError("Rewrite range is no longer valid (file was changed too much)")
                                    return@withContext
                                }
                                InlineDiffPresenter.show(
                                    project = project,
                                    editor = request.editor,
                                    rangeMarker = rangeMarker,
                                    originalText = request.selectionText,
                                    replacementText = response.replacementText,
                                    onAccept = {
                                        applyReplacement(request, rangeMarker, response.replacementText)
                                        release(active)
                                    },
                                    onReject = {
                                        release(active)
                                    }
                                )
                            } catch (e: Exception) {
                                release(active)
                                showError("Rewrite failed: Invalid response from bridge: ${e.message}")
                            }
                        }
                        is BridgeResult.Error -> {
                            release(active)
                            showError("Rewrite failed: ${result.message}")
                        }
                        BridgeResult.Cancelled -> {
                            release(active)
                        }
                    }
                }
            } catch (e: CancellationException) {
                withContext(NonCancellable + Dispatchers.Main) { release(active) }
                throw e
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    release(active)
                    showError("Rewrite failed: ${e.message}")
                }
            }
        }
        cancelJob = { job.cancel() }
    }

    private fun applyReplacement(request: RewriteRequest, rangeMarker: com.intellij.openapi.editor.RangeMarker, replacement: String) {
        if (!rangeMarker.isValid) {
            showError("Cannot apply: the file region has changed")
            return
        }
        WriteCommandAction.runWriteCommandAction(project, "Bearing Rewrite", null, {
            request.editor.document.replaceString(
                rangeMarker.startOffset,
                rangeMarker.endOffset,
                replacement
            )
        })
    }

    private fun showError(message: String) {
        ApplicationManager.getApplication().invokeLater {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Bearing")
                .createNotification(message, NotificationType.ERROR)
                .notify(project)
        }
    }
}
