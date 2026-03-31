package com.benben.bendown_android.parser

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color

/**
 * Markdown解析器
 */
object MarkdownParser {

    /**
     * 解析行内格式（粗体、斜体、代码、链接、删除线）
     */
    fun parseInlineFormatting(text: String): AnnotatedString {
        return buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                when {
                    // 删除线 ~~text~~
                    i + 1 < text.length && text.substring(i, i + 2) == "~~" -> {
                        i += 2
                        val end = text.indexOf("~~", i)
                        if (end != -1) {
                            withStyle(
                                style = SpanStyle(textDecoration = TextDecoration.LineThrough)
                            ) {
                                append(parseInlineFormatting(text.substring(i, end)))
                            }
                            i = end + 2
                        } else {
                            append("~~")
                        }
                    }

                    // 链接 [text](url)
                    text[i] == '[' -> {
                        val linkEnd = text.indexOf("](", i)
                        if (linkEnd != -1) {
                            val linkText = text.substring(i + 1, linkEnd)
                            val urlStart = linkEnd + 2
                            val urlEnd = text.indexOf(")", urlStart)
                            if (urlEnd != -1) {
                                val url = text.substring(urlStart, urlEnd)

                                // 添加点击链接
                                withStyle(
                                    style = SpanStyle(
                                        color = Color(0xFF1976D2),
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append(linkText)
                                }

                                // 添加点击事件（使用StringAnnotation）
                                addStringAnnotation(
                                    tag = "URL",
                                    annotation = url,
                                    start = length - linkText.length,
                                    end = length
                                )

                                i = urlEnd + 1
                            } else {
                                append(text[i])
                                i++
                            }
                        } else {
                            append(text[i])
                            i++
                        }
                    }

                    // 粗体 **text** 或 __text__
                    i + 1 < text.length && (text.substring(i, i + 2) == "**" || text.substring(i, i + 2) == "__") -> {
                        val delimiter = text.substring(i, i + 2)
                        i += 2
                        val end = text.indexOf(delimiter, i)
                        if (end != -1) {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(parseInlineFormatting(text.substring(i, end)))
                            }
                            i = end + 2
                        } else {
                            append(delimiter)
                        }
                    }

                    // 斜体 *text* 或 _text_
                    (text[i] == '*' || text[i] == '_') -> {
                        val delimiter = text[i].toString()
                        i++
                        val end = text.indexOf(delimiter, i)
                        if (end != -1) {
                            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(parseInlineFormatting(text.substring(i, end)))
                            }
                            i = end + 2
                        } else {
                            append(delimiter)
                        }
                    }

                    // 行内代码 `code`
                    text[i] == '`' -> {
                        i++
                        val end = text.indexOf('`', i)
                        if (end != -1) {
                            withStyle(
                                style = SpanStyle(
                                    fontFamily = FontFamily.Monospace,
                                    background = Color(0xFFF5F5F5),
                                    color = Color(0xFFD32F2F)
                                )
                            ) {
                                append(text.substring(i, end))
                            }
                            i = end + 1
                        } else {
                            append('`')
                        }
                    }

                    else -> {
                        append(text[i])
                        i++
                    }
                }
            }
        }
    }
}
