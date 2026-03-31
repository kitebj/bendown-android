package com.benben.bendown_android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benben.bendown_android.parser.MarkdownParser

/**
 * Markdown 无序列表组件
 */
@Composable
fun MarkdownUnorderedList(
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "•",
                    fontSize = 18.sp,
                    color = Color(0xFF1976D2),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(
                    text = MarkdownParser.parseInlineFormatting(item),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

/**
 * Markdown 有序列表组件
 */
@Composable
fun MarkdownOrderedList(
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "${index + 1}.",
                    fontSize = 15.sp,
                    color = Color(0xFF1976D2),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(
                    text = MarkdownParser.parseInlineFormatting(item),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

/**
 * Markdown 任务列表组件
 */
@Composable
fun MarkdownTaskList(
    items: List<Pair<Boolean, String>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        items.forEach { (checked, item) ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = MarkdownParser.parseInlineFormatting(item),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textDecoration = if (checked) TextDecoration.LineThrough else null,
                    color = if (checked) Color.Gray else Color.Unspecified
                )
            }
        }
    }
}
