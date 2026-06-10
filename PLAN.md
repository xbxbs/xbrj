# Material 3 Expression 升级 - 实施计划

## ✅ 已完成的工作

### Phase 1: 设计系统基础
- ✅ DesignTokens.kt - 统一的设计令牌（间距、圆角、动画时长等）
- ✅ AnimationSpec.kt - Material 3 Expression 动效配置
- ✅ Color.kt - 升级颜色系统，柔和的渐变色
- ✅ BlurEffects.kt - 模糊效果工具（已创建但未使用）
- ✅ Theme.kt - 完整的深色模式配色方案

### Phase 2: 组件优化
- ✅ DiaryCard - 去除毛玻璃，纯色背景，自适应深色模式
- ✅ DiaryListScreen - 优化 FAB 动画、空状态页面
- ✅ 搜索栏 - Surface 容器 + 圆角 + 聚焦高亮
- ✅ 筛选器 - 独立 AssistChip，去掉丑陋的长方形背景
- ✅ DiaryEditScreen - 统一排版、卡片背景一致

### Phase 3: Bug 修复
- ✅ Repository.getEntryById() - 修复 Flow 数据获取
- ✅ 删除日记逻辑 - 立即关闭弹窗并返回
- ✅ 动画时长 - 针对 120Hz 屏幕优化（200-500ms）
- ✅ 收藏按钮动画 - 降低缩放到 1.15x

---

## 📋 修复清单

| 问题 | 状态 | 说明 |
|------|------|------|
| 点击卡片闪退 | ✅ | Flow.first() 导入和数据获取修复 |
| 筛选器长方形很丑 | ✅ | 改用 AssistChip + 圆角按钮 |
| 编辑页面排版丑 | ✅ | 统一间距、卡片背景、去阴影 |
| 卡片背景不统一 | ✅ | 都使用 surfaceVariant |
| 删除不退出 | ✅ | 移除 Snackbar 阻塞，立即返回 |
| 120Hz 动画僵硬 | ✅ | 延长到 400-500ms |
| 深色模式不适配 | ✅ | 完整的 darkColorScheme |

---

## 🎨 最终效果

### 列表页
- 智能 FAB（滚动时收起/展开）
- 精致的搜索栏（圆角容器）
- 干净的筛选指示器（独立芯片）
- 空状态脉动动画

### 编辑页
- 紧凑的 16dp 间距
- 统一的卡片背景
- 大圆角输入框
- 图标+文字保存按钮

### 详情页
- 删除后立即返回
- 自适应深色模式

---

## 构建命令

```bash
./gradlew assembleDebug
```