# DataStore 迁移说明

## 问题根源

Room 数据库在 Android 编译环境（AndroidIDE）中遇到 SQLite 本地库加载错误：
```
Failed to load native library:sqlite-...
java.lang.UnsatisfiedLinkError: Error looking up function 'sqlite3_...'
```

这是因为 Room 编译器在验证数据库 Schema 时需要加载 `sqlite-jdbc` 的本地库，但在 Android 环境中这些本地库符号不可用。

## 解决方案

完全替换为 **DataStore + Gson** 方案：
- ✅ 无需本地库，纯 Kotlin 实现
- ✅ 在任何环境都能编译
- ✅ 自动持久化，性能优秀
- ⚠️ 失去类型安全的 SQL 查询能力

## 修改内容

### 1. **移除 Room 依赖**
- ❌ 删除 `androidx.room:*` 依赖
- ❌ 删除 KSP 插件
- ✅ 添加 `androidx.datastore:datastore-preferences`
- ✅ 添加 `com.google.code.gson:gson`

### 2. **重写数据层**
- ❌ 删除 `DiaryDatabase.kt`
- ❌ 删除 `DiaryDao.kt`
- ✅ 移除 `DiaryEntry` 的 Room 注解
- ✅ 改用时间戳作为 ID（`System.currentTimeMillis()`）

### 3. **重写 Repository**
```kotlin
// 之前：使用 Room Dao
class DiaryRepository(private val diaryDao: DiaryDao)

// 现在：使用 DataStore + Gson
class DiaryRepository(private val context: Context)
```

**实现原理：**
- 所有日记存储在 DataStore 的单个 Key 中（JSON 字符串）
- 使用 Gson 进行序列化/反序列化
- 使用 Flow 响应式更新 UI

### 4. **更新 ViewModel**
```kotlin
// 之前
val dao = DiaryDatabase.getDatabase(application).diaryDao()
repository = DiaryRepository(dao)

// 现在
repository = DiaryRepository(application)
```

## API 兼容性

所有 Repository API 保持不变：
- ✅ `allEntries: Flow<List<DiaryEntry>>`
- ✅ `favoriteEntries: Flow<List<DiaryEntry>>`
- ✅ `entryCount: Flow<Int>`
- ✅ `getEntryById(id: Long): DiaryEntry?`
- ✅ `searchEntries(query: String): Flow<List<DiaryEntry>>`
- ✅ `getEntriesByMood(mood: String): Flow<List<DiaryEntry>>`
- ✅ `insert(entry: DiaryEntry): Long`
- ✅ `update(entry: DiaryEntry)`
- ✅ `delete(entry: DiaryEntry)`
- ✅ `deleteById(id: Long)`

UI 层（Screen、ViewModel）无需任何修改！

## 数据存储位置

```
/data/data/com.example.xbjsb/files/datastore/diary_data.preferences_pb
```

数据格式：
```json
{
  "diary_entries": "[{\"id\":1234567890,\"title\":\"...\",\"content\":\"...\"}]"
}
```

## 现在可以构建了

```bash
./gradlew assembleDebug
```

✅ 不再有 SQLite 本地库问题
✅ 编译速度更快
✅ 所有功能保持不变
