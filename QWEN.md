# BenDown Android - 关键决策记录

## 项目信息
- **项目名称**: BenDown Android
- **包名**: com.benben.bendown_android
- **当前版本**: 0.2.7
- **开始日期**: 2024年
- **更新日志**: [CHANGELOG.md](./CHANGELOG.md)

---

## 关键决策记录

| 日期 | 决策 | 结论 |
|------|------|------|
| 2024-03-30 | 完成代码Review | 发现高优先级问题：未使用导入、Deprecated API、硬编码资源 |
| 2024-03-30 | 完成高优先级修复 | 移除未使用导入、修复Deprecated API、清理临时文件 |
| 2024-03-30 | 评估文件系统设计 | 决定采纳SAF方案，不申请全局存储权限 |
| 2024-03-30 | 确定实施顺序 | 先做结构重构，再加文件功能 |
| 2026-03-31 | 完成阶段0重构 | 代码结构拆分完成，构建通过 |
| 2026-03-31 | 阶段1按场景拆分 | 拆分为4个小阶段，每个可独立验证 |
| 2026-03-31 | 完成阶段1：基础文件支持 | 三种场景全部完成（正常启动/VIEW/SEND） |

---

## 📋 完整任务清单

### 阶段0：代码结构重构 ✅ 已完成
- [x] refactor-001: 创建数据模型层 - data/model/MarkdownFile.kt
- [x] refactor-002: 创建UI组件层 - ui/components/MarkdownHeading.kt
- [x] refactor-003: 创建UI组件层 - ui/components/MarkdownList.kt
- [x] refactor-004: 创建UI组件层 - ui/components/MarkdownQuote.kt
- [x] refactor-005: 创建UI组件层 - ui/components/MarkdownCodeBlock.kt
- [x] refactor-006: 创建UI组件层 - ui/components/MarkdownParagraph.kt
- [x] refactor-007: 创建页面层 - ui/screens/FileListScreen.kt
- [x] refactor-008: 创建页面层 - ui/screens/MarkdownViewerScreen.kt
- [x] refactor-009: 创建解析器层 - parser/MarkdownParser.kt
- [x] refactor-010: 简化MainActivity.kt - 只保留Activity入口
- [x] refactor-011: 验证重构 - 所有功能正常工作

### Review改进项
- [ ] review-001: 提取硬编码字符串到strings.xml
- [ ] review-002: 提取硬编码颜色到colors.xml
- [ ] review-003: 提取硬编码尺寸到dimens.xml
- [ ] review-004: 使用LazyColumn替代Column（性能优化）
- [ ] review-005: 将正则表达式定义为常量

---

### 阶段1：基础文件支持（按场景拆分）✅ 已完成

#### 阶段1a：基础架构 + 正常启动场景
- [x] 1a-001: 修改 AndroidManifest.xml - 添加基础配置
- [x] 1a-002: 创建 DocumentResolver 接口和简化实现
- [x] 1a-003: 实现文件选择器（ACTION_OPEN_DOCUMENT）
- [x] 1a-004: 验证：正常启动 → 选择文件 → 显示内容 ✅

#### 阶段1b：VIEW Intent 场景（其他 app 打开）
- [x] 1b-001: 修改 AndroidManifest.xml - 添加 VIEW Intent Filter
- [x] 1b-002: 实现 handleIntent() 处理 VIEW Intent
- [x] 1b-003: 验证：从文件管理器点击 .md 文件 → 直接打开 ✅

#### 阶段1c：SEND Intent 场景（分享接收）
- [x] 1c-001: 修改 AndroidManifest.xml - 添加 SEND Intent Filter
- [x] 1c-002: 完善 handleIntent() 处理 SEND Intent
- [x] 1c-003: 验证：从其他 app 分享 .md 文件 → 接收并显示 ✅

#### 阶段1d：安全措施 + 清理
- [x] 1d-001: 添加文件大小限制（防 OOM，10MB）
- [x] 1d-002: 添加文件类型检查（.md/.txt/.markdown）
- [x] 1d-003: 添加二进制文件检查
- [x] 1d-004: 移除 assets 文件读取代码
- [x] 1d-005: 更新首页 - 不再显示示例文件，只显示选择按钮

---

## 📁 推荐的文件结构

```
app/src/main/java/com/benben/bendown_android/
├── MainActivity.kt
├── data/
│   └── model/
│       └── MarkdownFile.kt
├── ui/
│   ├── components/
│   │   ├── MarkdownBlock.kt
│   │   ├── MarkdownHeading.kt
│   │   ├── MarkdownList.kt
│   │   ├── MarkdownQuote.kt
│   │   ├── MarkdownCodeBlock.kt
│   │   └── MarkdownParagraph.kt
│   └── screens/
│       ├── FileListScreen.kt
│       └── MarkdownViewerScreen.kt
└── parser/
    └── MarkdownParser.kt
```

---

## 📋 文件系统设计采纳的部分

### ✅ 采纳
- 使用SAF（Storage Access Framework），不申请全局存储权限
- Intent Filter支持三种场景：VIEW（打开）、SEND（分享）、MAIN（正常启动）
- 统一文件处理入口 handleIntent()
- 简化版DocumentResolver（仅支持content://）
- URI权限验证（防止路径遍历）
- 文件大小限制（防OOM）

### ❌ 暂不采纳
- MediaUri/FileUri Handler（过度设计）
- 最近文件列表（非必需）
- 图片资源处理（当前不需要）
- 内存缓存策略（过度优化）
- 第三方库（Markwon/Coil，已有自定义解析器）

---

## 🎯 实施原则

1. **先重构，再加功能** - 先完成结构拆分，再加新功能
2. **保持功能不变** - 重构期间不改变现有功能
3. **快速迭代** - 每个阶段可独立完成和验证
4. **简化优先** - 避免过度设计

---

**文档最后更新**: 2026-03-31
