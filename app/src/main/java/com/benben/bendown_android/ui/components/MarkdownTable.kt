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
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
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
 * 使用 SubcomposeLayout 先测量每列最大宽度，再统一应用
 */
@Composable
fun MarkdownTable(
    headers: List<String>,
    alignments: List<TableAlignment>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val columnCount = maxOf(headers.size, rows.maxOfOrNull { it.size } ?: 0)

    Column(
        modifier = modifier
            .padding(vertical = 8.dp)
            .horizontalScroll(scrollState)
    ) {
        TableWithAdaptiveColumns(
            columnCount = columnCount,
            headers = headers,
            alignments = alignments,
            rows = rows
        )
    }
}

/**
 * 使用 SubcomposeLayout 实现自适应列宽的表格
 */
@Composable
private fun TableWithAdaptiveColumns(
    columnCount: Int,
    headers: List<String>,
    alignments: List<TableAlignment>,
    rows: List<List<String>>
) {
    SubcomposeLayout { constraints ->
        // 第一步：测量所有单元格，找出每列的最大宽度
        val columnWidths = IntArray(columnCount) { 0 }

        // 测量表头单元格
        for (colIndex in 0 until columnCount) {
            val measurable = subcompose("header_$colIndex") {
                TableCell(
                    text = headers.getOrNull(colIndex) ?: "",
                    alignment = alignments.getOrElse(colIndex) { TableAlignment.LEFT },
                    isHeader = true
                )
            }.first()
            val placeable = measurable.measure(Constraints())
            columnWidths[colIndex] = maxOf(columnWidths[colIndex], placeable.width)
        }

        // 测量所有数据行单元格
        for ((rowIndex, row) in rows.withIndex()) {
            for (colIndex in 0 until columnCount) {
                val measurable = subcompose("cell_${rowIndex}_$colIndex") {
                    TableCell(
                        text = row.getOrNull(colIndex) ?: "",
                        alignment = alignments.getOrElse(colIndex) { TableAlignment.LEFT },
                        isHeader = false
                    )
                }.first()
                val placeable = measurable.measure(Constraints())
                columnWidths[colIndex] = maxOf(columnWidths[colIndex], placeable.width)
            }
        }

        // 第二步：使用计算好的列宽，重新测量和布局所有行
        val totalWidth = columnWidths.sum()

        // 布局所有行
        val headerPlaceables = subcompose("header_row") {
            Row(Modifier.background(Color(0xFFF5F5F5))) {
                for (colIndex in 0 until columnCount) {
                    Box(Modifier.width(columnWidths[colIndex].dp)) {
                        TableCell(
                            text = headers.getOrNull(colIndex) ?: "",
                            alignment = alignments.getOrElse(colIndex) { TableAlignment.LEFT },
                            isHeader = true
                        )
                    }
                }
            }
        }.first().measure(Constraints.fixedWidth(totalWidth))

        val rowPlaceables = rows.mapIndexed { rowIndex, row ->
            val bgColor = if (rowIndex % 2 == 0) Color.White else Color(0xFFFAFAFA)
            subcompose("data_row_$rowIndex") {
                Row(Modifier.background(bgColor)) {
                    for (colIndex in 0 until columnCount) {
                        Box(Modifier.width(columnWidths[colIndex].dp)) {
                            TableCell(
                                text = row.getOrNull(colIndex) ?: "",
                                alignment = alignments.getOrElse(colIndex) { TableAlignment.LEFT },
                                isHeader = false
                            )
                        }
                    }
                }
            }.first().measure(Constraints.fixedWidth(totalWidth))
        }

        // 计算总高度
        var totalHeight = headerPlaceables.height
        rowPlaceables.forEach { totalHeight += it.height }
        totalHeight += (1.dp.roundToPx()) // 表头分隔线
        totalHeight += ((rows.size - 1) * 0.5.dp.roundToPx()) // 行分隔线

        // 布局
        layout(totalWidth, totalHeight) {
            var y = 0

            // 放置表头
            headerPlaceables.placeRelative(0, y)
            y += headerPlaceables.height

            // 放置表头分隔线
            y += 1.dp.roundToPx()

            // 放置数据行
            rowPlaceables.forEachIndexed { index, placeable ->
                placeable.placeRelative(0, y)
                y += placeable.height

                if (index < rowPlaceables.size - 1) {
                    y += 0.5.dp.roundToPx()
                }
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
    )
}
