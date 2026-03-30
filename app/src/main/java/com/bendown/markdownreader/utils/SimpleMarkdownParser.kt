package com.bendown.markdownreader.utils

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * 简单的Markdown解析器
 * 用于MVP阶段的基础解析
 */
object SimpleMarkdownParser {
    
    /**
     * 解析Markdown文本为AnnotatedString
     */
    fun parseToAnnotatedString(markdown: String): List<AnnotatedStringBlock> {
        val lines = markdown.lines()
        val blocks = mutableListOf<AnnotatedStringBlock>()
        
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            
            when {
                // 空行
                line.isEmpty() -> {
                    blocks.add(AnnotatedStringBlock("\n\n", BlockType.PARAGRAPH))
                    i++
                }
                
                // 标题 (#, ##, ###)
                line.startsWith("#") -> {
                    val level = line.takeWhile { it == '#' }.length
                    val title = line.substring(level).trim()
                    blocks.add(AnnotatedStringBlock(title, BlockType.HEADING(level)))
                    i++
                }
                
                // 无序列表项
                line.startsWith("- ") || line.startsWith("* ") -> {
                    val items = mutableListOf<String>()
                    while (i < lines.size && (lines[i].trim().startsWith("- ") || lines[i].trim().startsWith("* "))) {
                        items.add(lines[i].trim().substring(2))
                        i++
                    }
                    blocks.add(AnnotatedStringBlock(items.joinToString("\n"), BlockType.UNORDERED_LIST))
                }
                
                // 有序列表项
                Regex("^\\d+\\.\\s").containsMatchIn(line) -> {
                    val items = mutableListOf<String>()
                    while (i < lines.size && Regex("^\\d+\\.\\s").containsMatchIn(lines[i].trim())) {
                        val item = lines[i].trim()
                        items.add(item.substring(item.indexOf('.') + 2))
                        i++
                    }
                    blocks.add(AnnotatedStringBlock(items.joinToString("\n"), BlockType.ORDERED_LIST))
                }
                
                // 引用块
                line.startsWith("> ") -> {
                    val quotes = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim().startsWith("> ")) {
                        quotes.add(lines[i].trim().substring(2))
                        i++
                    }
                    blocks.add(AnnotatedStringBlock(quotes.joinToString("\n"), BlockType.BLOCKQUOTE))
                }
                
                // 代码块
                line.startsWith("```") -> {
                    val language = line.substring(3).trim()
                    val codeLines = mutableListOf<String>()
                    i++
                    
                    while (i < lines.size && !lines[i].trim().startsWith("```")) {
                        codeLines.add(lines[i])
                        i++
                    }
                    
                    if (i < lines.size && lines[i].trim().startsWith("```")) {
                        i++ // 跳过结束标记
                    }
                    
                    blocks.add(AnnotatedStringBlock(
                        codeLines.joinToString("\n"),
                        BlockType.CODE_BLOCK(language)
                    ))
                }
                
                // 普通段落
                else -> {
                    val paragraphLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim().isNotEmpty() &&
                           !lines[i].trim().startsWith("#") &&
                           !lines[i].trim().startsWith("- ") &&
                           !lines[i].trim().startsWith("* ") &&
                           !Regex("^\\d+\\.\\s").containsMatchIn(lines[i].trim()) &&
                           !lines[i].trim().startsWith("> ") &&
                           !lines[i].trim().startsWith("```")) {
                        paragraphLines.add(lines[i].trim())
                        i++
                    }
                    if (paragraphLines.isNotEmpty()) {
                        blocks.add(AnnotatedStringBlock(
                            paragraphLines.joinToString(" "),
                            BlockType.PARAGRAPH
                        ))
                    }
                }
            }
        }
        
        return blocks
    }
    
    /**
     * 处理行内格式（粗体、斜体、代码）
     */
    fun processInlineFormatting(text: String): AnnotatedStringBuilderResult {
        val builder = buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                when {
                    // 粗体 **text** 或 __text__
                    i + 1 < text.length && (text.substring(i, i + 2) == "**" || text.substring(i, i + 2) == "__") -> {
                        val delimiter = text.substring(i, i + 2)
                        i += 2
                        val end = text.indexOf(delimiter, i)
                        if (end != -1) {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(processInlineFormatting(text.substring(i, end)).text)
                            }
                            i = end + 2
                        } else {
                            append(delimiter)
                        }
                    }
                    
                    // 斜体 *text* 或 _text_
                    text[i] == '*' || text[i] == '_' -> {
                        val delimiter = text[i].toString()
                        i++
                        val end = text.indexOf(delimiter, i)
                        if (end != -1) {
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(processInlineFormatting(text.substring(i, end)).text)
                            }
                            i = end + 1
                        } else {
                            append(delimiter)
                        }
                    }
                    
                    // 行内代码 `code`
                    text[i] == '`' -> {
                        i++
                        val end = text.indexOf('`', i)
                        if (end != -1) {
                            withStyle(SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = androidx.compose.ui.graphics.Color.LightGray
                            )) {
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
        
        return AnnotatedStringBuilderResult(builder)
    }
}

/**
 * Markdown块类型
 */
sealed class BlockType {
    object PARAGRAPH : BlockType()
    data class HEADING(val level: Int) : BlockType()
    object UNORDERED_LIST : BlockType()
    object ORDERED_LIST : BlockType()
    object BLOCKQUOTE : BlockType()
    data class CODE_BLOCK(val language: String = "") : BlockType()
}

/**
 * 带类型的文本块
 */
data class AnnotatedStringBlock(
    val text: String,
    val type: BlockType
)

/**
 * 解析结果
 */
data class AnnotatedStringBuilderResult(
    val text: androidx.compose.ui.text.AnnotatedString
)