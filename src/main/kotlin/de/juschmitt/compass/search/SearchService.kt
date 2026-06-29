package de.juschmitt.compass.search

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import de.juschmitt.compass.bridge.BridgeClient
import de.juschmitt.compass.bridge.BridgeResult
import de.juschmitt.compass.bridge.extractJson
import de.juschmitt.compass.objective.ObjectiveService
import de.juschmitt.compass.prompt.PromptBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Service(Service.Level.PROJECT)
class SearchService(private val project: Project, private val cs: CoroutineScope) {

    private val mutex = Mutex()
    private var currentJob: Job? = null

    @Volatile
    private var lastResults: List<SearchResult> = emptyList()

    private val json = Json { ignoreUnknownKeys = true }

    fun runSearch(searchPrompt: String) {
        cs.launch {
            // Cancel the previous job before starting a new one
            mutex.withLock {
                currentJob?.cancel()
                currentJob = null
            }

            val previousResults = lastResults

            val job = cs.launch {
                withContext(Dispatchers.Main) {
                    project.messageBus.syncPublisher(SearchListener.TOPIC)
                        .searchStateChanged(SearchState.Running(searchPrompt, previousResults))
                }

                val objective = project.service<ObjectiveService>().objective
                val prompt = PromptBuilder.buildSearch(searchPrompt, objective)
                val bridgeClient = BridgeClient(project)
                val result = bridgeClient.execute(prompt)

                ensureActive()

                val isStillCurrent = mutex.withLock { currentJob == coroutineContext[Job] }
                if (!isStillCurrent) return@launch

                when (result) {
                    is BridgeResult.Success -> {
                        try {
                            val response = json.decodeFromString<SearchResponse>(extractJson(result.output))
                            lastResults = response.results
                            withContext(Dispatchers.Main) {
                                project.messageBus.syncPublisher(SearchListener.TOPIC)
                                    .searchStateChanged(SearchState.Results(searchPrompt, response.results))
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                project.messageBus.syncPublisher(SearchListener.TOPIC)
                                    .searchStateChanged(SearchState.Error(searchPrompt, "Invalid JSON: ${e.message}", previousResults))
                                showError("Search failed: Invalid response from bridge: ${e.message}")
                            }
                        }
                    }
                    is BridgeResult.Error -> {
                        withContext(Dispatchers.Main) {
                            project.messageBus.syncPublisher(SearchListener.TOPIC)
                                .searchStateChanged(SearchState.Error(searchPrompt, result.message, previousResults))
                            showError("Search failed: ${result.message}")
                        }
                    }
                    BridgeResult.Cancelled -> { /* silently dropped */ }
                }
            }

            mutex.withLock { currentJob = job }
            job.join()
            mutex.withLock { if (currentJob == job) currentJob = null }
        }
    }

    fun cancelCurrentSearch() {
        cs.launch {
            mutex.withLock {
                currentJob?.cancel()
                currentJob = null
            }
        }
    }

    private fun showError(message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Compass")
            .createNotification(message, NotificationType.ERROR)
            .notify(project)
    }
}
