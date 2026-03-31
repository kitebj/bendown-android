package com.benben.bendown_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benben.bendown_android.parser.MarkdownParser

/**
 * 表格对齐方式
 */
enum class TableAlignment {
    LEFT, CENTER, RIGHT
}

/**
 * Markdown 表格渲染组件
 */
@Composable
fun MarkdownTable(
    headers: List<String>,
    alignments: List<TableAlignment>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(scrollState)
    ) {
        // 表头
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
        ) {
            headers.forEachIndexed { index, header ->
                TableCell(
                    text = header,
                    alignment = alignments.getOrElse(index) { TableAlignment.LEFT },
                    isHeader = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 分隔线
        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFE0E0E0)
        )

        // 数据行
        rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (rowIndex % 2 == 0) Color.White else Color(0xFFFAFAFA))
            ) {
                row.forEachIndexed { index, cell ->
                    TableCell(
                        text = cell,
                        alignment = alignments.getOrElse(index) { TableAlignment.LEFT },
                        isHeader = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 行分隔线
            if (rowIndex < rows.size - 1) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Color(0xFFEEEEEE)
                )
            }
        }
    }
}

/**
 * 表格单元格
 */
@Composable
fun TableCell(
    text: String,
    alignment: TableAlignment,
    isHeader: Boolean,
    modifier: Modifier = Modifier
) {
    val textAlign = when (alignment) {
        TableAlignment.LEFT -> TextAlign.Start
        TableAlignment.CENTER -> TextAlign.Center
        TableAlignment.RIGHT -> TextAlign.End
    }

    Text(
        text = MarkdownParser.parseInlineFormatting(text.trim()),
        fontSize = 13.sp,
        fontWeight = if (isHeader) FontWeight.Medium else FontWeight.Normal,
        textAlign = textAlign,
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .fillMaxWidth()
    )
}
