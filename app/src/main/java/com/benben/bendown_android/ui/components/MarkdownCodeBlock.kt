package com.benben.bendown_android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Markdown 代码块组件
 */
@Composable
fun MarkdownCodeBlock(
    language: String,
    codeLines: List<String>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(vertical = 8.dp),
        color = Color(0xFF2D2D2D),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (language.isNotEmpty()) {
                Text(
                    text = language,
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Text(
                text = codeLines.joinToString("\n"),
                color = Color(0xFFE0E0E0),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}
