package com.benben.bendown_android.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benben.bendown_android.parser.MarkdownParser

/**
 * Markdown 段落组件
 */
@Composable
fun MarkdownParagraph(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = MarkdownParser.parseInlineFormatting(text),
        fontSize = 15.sp,
        lineHeight = 24.sp,
        color = Color(0xFF212121),
        modifier = modifier.padding(vertical = 2.dp)
    )
}
