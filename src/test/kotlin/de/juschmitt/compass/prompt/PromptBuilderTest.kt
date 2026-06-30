package de.juschmitt.compass.prompt

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

    @Test
    fun test_buildSearch_withObjective_containsSearchPromptAndObjectiveText() {
        val result = PromptBuilder.buildSearch(
            searchPrompt = "find all usages of CoroutineScope",
            objective = "Refactor coroutine handling"
        )
        assertTrue(result.contains("find all usages of CoroutineScope"))
        assertTrue(result.contains("Refactor coroutine handling"))
    }

    @Test
    fun test_buildSearch_withoutObjective_containsSearchPromptButNoObjectiveSection() {
        val result = PromptBuilder.buildSearch(
            searchPrompt = "find all usages of CoroutineScope",
            objective = null
        )
        assertTrue(result.contains("find all usages of CoroutineScope"))
        assertFalse(result.contains("Current developer objective"))
    }

    @Test
    fun test_buildSearch_withBlankObjective_treatedSameAsNoObjective() {
        val result = PromptBuilder.buildSearch(
            searchPrompt = "find something",
            objective = "  "
        )
        assertFalse(result.contains("Current developer objective"))
    }

    @Test
    fun test_buildSearch_schemaFieldsPresent() {
        val result = PromptBuilder.buildSearch(searchPrompt = "find something", objective = null)
        assertTrue(result.contains("relativeFilePath"))
        assertTrue(result.contains("startLine"))
        assertTrue(result.contains("endLine"))
        assertTrue(result.contains("note"))
    }

    @Test
    fun test_buildSearch_containsToolUseHint() {
        val result = PromptBuilder.buildSearch(searchPrompt = "find something", objective = null)
        assertTrue(result.contains("Use your available tools"))
    }

    @Test
    fun test_buildSearch_containsResultCountGuidance() {
        val result = PromptBuilder.buildSearch(searchPrompt = "find something", objective = null)
        assertTrue(result.contains("up to 10"))
    }

    @Test
    fun test_buildRewrite_selectionTextAppearsInResult() {
        val result = PromptBuilder.buildRewrite(
            selectionText = "val x = 1",
            filePath = "src/Foo.kt",
            userPrompt = "rename x to y",
            objective = null
        )
        assertTrue(result.contains("val x = 1"))
    }

    @Test
    fun test_buildRewrite_filePathAppearsInResult() {
        val result = PromptBuilder.buildRewrite(
            selectionText = "val x = 1",
            filePath = "src/Foo.kt",
            userPrompt = "rename x to y",
            objective = null
        )
        assertTrue(result.contains("src/Foo.kt"))
    }

    @Test
    fun test_buildRewrite_userPromptAppearsInResult() {
        val result = PromptBuilder.buildRewrite(
            selectionText = "val x = 1",
            filePath = "src/Foo.kt",
            userPrompt = "rename x to y",
            objective = null
        )
        assertTrue(result.contains("rename x to y"))
    }

    @Test
    fun test_buildRewrite_withObjective_containsObjectiveText() {
        val result = PromptBuilder.buildRewrite(
            selectionText = "val x = 1",
            filePath = "src/Foo.kt",
            userPrompt = "rename x to y",
            objective = "Improve naming conventions"
        )
        assertTrue(result.contains("Improve naming conventions"))
    }

    @Test
    fun test_buildRewrite_withoutObjective_doesNotContainObjectiveSection() {
        val result = PromptBuilder.buildRewrite(
            selectionText = "val x = 1",
            filePath = "src/Foo.kt",
            userPrompt = "rename x to y",
            objective = null
        )
        assertFalse(result.contains("Current developer objective"))
    }

    @Test
    fun test_buildRewrite_schemaFieldReplacementTextPresent() {
        val result = PromptBuilder.buildRewrite(
            selectionText = "val x = 1",
            filePath = "src/Foo.kt",
            userPrompt = "rename x to y",
            objective = null
        )
        assertTrue(result.contains("replacementText"))
    }

    @Test
    fun test_buildRewrite_containsFileReadInstruction() {
        val result = PromptBuilder.buildRewrite(
            selectionText = "val x = 1",
            filePath = "src/Foo.kt",
            userPrompt = "rename x to y",
            objective = null
        )
        assertTrue(result.contains("Read the full file"))
    }

    @Test
    fun test_buildRewrite_doesNotContainRedundantSelectionAsContextLine() {
        val result = PromptBuilder.buildRewrite(
            selectionText = "val x = 1",
            filePath = "src/Foo.kt",
            userPrompt = "rename x to y",
            objective = null
        )
        assertFalse(result.contains("Take the provided code selection as context"))
    }
}
