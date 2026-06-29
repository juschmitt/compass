package de.juschmitt.compass.search.ui

import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import de.juschmitt.compass.search.SearchResult
import javax.swing.JList

class SearchResultCellRenderer : ColoredListCellRenderer<SearchResult>() {

    private fun shortenPath(path: String): String {
        val segments = path.split("/")
        if (segments.size <= 1) return path
        val abbreviated = segments.dropLast(1).map { it.take(1) }
        return (abbreviated + segments.last()).joinToString("/")
    }

    override fun customizeCellRenderer(
        list: JList<out SearchResult>,
        value: SearchResult?,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        value ?: return
        append(shortenPath(value.relativeFilePath), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        append(":${value.startLine}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        if (value.endLine != value.startLine) {
            append("-${value.endLine}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
        append("  ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        append(value.note, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }
}
