# 我的日记 - Material 3 日记应用

一个完全采用 Material Design 3 规范的精美日记应用，使用 Jetpack Compose 构建。

## ✨ 核心功能

### 📝 日记管理
- 创建、编辑、查看、删除日记
- 收藏重要日记
- 自动保存时间戳

### 🎭 心情记录
五种心情状态：😊开心 😢难过 🎉兴奋 😌平静 😐一般

### 🏷️ 标签系统
- 为日记添加多个标签
- 标签可用于搜索和分类

### 🔍 强大筛选
- 全文搜索（标题/内容/标签）
- 心情筛选
- 收藏筛选
- 组合筛选

## 🎨 设计亮点

### Material 3
- Dynamic Color (Android 12+)
- 深色模式自适应
- 完整 Typography 系统

### 流畅动画
- 页面转场动画
- FAB 弹簧效果
- 列表项位置过渡
- 收藏按钮缩放动画

## 🏗️ 技术架构

```
UI Layer (Compose)
    ↓
ViewModel (状态管理)
    ↓
Repository (数据抽象)
    ↓
Room Database (持久化)
```

### 技术栈
- **UI**: Jetpack Compose + Material 3
- **导航**: Navigation Compose
- **数据库**: Room + Kotlin Flow
- **架构**: MVVM
- **异步**: Kotlin Coroutines

## 📦 项目结构

```
com.example.xbjsb/
├── data/
│   ├── DiaryEntry.kt
│   ├── database/
│   │   ├── DiaryDao.kt
│   │   └── DiaryDatabase.kt
│   └── repository/
│       └── DiaryRepository.kt
├── viewmodel/
│   └── DiaryViewModel.kt
├── ui/
│   ├── screens/
│   │   ├── DiaryListScreen.kt
│   │   ├── DiaryEditScreen.kt
│   │   └── DiaryDetailScreen.kt
│   ├── components/
│   │   └── DiaryCard.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt
│   └── DiaryNavigation.kt
└── MainActivity.kt
```

## 🚀 构建运行

### 环境要求
- Android Studio Hedgehog+
- Kotlin 1.9.0+
- Android SDK 21+ (最低)
- 编译 SDK 35

### 构建步骤
```bash
./gradlew build
./gradlew installDebug
```

### 最佳体验
- 推荐 Android 12+ 设备（Dynamic Color）
- 真机或 API 31+ 模拟器

## 🎯 核心特性说明

### 1. 响应式数据流
使用 Kotlin Flow 实现自动 UI 更新：
```kotlin
val entries: StateFlow<List<DiaryEntry>>
```

### 2. 智能筛选
多条件组合筛选，实时响应：
- 搜索关键词
- 心情状态
- 收藏标记

### 3. Material 3 动画
- 300ms 页面转场
- 弹簧阻尼动画
- 流畅的状态过渡

## 📱 界面展示

### 列表页
- 卡片式日记列表
- 顶部搜索/筛选栏
- 浮动创建按钮
- 空状态引导

### 编辑页
- 标题/内容输入
- 心情选择器
- 标签管理
- 收藏开关

### 详情页
- 优雅阅读布局
- 日期/心情标识
- 标签显示
- 编辑/删除操作

## 🔮 扩展方向

- [ ] 图片插入
- [ ] 日记导出
- [ ] 云端同步
- [ ] 密码保护
- [ ] 日历视图
- [ ] 心情统计

---

**记录生活，感受美好 📝✨**