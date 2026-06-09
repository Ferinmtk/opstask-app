package com.simplifybiz.ops.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Renders the small subset of HTML our task descriptions/notes actually
 * contain (paragraphs, line breaks, bold, italic, and bullet lists) into a
 * styled AnnotatedString. Anything else is stripped to plain text.
 *
 * Lives in commonMain so it works identically on Android and iOS — the
 * platform HTML renderers (e.g. Html.fromHtml) aren't available in shared code.
 */
fun htmlToAnnotatedString(raw: String): AnnotatedString {
    if (raw.isBlank()) return AnnotatedString("")

    val tagRegex = Regex("<\\s*(/?)\\s*([a-zA-Z0-9]+)[^>]*>")
    val sb = StringBuilder()
    // Spans to apply after the plain text is assembled: (start, end, style)
    data class Span(val start: Int, val end: Int, val bold: Boolean, val italic: Boolean)
    val spans = mutableListOf<Span>()

    var bold = 0
    var italic = 0
    var boldStart = -1
    var italicStart = -1
    var cursor = 0

    fun ensureNewline() {
        if (sb.isNotEmpty() && !sb.endsWith("\n")) sb.append("\n")
    }

    fun emit(text: String) {
        val decoded = decodeEntities(text).replace(Regex("\\s+"), " ")
        if (decoded.isEmpty()) return
        // Avoid leading spaces right after a line break
        if (sb.isEmpty() || sb.endsWith("\n")) {
            sb.append(decoded.trimStart())
        } else {
            sb.append(decoded)
        }
    }

    for (m in tagRegex.findAll(raw)) {
        // Text before this tag
        emit(raw.substring(cursor, m.range.first))
        cursor = m.range.last + 1

        val closing = m.groupValues[1] == "/"
        when (m.groupValues[2].lowercase()) {
            "strong", "b" -> {
                if (!closing) { if (bold == 0) boldStart = sb.length; bold++ }
                else if (bold > 0) { bold--; if (bold == 0) spans.add(Span(boldStart, sb.length, true, false)) }
            }
            "em", "i" -> {
                if (!closing) { if (italic == 0) italicStart = sb.length; italic++ }
                else if (italic > 0) { italic--; if (italic == 0) spans.add(Span(italicStart, sb.length, false, true)) }
            }
            "li" -> if (!closing) { ensureNewline(); sb.append("•  ") } else ensureNewline()
            "br" -> sb.append("\n")
            "p", "div" -> if (closing) ensureNewline() else ensureNewline()
            "ul", "ol" -> ensureNewline()
        }
    }
    // Trailing text after the last tag
    emit(raw.substring(cursor))

    // Collapse excess blank lines and trim
    val text = sb.toString().replace(Regex("\n{3,}"), "\n\n").trim()

    return buildAnnotatedString {
        append(text)
        for (s in spans) {
            val start = s.start.coerceIn(0, text.length)
            val end = s.end.coerceIn(0, text.length)
            if (end > start) {
                addStyle(
                    SpanStyle(
                        fontWeight = if (s.bold) FontWeight.Bold else null,
                        fontStyle = if (s.italic) FontStyle.Italic else null
                    ),
                    start, end
                )
            }
        }
    }
}

private fun decodeEntities(s: String): String {
    var r = s
    val named = mapOf(
        "&lt;" to "<", "&gt;" to ">", "&quot;" to "\"", "&#39;" to "'", "&apos;" to "'",
        "&nbsp;" to " ", "&mdash;" to "\u2014", "&ndash;" to "\u2013", "&hellip;" to "\u2026",
        "&rsquo;" to "\u2019", "&lsquo;" to "\u2018", "&rdquo;" to "\u201D", "&ldquo;" to "\u201C"
    )
    named.forEach { (k, v) -> r = r.replace(k, v) }
    r = Regex("&#(\\d+);").replace(r) { it.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: it.value }
    r = Regex("&#x([0-9a-fA-F]+);").replace(r) { it.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: it.value }
    r = r.replace("&amp;", "&") // decode last to avoid double-decoding
    return r
}
