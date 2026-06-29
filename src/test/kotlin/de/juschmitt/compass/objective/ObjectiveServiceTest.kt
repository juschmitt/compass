package de.juschmitt.compass.objective

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ObjectiveServiceTest : BasePlatformTestCase() {

    private fun service(): ObjectiveService = project.service()

    fun test_setObjective_nonBlankValue_storesObjective() {
        val svc = service()
        svc.setObjective("my goal")
        assertEquals("my goal", svc.objective)
    }

    fun test_setObjective_blankString_normalisesToNull() {
        val svc = service()
        svc.setObjective("   ")
        assertNull(svc.objective)
    }

    fun test_setObjective_emptyString_normalisesToNull() {
        val svc = service()
        svc.setObjective("")
        assertNull(svc.objective)
    }

    fun test_setObjective_null_normalisesToNull() {
        val svc = service()
        svc.setObjective(null)
        assertNull(svc.objective)
    }

    fun test_setObjective_replaceExisting_storesNewValue() {
        val svc = service()
        svc.setObjective("first")
        svc.setObjective("second")
        assertEquals("second", svc.objective)
    }

    fun test_setObjective_clearByEmptyAfterSet_normalisesToNull() {
        val svc = service()
        svc.setObjective("goal")
        svc.setObjective("")
        assertNull(svc.objective)
    }

    fun test_setObjective_messageBusFires_listenerReceivesNewValue() {
        val svc = service()
        var received: String? = "UNSET"
        project.messageBus.connect(testRootDisposable).subscribe(
            ObjectiveListener.TOPIC,
            ObjectiveListener { newObjective -> received = newObjective }
        )

        svc.setObjective("my goal")

        assertEquals("my goal", received)
    }

    fun test_setObjective_messageBusFires_listenerReceivesNullWhenCleared() {
        val svc = service()
        svc.setObjective("initial")

        var received: String? = "UNSET"
        project.messageBus.connect(testRootDisposable).subscribe(
            ObjectiveListener.TOPIC,
            ObjectiveListener { newObjective -> received = newObjective }
        )

        svc.setObjective("")

        assertNull(received)
    }
}
