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
import java.util.Collections
import java.util.LinkedHashMap

/**
 * Markdown 解析器
 */
object MarkdownParser {

    // 最大递归深度，防止栈溢出
    private const val MAX_RECURSION_DEPTH = 10

    // 解析结果缓存（线程安全的 LRU 缓存）
    private const val MAX_CACHE_SIZE = 200
    private val cache = Collections.synchronizedMap(object : LinkedHashMap<String, AnnotatedString>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, AnnotatedString>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    })

    /**
     * 清除缓存
     */
    fun clearCache() {
        synchronized(cache) {
            cache.clear()
        }
    }

    /**
     * 解析行内格式（粗体、斜体、代码、链接、删除线）
     * @param text 要解析的文本
     * @param depth 当前递归深度（内部使用）
     */
    fun parseInlineFormatting(text: String, depth: Int = 0): AnnotatedString {
        // 超过最大深度，直接返回原文本
        if (depth >= MAX_RECURSION_DEPTH) {
            return buildAnnotatedString { append(text) }
        }

        // 只有顶层调用才使用缓存
        if (depth == 0) {
            synchronized(cache) {
                cache[text]?.let { return it }
            }
        }

        val result = parseInlineFormattingInternal(text, depth)

        // 缓存结果
        if (depth == 0) {
            synchronized(cache) {
                cache[text] = result
            }
        }

        return result
    }

    /**
     * 内部解析方法
     */
    private fun parseInlineFormattingInternal(text: String, depth: Int): AnnotatedString {
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
                                append(parseInlineFormatting(text.substring(i, end), depth + 1))
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

                                // 添加点击链接样式
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
                                append(parseInlineFormatting(text.substring(i, end), depth + 1))
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
                                append(parseInlineFormatting(text.substring(i, end), depth + 1))
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
