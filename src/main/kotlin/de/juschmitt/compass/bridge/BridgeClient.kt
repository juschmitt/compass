package de.juschmitt.compass.bridge

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import de.juschmitt.compass.config.EffectiveConfig
import de.juschmitt.compass.config.WorkflowAppSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.nio.charset.StandardCharsets
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

class BridgeClient(private val project: Project) {

    suspend fun execute(prompt: String): BridgeResult {
        val command = EffectiveConfig.resolve(project)
            ?: return BridgeResult.Error("No bridge command configured")

        val tokens = BridgeCommandParser.parse(command)
        if (tokens.isEmpty()) return BridgeResult.Error("Bridge command is empty")

        val cmdLine = GeneralCommandLine(tokens)
            .withWorkDirectory(project.basePath)
            .withCharset(StandardCharsets.UTF_8)

        val handler = try {
            CapturingProcessHandler(cmdLine)
        } catch (e: Exception) {
            return BridgeResult.Error("Failed to start bridge: ${e.message}")
        }

        try {
            handler.processInput.use { stdin ->
                stdin.write(prompt.toByteArray(StandardCharsets.UTF_8))
                stdin.flush()
            }
        } catch (e: Exception) {
            handler.destroyProcess()
            return BridgeResult.Error("Failed to write to bridge stdin: ${e.message}")
        }

        val timeoutSeconds = service<WorkflowAppSettings>().bridgeTimeoutSeconds

        val runBridge: suspend () -> ProcessOutput? = {
            suspendCancellableCoroutine { cont ->
                cont.invokeOnCancellation { handler.destroyProcess() }
                Thread {
                    val output = try { handler.runProcess() } catch (_: Exception) { null }
                    if (cont.isActive) cont.resume(output)
                }.also { it.isDaemon = true; it.name = "compass-bridge" }.start()
            }
        }

        val processResult = if (timeoutSeconds > 0) {
            withTimeoutOrNull(timeoutSeconds.seconds) { runBridge() }
        } else {
            runBridge()
        }

        if (processResult == null) {
            handler.destroyProcess()
            return if (timeoutSeconds > 0) {
                BridgeResult.Error("Bridge command timed out (${timeoutSeconds}s)")
            } else {
                BridgeResult.Error("Bridge command failed unexpectedly")
            }
        }

        if (processResult.exitCode != 0) {
            return BridgeResult.Error(
                "Bridge exited with code ${processResult.exitCode}: ${processResult.stderr.trim()}",
                processResult.exitCode
            )
        }

        return BridgeResult.Success(processResult.stdout.trim())
    }
}

/**
 * Extracts a JSON object from an LLM response. Handles three cases:
 *   - Markdown code fences:  ```json\n{...}\n```  → content inside the fences
 *   - surrounding prose:     "Here is the result:\n{...}\nLet me know if..."  → first `{` … last `}`
 *   - plain JSON:            {...}  → returned as-is
 *
 * Fenced and unfenced paths are kept distinct: if fences are present the
 * content inside them is trusted directly; prose-stripping only applies when
 * there are no fences.
 */
internal fun extractJson(text: String): String {
    val trimmed = text.trim()

    if (trimmed.startsWith("```")) {
        val firstNewline = trimmed.indexOf('\n')
        val lastFence = trimmed.lastIndexOf("```")
        if (firstNewline != -1 && lastFence > firstNewline) {
            return trimmed.substring(firstNewline + 1, lastFence).trim()
        }
    }

    val start = trimmed.indexOf('{')
    val end = trimmed.lastIndexOf('}')
    if (start != -1 && end > start) {
        return trimmed.substring(start, end + 1)
    }

    return trimmed
}
