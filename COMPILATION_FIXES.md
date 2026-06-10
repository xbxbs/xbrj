# 编译错误修复记录

## 已修复的错误

### 1. ViewModel 初始化错误
**错误信息：**
```
Variable 'repository' must be initialized.
```

**原因：** `repository` 声明为 `lateinit` 但在 init 块中初始化，导致在属性初始化时无法访问。

**修复：** 直接在声明时初始化
```kotlin
// 之前
private val repository: DiaryRepository
init {
    repository = DiaryRepository(application)
}

// 现在
private val repository: DiaryRepository = DiaryRepository(application)
```

---

### 2. DiaryCard 协程作用域错误
**错误信息：**
```
Unresolved reference 'launch'.
Suspend function 'delay' should be called only from a coroutine.
```

**原因：** 使用了已废弃的 `GlobalScope.launch`，且在 Composable 中未获取协程作用域。

**修复：** 使用 `rememberCoroutineScope()`
```kotlin
// 之前
kotlinx.coroutines.GlobalScope.launch {
    kotlinx.coroutines.delay(300)
    isFavoriteAnimating = false
}

// 现在
val scope = rememberCoroutineScope()
scope.launch {
    kotlinx.coroutines.delay(300)
    isFavoriteAnimating = false
}
```

---

### 3. DiaryEditScreen InputChip 重复参数
**错误信息：**
```
@Composable invocations can only happen from the context of a @Composable function
Argument already passed for this parameter.
```

**原因：** `InputChip` 有两个 `onClick` 参数，第一个为空，第二个在 `modifier` 后重复定义。

**修复：** 合并为一个 `onClick` 参数
```kotlin
// 之前
InputChip(
    selected = false,
    onClick = { },
    label = { Text("#$tag") },
    trailingIcon = { ... },
    modifier = Modifier,
    onClick = { /* 删除逻辑 */ }
)

// 现在
InputChip(
    selected = false,
    onClick = { /* 删除逻辑 */ },
    label = { Text("#$tag") },
    trailingIcon = { ... }
)
```

---

### 4. DiaryListScreen 实验性 API 警告
**错误信息：**
```
This foundation API is experimental and is likely to change or be removed in the future.
```

**原因：** `animateItemPlacement()` 是实验性 API，需要显式 opt-in。

**修复：** 添加 `@OptIn(ExperimentalFoundationApi::class)`
```kotlin
// 之前
@OptIn(ExperimentalMaterial3Api::class)

// 现在
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
import androidx.compose.foundation.ExperimentalFoundationApi
```

---

## 总结

所有编译错误已修复，项目现在应该能够成功编译。这些错误主要涉及：
- Kotlin 属性初始化顺序
- Compose 协程作用域管理
- Compose API 参数正确使用
- 实验性 API 的 opt-in 注解

现在可以运行 `./gradlew assembleDebug` 进行构建了！✅
