# Compass

An IntelliJ IDEA plugin that enhances traditional coding with AI.
No AI agent mode, no code suggestions/completions, no chat.
Set an objective (optional), search for what you want to do, go through the AI-generated list and write the code yourself.
Or use selection rewrite to let AI rewrite sections when you want to.

## Features

### Objective Tracking
- Set and track development objectives directly in your IDE
- Objective displayed in status bar for constant visibility
- Helps maintain focus on current development goals

### AI-Powered Code Rewriting
- Select code and rewrite it using AI with inline prompts
- Real-time diff visualization showing proposed changes
- Accept or reject changes with inline controls

### Intelligent Search
- Enhanced search capabilities powered by AI
- Context-aware code exploration
- Results are tracked in a separate tool window
- Results also appear as inline note annotations above each matched line in already-open files, dismissable per result

## Installation

### From JetBrains Marketplace
_(Coming soon)_

### From Source
1. Clone this repository
2. Open in IntelliJ IDEA
3. Run the `Run Plugin` configuration

## Configuration

Configure the plugin via **Settings → Tools → Compass**. The page contains a global bridge command field and a checkbox to enable a per-project override with its own bridge command field.

The only required setting is the **Bridge Command** — a full shell command string that the plugin executes with the project root as the working directory. The plugin sends the prompt via stdin and reads a JSON response from stdout.

### Example: Claude Code as the bridge command

```bash
claude --print --output-format text --allowedTools "Read" "Grep" "Glob" "Bash" --model claude-sonnet-4-5 "$(cat)"
```

Any LLM CLI or script that reads from stdin and writes the expected JSON to stdout works as a bridge command.

## Keyboard Shortcuts

| Action                | Windows / Linux | macOS           |
|-----------------------|-----------------|-----------------|
| Set Current Objective | `Ctrl+Alt+O`    | `Ctrl+Option+O` |
| Run Search            | `Ctrl+Alt+F`    | `Ctrl+Option+F` |
| Selection Rewrite     | `Ctrl+Alt+R`    | `Ctrl+Option+R` |

All shortcuts can be customised in **Settings → Keymap → Compass**.

## Usage

### Setting an Objective

- Invoke **Set Current Objective** (`Ctrl+Alt+O` / `Ctrl+Option+O`) or click the status bar widget.
- Enter your current development goal and confirm.
- The objective appears in the status bar and in the search tool window for reference.
- Submit an empty value to clear the objective.
- The objective is session-only and does not persist across IDE restarts.

### Running a Search

- Invoke **Run Search** (`Ctrl+Alt+F` / `Ctrl+Option+F`).
- Enter a search prompt. If a current objective is set, it is included automatically as background context.
- Results appear in the search tool window with a relative file path, start line, end line, and a short explanatory note for each match.
- In files that are already open, results also appear as inline note annotations above each matched line; each annotation can be dismissed individually.
- Starting a new search cancels any in-flight search. Old results remain visible until the new result set is complete.

### Rewriting a Selection

- Select the code you want to change in the editor.
- Invoke **Selection Rewrite** (`Ctrl+Alt+R` / `Ctrl+Option+R`).
- Enter your rewrite instruction in the inline prompt that appears in the editor.
- Review the inline diff. Accept or reject the change as a whole.
- The file is not modified unless you explicitly apply the change through the plugin.

## Bridge Command Setup

The plugin communicates with an external AI tool through a configurable bridge command. The plugin sends the full prompt text to the command via stdin and expects a JSON response on stdout, with the project root as the working directory.

### Configuration in Plugin Settings

1. Navigate to **Settings → Tools → Compass**
2. In the **Bridge Command** field, enter your configured command
3. To use a different command for a specific project, enable the per-project override checkbox and enter a project-specific command

## Development

### Requirements
- Java 21 or higher
- IntelliJ IDEA 2025.2 or higher (Android Studio Meerkat or later)

### Building
```bash
./gradlew buildPlugin
```

### Running
```bash
./gradlew runIde
```

### Testing
```bash
./gradlew test
```

## Architecture

- `bridge/` - Communication layer with the bridge command
- `config/` - Plugin configuration and settings
- `objective/` - Objective tracking and status bar integration
- `rewrite/` - Code rewriting engine and UI
- `search/` - Search functionality
- `prompt/` - Prompt building and management

## License

MIT

## Inspiration

Inspired by the Neovim plugin [ThePrimeagen/99](https://github.com/ThePrimeagen/99).
