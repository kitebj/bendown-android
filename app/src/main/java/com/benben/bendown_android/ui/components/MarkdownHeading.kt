package com.benben.bendown_android.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benben.bendown_android.parser.MarkdownParser

/**
 * Markdown 标题组件
 */
@Composable
fun MarkdownHeading(
    level: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    val (fontSize, fontWeight, color, paddingTop, paddingBottom) = when (level) {
        1 -> Tuple5(28.sp, FontWeight.Bold, Color(0xFF1976D2), 24.dp, 12.dp)
        2 -> Tuple5(22.sp, FontWeight.Bold, Color(0xFF424242), 20.dp, 10.dp)
        3 -> Tuple5(18.sp, FontWeight.SemiBold, Color(0xFF616161), 16.dp, 8.dp)
        else -> Tuple5(16.sp, FontWeight.Medium, Color.Unspecified, 12.dp, 6.dp)
    }

    Text(
        text = MarkdownParser.parseInlineFormatting(text),
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier.padding(top = paddingTop, bottom = paddingBottom)
    )
}

/**
 * 辅助数据类，用于返回多个值
 */
private data class Tuple5<T1, T2, T3, T4, T5>(
    val first: T1,
    val second: T2,
    val third: T3,
    val fourth: T4,
    val fifth: T5
)
