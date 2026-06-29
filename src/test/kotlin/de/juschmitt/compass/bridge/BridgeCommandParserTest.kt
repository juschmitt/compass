package de.juschmitt.compass.bridge

import org.junit.Assert.assertEquals
import org.junit.Test

class BridgeCommandParserTest {

    @Test
    fun test_parse_simpleTwoTokenCommand_returnsTwoTokens() {
        val result = BridgeCommandParser.parse("claude --print")
        assertEquals(listOf("claude", "--print"), result)
    }

    @Test
    fun test_parse_singleQuotedArgumentWithSpaces_returnsTokenWithSpaces() {
        val result = BridgeCommandParser.parse("claude --arg 'with spaces'")
        assertEquals(listOf("claude", "--arg", "with spaces"), result)
    }

    @Test
    fun test_parse_doubleQuotedArgumentWithSpaces_returnsTokenWithSpaces() {
        val result = BridgeCommandParser.parse("claude --model \"claude-3-5\"")
        assertEquals(listOf("claude", "--model", "claude-3-5"), result)
    }

    @Test
    fun test_parse_emptyString_returnsEmptyList() {
        val result = BridgeCommandParser.parse("")
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun test_parse_blankStringOnlySpaces_returnsEmptyList() {
        val result = BridgeCommandParser.parse("   ")
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun test_parse_multipleConsecutiveSpaces_returnsDeduplicatedTokens() {
        val result = BridgeCommandParser.parse("claude  --print")
        assertEquals(listOf("claude", "--print"), result)
    }

    @Test
    fun test_parse_singleTokenNoSpaces_returnsSingleElementList() {
        val result = BridgeCommandParser.parse("claude")
        assertEquals(listOf("claude"), result)
    }

    @Test
    fun test_parse_mixedSingleAndDoubleQuotes_returnsAllTokensCorrectly() {
        val result = BridgeCommandParser.parse("cmd 'a b' \"c d\"")
        assertEquals(listOf("cmd", "a b", "c d"), result)
    }

    @Test
    fun test_parse_unclosedSingleQuote_doesNotThrow() {
        val result = BridgeCommandParser.parse("cmd 'unclosed")
        assertEquals(listOf("cmd", "unclosed"), result)
    }

    @Test
    fun test_parse_unclosedDoubleQuote_doesNotThrow() {
        val result = BridgeCommandParser.parse("cmd \"unclosed")
        assertEquals(listOf("cmd", "unclosed"), result)
    }

    @Test
    fun test_parse_realWorldOneLiner_returnsAllTokens() {
        val result = BridgeCommandParser.parse(
            "claude --print --output-format text --allowedTools \"Read\" \"Grep\""
        )
        assertEquals(
            listOf("claude", "--print", "--output-format", "text", "--allowedTools", "Read", "Grep"),
            result
        )
    }
}
