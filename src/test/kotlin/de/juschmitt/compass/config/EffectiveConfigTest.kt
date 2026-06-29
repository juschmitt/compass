package de.juschmitt.compass.config

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class EffectiveConfigTest : BasePlatformTestCase() {

    private fun appSettings(): WorkflowAppSettings = service()
    private fun projectSettings(): WorkflowProjectSettings = project.service()

    override fun setUp() {
        super.setUp()
        appSettings().bridgeCommand = ""
        projectSettings().bridgeCommand = ""
        projectSettings().overrideEnabled = false
    }

    fun test_resolve_globalCommandUsedWhenOverrideDisabled() {
        appSettings().bridgeCommand = "global-bridge"
        projectSettings().overrideEnabled = false

        assertEquals("global-bridge", EffectiveConfig.resolve(project))
    }

    fun test_resolve_projectOverrideTakesPrecedenceWhenEnabled() {
        appSettings().bridgeCommand = "global-bridge"
        projectSettings().bridgeCommand = "project-bridge"
        projectSettings().overrideEnabled = true

        assertEquals("project-bridge", EffectiveConfig.resolve(project))
    }

    fun test_resolve_returnsNullWhenBothBlank() {
        appSettings().bridgeCommand = ""
        projectSettings().bridgeCommand = ""
        projectSettings().overrideEnabled = false

        assertNull(EffectiveConfig.resolve(project))
    }

    fun test_resolve_projectOverrideIgnoredWhenDisabled_fallsBackToAppCommand() {
        appSettings().bridgeCommand = "global-bridge"
        projectSettings().bridgeCommand = "project-bridge"
        projectSettings().overrideEnabled = false

        assertEquals("global-bridge", EffectiveConfig.resolve(project))
    }

    fun test_resolve_projectOverrideIgnoredWhenBlank_fallsBackToAppCommand() {
        appSettings().bridgeCommand = "global-bridge"
        projectSettings().bridgeCommand = ""
        projectSettings().overrideEnabled = true

        assertEquals("global-bridge", EffectiveConfig.resolve(project))
    }

    fun test_resolve_appCommandBlankAndOverrideDisabled_returnsNull() {
        appSettings().bridgeCommand = ""
        projectSettings().overrideEnabled = false

        assertNull(EffectiveConfig.resolve(project))
    }
}
