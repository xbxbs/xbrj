package com.example.xbjsb.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.xbjsb.data.DiaryEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "diary_data")

class DiaryRepository(private val context: Context) {
    
    private val gson = Gson()
    private val ENTRIES_KEY = stringPreferencesKey("diary_entries")
    
    val allEntries: Flow<List<DiaryEntry>> = context.dataStore.data.map { preferences ->
        val json = preferences[ENTRIES_KEY] ?: "[]"
        val type = object : TypeToken<List<DiaryEntry>>() {}.type
        try {
            val entries: List<DiaryEntry> = gson.fromJson(json, type)
            entries.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            // 数据格式不兼容，返回空列表（数据迁移）
            emptyList()
        }
    }
    
    val favoriteEntries: Flow<List<DiaryEntry>> = allEntries.map { entries ->
        entries.filter { it.isFavorite }
    }
    
    val entryCount: Flow<Int> = allEntries.map { it.size }
    
    suspend fun getAllEntriesSnapshot(): List<DiaryEntry> {
        val json = context.dataStore.data.map { it[ENTRIES_KEY] ?: "[]" }.first()
        val type = object : TypeToken<List<DiaryEntry>>() {}.type
        return try {
            val entries: List<DiaryEntry> = gson.fromJson(json, type)
            entries.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun saveAll(entries: List<DiaryEntry>) {
        context.dataStore.edit { preferences ->
            preferences[ENTRIES_KEY] = gson.toJson(entries.sortedByDescending { it.timestamp })
        }
    }
    
    suspend fun replaceAll(entries: List<DiaryEntry>) {
        val currentEntries = getAllEntriesSnapshot()
        currentEntries.forEach { deleteImagesForEntry(it) }
        saveAll(entries)
    }
    
    suspend fun getEntryById(id: Long): DiaryEntry? {
        val json = context.dataStore.data.map { it[ENTRIES_KEY] ?: "[]" }.first()
        val type = object : TypeToken<List<DiaryEntry>>() {}.type
        return try {
            val entries: List<DiaryEntry> = gson.fromJson(json, type)
            entries.firstOrNull { it.id == id }
        } catch (e: Exception) {
            null
        }
    }
    
    fun searchEntries(query: String): Flow<List<DiaryEntry>> {
        return allEntries.map { entries ->
            if (query.isBlank()) {
                entries
            } else {
                entries.filter { entry ->
                    entry.title.contains(query, ignoreCase = true) ||
                    entry.content.contains(query, ignoreCase = true) ||
                    entry.tags.contains(query, ignoreCase = true)
                }
            }
        }
    }
    
    fun getEntriesByMood(mood: String): Flow<List<DiaryEntry>> {
        return allEntries.map { entries ->
            entries.filter { it.mood == mood }
        }
    }
    
    suspend fun insert(entry: DiaryEntry): Long {
        context.dataStore.edit { preferences ->
            val json = preferences[ENTRIES_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<DiaryEntry>>() {}.type
            val entries: MutableList<DiaryEntry> = try {
                gson.fromJson(json, type)
            } catch (e: Exception) {
                mutableListOf()
            }
            entries.add(entry)
            preferences[ENTRIES_KEY] = gson.toJson(entries)
        }
        return entry.id
    }
    
    suspend fun update(entry: DiaryEntry) {
        context.dataStore.edit { preferences ->
            val json = preferences[ENTRIES_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<DiaryEntry>>() {}.type
            val entries: MutableList<DiaryEntry> = try {
                gson.fromJson(json, type)
            } catch (e: Exception) {
                mutableListOf()
            }
            val index = entries.indexOfFirst { it.id == entry.id }
            if (index != -1) {
                entries[index] = entry
                preferences[ENTRIES_KEY] = gson.toJson(entries)
            }
        }
    }
    
    suspend fun delete(entry: DiaryEntry) {
        // 先删除关联的图片文件
        deleteImagesForEntry(entry)
        // 再删除数据库记录
        deleteById(entry.id)
    }
    
    suspend fun deleteById(id: Long) {
        context.dataStore.edit { preferences ->
            val json = preferences[ENTRIES_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<DiaryEntry>>() {}.type
            val entries: MutableList<DiaryEntry> = try {
                gson.fromJson(json, type)
            } catch (e: Exception) {
                mutableListOf()
            }
            
            // 找到要删除的 entry，清理图片
            val targetEntry = entries.find { it.id == id }
            if (targetEntry != null) {
                deleteImagesForEntry(targetEntry)
            }
            
            entries.removeIf { it.id == id }
            preferences[ENTRIES_KEY] = gson.toJson(entries)
        }
    }
    
    /**
     * 删除 entry 关联的所有图片文件
     */
    private fun deleteImagesForEntry(entry: DiaryEntry) {
        if (entry.images.isBlank()) return
        
        val imagePaths = entry.images.split(',').filter { it.isNotBlank() }
        imagePaths.forEach { path ->
            try {
                val file = java.io.File(path)
                if (file.exists() && file.isFile) {
                    file.delete()
                }
            } catch (e: Exception) {
                // 忽略删除失败，避免影响日记删除
                e.printStackTrace()
            }
        }
    }
}