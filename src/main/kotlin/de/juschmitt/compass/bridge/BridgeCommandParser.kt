package de.juschmitt.compass.bridge

object BridgeCommandParser {
    fun parse(command: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var i = 0
        while (i < command.length) {
            when {
                command[i] == '\'' -> {
                    val end = command.indexOf('\'', i + 1)
                    if (end == -1) {
                        current.append(command.substring(i + 1))
                        i = command.length
                    } else {
                        current.append(command.substring(i + 1, end))
                        i = end + 1
                    }
                }
                command[i] == '"' -> {
                    val end = command.indexOf('"', i + 1)
                    if (end == -1) {
                        current.append(command.substring(i + 1))
                        i = command.length
                    } else {
                        current.append(command.substring(i + 1, end))
                        i = end + 1
                    }
                }
                command[i].isWhitespace() -> {
                    if (current.isNotEmpty()) {
                        tokens.add(current.toString())
                        current.clear()
                    }
                    i++
                }
                else -> {
                    current.append(command[i])
                    i++
                }
            }
        }
        if (current.isNotEmpty()) tokens.add(current.toString())
        return tokens
    }
}
