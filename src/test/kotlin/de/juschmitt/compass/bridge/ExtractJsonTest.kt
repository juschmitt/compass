package de.juschmitt.compass.bridge

import org.junit.Assert.assertEquals
import org.junit.Test

class ExtractJsonTest {

    @Test
    fun test_extractJson_plainJson_returnedAsIs() {
        val input = """{"key":"value"}"""
        assertEquals("""{"key":"value"}""", extractJson(input))
    }

    @Test
    fun test_extractJson_jsonWithSurroundingWhitespace_trimmed() {
        val input = """  {"key":"value"}  """
        assertEquals("""{"key":"value"}""", extractJson(input))
    }

    @Test
    fun test_extractJson_fencedWithLanguageTag_returnsContentBetweenFences() {
        val input = "```json\n{\"key\":\"value\"}\n```"
        assertEquals("""{"key":"value"}""", extractJson(input))
    }

    @Test
    fun test_extractJson_fencedWithoutLanguageTag_returnsContentBetweenFences() {
        val input = "```\n{\"key\":\"value\"}\n```"
        assertEquals("""{"key":"value"}""", extractJson(input))
    }

    @Test
    fun test_extractJson_jsonWithTextBeforeBraces_returnsJsonOnly() {
        val input = """Here is the result: {"key":"value"}"""
        assertEquals("""{"key":"value"}""", extractJson(input))
    }

    @Test
    fun test_extractJson_jsonWithTextAfterBraces_returnsJsonOnly() {
        val input = """{"key":"value"} done"""
        assertEquals("""{"key":"value"}""", extractJson(input))
    }

    @Test
    fun test_extractJson_nestedJson_usesLastClosingBrace() {
        val input = """{"a":{"b":"c"}}"""
        assertEquals("""{"a":{"b":"c"}}""", extractJson(input))
    }

    @Test
    fun test_extractJson_emptyString_returnsEmptyString() {
        val input = ""
        assertEquals("", extractJson(input))
    }
}
