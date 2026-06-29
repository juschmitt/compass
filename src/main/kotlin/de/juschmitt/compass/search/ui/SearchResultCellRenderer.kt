package de.juschmitt.compass.search.ui

import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import de.juschmitt.compass.search.SearchResult
import javax.swing.JList

class SearchResultCellRenderer : ColoredListCellRenderer<SearchResult>() {
    override fun customizeCellRenderer(
        list: JList<out SearchResult>,
        value: SearchResult?,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        value ?: return
        append(value.relativeFilePath, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        append(":${value.startLine}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        if (value.endLine != value.startLine) {
            append("-${value.endLine}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
        append("  ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        append(value.note, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }
}
