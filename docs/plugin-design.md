# Compass — Plugin Design

## Design Philosophy

Compass is built on the principle that the developer stays in control. AI acts as a focused assistant scoped to explicit, user-initiated requests — it is not an autonomous agent and does not modify files without the developer reviewing and applying a diff. Every code change travels through an inline diff/apply flow before it touches the editor. The plugin is provider-agnostic: it defines the prompt structure and response schemas, and delegates all LLM interaction to a user-configured bridge command. This means any LLM backend, CLI, or script the user trusts can be used without changes to the plugin itself.

## Features

### Objective Tracking

The plugin maintains a single current objective per project session. The objective is visible in the status bar and the search tool window at all times. Users set or replace the objective through a popup action; submitting an empty value clears it. The objective is session-only and is not persisted across IDE restarts. When an objective is set, it is automatically included as background context in both search and rewrite prompts.

### Search

Search is invoked through a dedicated editor action. The user enters a search prompt, which may include free-text and raw `@file` references. The plugin builds the full prompt — combining the user's query with the current objective when present — and sends it to the bridge command. Results are displayed in a dedicated tool window. Each result includes a relative file path, a start line, an end line, and a short explanatory note. In files that are already open in the editor, results also appear as inline note annotations above each matched line; each annotation can be dismissed individually. Only one search runs at a time; starting a new search cancels the previous one, and old results remain visible until the new result set arrives successfully.

### Selection Rewrite

Selection rewrite is invoked on a non-empty text selection. An inline prompt appears in the editor where the user enters a rewrite instruction. The plugin builds the rewrite prompt from the selected text, the current file path, the user instruction, and the current objective when set, then executes the bridge command. On a valid response, the plugin renders an inline diff directly in the editor. The user accepts or rejects the entire change. No file modification occurs unless the user explicitly applies the diff through the plugin. Multiple rewrites can run concurrently across different files or non-overlapping regions of the same file; an attempt to start a rewrite overlapping an in-flight one in the same file is rejected with an error.

### Configuration

The plugin exposes a single settings page at **Settings → Tools → Compass**. A global bridge command is configured there. A checkbox enables a per-project override with its own bridge command field, allowing different commands per project. Prompt templates are owned by the plugin and are not user-configurable.

## Bridge Contract

### Execution Model

The plugin executes the configured bridge command as a full shell command string. The working directory is always set to the project root. The full plugin-generated prompt is written to the command's stdin. The plugin reads the command's stdout and parses it as JSON. The project root path is not sent as part of the prompt or any other input parameter — the bridge infers it from its working directory if needed.

The bridge is treated as transport only. The plugin does not assume the bridge understands plugin operations beyond receiving a prompt on stdin and returning JSON on stdout. The bridge may invoke tools or run sub-commands internally; this is fully allowed. The bridge must not modify project files directly — all code changes must return through the plugin's diff/apply flow.

### JSON Response Schemas

The plugin defines two fixed response schemas. The prompt instructs the backend to return one of them depending on the active flow.

**Search response**

```json
{
  "results": [
    {
      "relativeFilePath": "app/src/main/Example.kt",
      "startLine": 12,
      "endLine": 18,
      "note": "Why this location matters"
    }
  ]
}
```

Every result must include all four fields. File paths are relative to the project root. Line numbers are 1-based.

**Rewrite response**

```json
{
  "replacementText": "model-generated replacement for the selected range only"
}
```

The replacement text covers the selected range only. Diffing and applying the replacement are handled entirely by the plugin.

### @file References

Raw `@file` references typed by the user in search or rewrite prompts are passed through to the bridge unchanged. The plugin does not resolve them. Resolution is the responsibility of the backend or CLI the user has configured.

### Instruction File Discovery

The plugin does not auto-discover `CLAUDE.md`, `AGENTS.md`, `AGENT.md`, or similar convention files. If the backend should load such files, that behavior belongs to the bridge command chosen by the user.

## Example Bridge Command

The following command uses Claude Code as the bridge:

```bash
claude --print --output-format text --allowedTools "Read" "Grep" "Glob" "Bash" --model claude-sonnet-4-5 "$(cat)"
```

Any LLM CLI or script that reads from stdin and writes the expected JSON schema to stdout works as a bridge command. The plugin imposes no constraints on the backend beyond the response schema.

## Architecture

The plugin is organised into the following top-level packages:

- `bridge/` — executes the configured bridge command, captures stdout, and parses the JSON response. Handles process lifecycle and cancellation.
- `config/` — global and per-project settings models, the settings UI, and effective-config resolution (global default overridden by per-project when enabled).
- `objective/` — the session objective service that stores the current objective in memory, and the status bar widget that displays it.
- `prompt/` — assembles the final prompt text sent to the bridge, combining user input, objective context, selected text, and the response schema instruction.
- `rewrite/` — the selection rewrite flow: inline prompt component, thinking indicator, bridge invocation, inline diff rendering, and accept/reject apply logic.
- `search/` — the search flow: search popup, bridge invocation, the search tool window and result list, and the inline note annotations shown in open editors.

## Concurrency Model

Only one search may be in flight at a time. Starting a new search cancels the previous one immediately; old results remain visible in the tool window until a new result set successfully completes. Multiple rewrites may run concurrently as long as their target regions do not overlap within the same file. Attempting to start a rewrite that overlaps an active rewrite in the same file produces an error and does not start the new request. Users can cancel any in-flight search or rewrite request at any time.
