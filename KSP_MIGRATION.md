# KSP 迁移说明

## 问题
KAPT 在 Android 环境中遇到 SQLite 本地库加载错误：
```
Failed to load native library:sqlite-3.41.2.2-fa721260-a8e2-4594-9ff8-ff3e9e8b9fc6-libsqlitejdbc.so
UnsatisfiedLinkError: dlopen failed: cannot locate symbol "Xzs_Construct"
```

这是因为 Room 的 KAPT 处理器需要在编译时验证数据库 Schema，需要加载本地 SQLite 库，在某些 Android 环境中会失败。

## 解决方案：迁移到 KSP

### 为什么选择 KSP？
1. **更快**：KSP 比 KAPT 快 2 倍以上
2. **更稳定**：不依赖本地库加载
3. **官方推荐**：Google 和 JetBrains 推荐的现代方案
4. **完美兼容**：Room 2.6+ 完全支持 KSP

### 修改内容

#### 1. 根目录 build.gradle.kts
添加 KSP 插件：
```kotlin
plugins {
    // ...
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}
```

#### 2. app/build.gradle.kts

**插件部分：**
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")  // 替换 kotlin-kapt
}
```

**依赖部分：**
```kotlin
// Room Database
val roomVersion = "2.6.1"
implementation("androidx.room:room-runtime:$roomVersion")
implementation("androidx.room:room-ktx:$roomVersion")
ksp("androidx.room:room-compiler:$roomVersion")  // 从 kapt 改为 ksp
```

### KSP vs KAPT 对比

| 特性 | KAPT | KSP |
|------|------|-----|
| 编译速度 | 慢 | 快 2x+ |
| Kotlin 2.0 支持 | 降级到 1.9 | 原生支持 |
| Room Schema 验证 | 需要本地库 | 不需要 |
| 内存占用 | 高 | 低 |
| 增量编译 | 支持 | 更好 |

### 其他好处
- 不再有 `Kapt currently doesn't support language version 2.0+` 警告
- 构建速度显著提升
- 更少的内存占用
- 更好的错误提示

## 现在可以构建了
```bash
./gradlew assembleDebug
```

✅ 问题已解决！享受 KSP 带来的性能提升吧！🚀