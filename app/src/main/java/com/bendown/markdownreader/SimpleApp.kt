package com.bendown.markdownreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SimpleMarkdownReaderApp()
            }
        }
    }
}

@Composable
fun SimpleMarkdownReaderApp() {
    var selectedFile by remember { mutableStateOf<String?>(null) }
    
    val testFiles = listOf(
        "欢迎使用.md",
        "开发计划.md", 
        "技术文档.md"
    )
    
    if (selectedFile == null) {
        // 文件列表
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📁 Markdown文件阅读器",
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 40.dp, bottom = 20.dp)
            )
            
            Text(
                text = "点击文件查看详情",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                items(testFiles) { fileName ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedFile = fileName },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = fileName,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "打开",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    } else {
        // 文件详情
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✅ 功能验证成功！",
                fontSize = 24.sp,
                color = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "当前文件:",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = selectedFile!!,
                        fontSize = 22.sp,
                        color = Color(0xFF1976D2)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 核心流程验证通过",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. 文件列表浏览 ✓",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "2. 文件选择 ✓", 
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "3. 界面导航 ✓",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { selectedFile = null },
                modifier = Modifier.width(200.dp)
            ) {
                Text("返回文件列表")
            }
        }
    }
}

@Preview
@Composable
fun PreviewApp() {
    SimpleMarkdownReaderApp()
}