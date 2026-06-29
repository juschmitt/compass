package de.juschmitt.compass.prompt

object PromptBuilder {

    private val SEARCH_SCHEMA = """
        {
          "results": [
            {
              "relativeFilePath": "relative/path/to/File.kt",
              "startLine": 1,
              "endLine": 10,
              "note": "Brief explanation of why this location is relevant"
            }
          ]
        }
    """.trimIndent()

    private val REWRITE_SCHEMA = """
        {
          "replacementText": "the complete replacement text for the selected range"
        }
    """.trimIndent()

    fun buildSearch(
        searchPrompt: String,
        objective: String?
    ): String = buildString {
        appendLine("You are a code search assistant. Use your available tools to search the project codebase and return results as JSON.")
        appendLine()
        if (!objective.isNullOrBlank()) {
            appendLine("Current developer objective:")
            appendLine(objective)
            appendLine()
        }
        appendLine("Search prompt:")
        appendLine(searchPrompt)
        appendLine()
        appendLine("Return ONLY a valid JSON object matching this exact schema (no markdown, no explanation):")
        appendLine(SEARCH_SCHEMA)
        appendLine()
        appendLine("Rules:")
        appendLine("- Every result must include all four fields: relativeFilePath, startLine, endLine, note.")
        appendLine("- File paths must be relative to the project root.")
        appendLine("- Line numbers are 1-based.")
        appendLine("- Exclude files that would be excluded by .gitignore.")
        appendLine("- Return up to 10 of the most relevant results, ordered by relevance.")
        appendLine("- Return an empty results array if nothing relevant is found.")
        appendLine("- Do not wrap the JSON in markdown code fences. Simply return the JSON object as text.")
    }

    fun buildRewrite(
        selectionText: String,
        filePath: String,
        userPrompt: String,
        objective: String?
    ): String = buildString {
        appendLine("You are a code rewrite assistant. Rewrite the provided code selection and return the result as JSON.")
        appendLine()
        if (!objective.isNullOrBlank()) {
            appendLine("Current developer objective:")
            appendLine(objective)
            appendLine()
        }
        appendLine("File: $filePath")
        appendLine()
        appendLine("Selected text to rewrite:")
        appendLine("```")
        appendLine(selectionText)
        appendLine("```")
        appendLine()
        appendLine("Rewrite instruction:")
        appendLine(userPrompt)
        appendLine()
        appendLine("Return ONLY a valid JSON object matching this exact schema (no markdown, no explanation):")
        appendLine(REWRITE_SCHEMA)
        appendLine()
        appendLine("Rules:")
        appendLine("- replacementText must be the complete replacement for the selected range only.")
        appendLine("- Read the full file at the path above to understand the surrounding context before rewriting.")
        appendLine("- Preserve indentation style and programming language idioms of the original.")
        appendLine("- Do NOT write files yourself.")
        appendLine("- Do not wrap the JSON in markdown code fences. Simply return the JSON object as text.")
    }
}
