# 构建问题修复说明

## 问题
```
Starting in Kotlin 2.0, the Compose Compiler Gradle plugin is required
when compose is enabled.
```

## 解决方案

### 1. 根目录 build.gradle.kts
添加 Compose Compiler 插件：
```kotlin
plugins {
    id("com.android.application") version "8.9.1" apply false
    id("com.android.library") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false  // 新增
}
```

### 2. app/build.gradle.kts
应用插件并移除旧配置：
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")  // 新增
    id("kotlin-kapt")
}

android {
    // ... 其他配置
    
    buildFeatures {
        compose = true
    }
    
    // 移除这个配置块（插件会自动处理）
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "1.5.8"
    // }
}
```

## 原因
从 Kotlin 2.0 开始，Compose Compiler 从 Kotlin 编译器中分离出来，成为独立的 Gradle 插件。使用 Kotlin 2.0.21 时必须显式添加 `org.jetbrains.kotlin.plugin.compose` 插件。

## 现在可以构建了
```bash
./gradlew assembleDebug
```

✅ 问题已解决！