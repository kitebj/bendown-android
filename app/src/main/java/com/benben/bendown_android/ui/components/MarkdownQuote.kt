package com.benben.bendown_android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benben.bendown_android.parser.MarkdownParser

/**
 * Markdown 引用块组件
 */
@Composable
fun MarkdownQuote(
    level: Int,
    lines: List<String>,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (level) {
        1 -> Pair(Color(0xFFF3E5F5), Color(0xFF6A1B9A))
        2 -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0))
        3 -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
        else -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = ((level - 1) * 12).dp,
                top = if (level > 1) 2.dp else 0.dp
            ),
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp, 8.dp)) {
            lines.forEachIndexed { index, quoteLine ->
                Text(
                    text = MarkdownParser.parseInlineFormatting(quoteLine),
                    fontStyle = FontStyle.Italic,
                    color = textColor,
                    lineHeight = 22.sp
                )
                if (index < lines.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
