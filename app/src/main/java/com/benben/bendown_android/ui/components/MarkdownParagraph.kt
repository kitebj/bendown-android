package com.benben.bendown_android.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
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
    val context = LocalContext.current
    val annotatedText = MarkdownParser.parseInlineFormatting(text)

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            // 检查是否点击了链接
            annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    // 打开浏览器
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                    context.startActivity(intent)
                }
        },
        style = androidx.compose.ui.text.TextStyle(
            fontSize = 15.sp,
            lineHeight = 24.sp,
            color = Color(0xFF212121)
        ),
        modifier = modifier.padding(vertical = 2.dp)
    )
}
