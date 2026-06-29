package de.juschmitt.compass

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import de.juschmitt.compass.objective.ObjectiveService
import de.juschmitt.compass.rewrite.RewriteService
import de.juschmitt.compass.search.SearchService

class WorkflowPluginTest : BasePlatformTestCase() {

    fun testPluginLoads() {
        assertNotNull(project)
    }

    fun testCoreServicesAreResolvable() {
        assertNotNull(project.service<ObjectiveService>())
        assertNotNull(project.service<SearchService>())
        assertNotNull(project.service<RewriteService>())
    }
}
