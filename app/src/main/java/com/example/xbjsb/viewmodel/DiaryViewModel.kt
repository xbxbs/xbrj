package com.example.xbjsb.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.xbjsb.data.DiaryEntry
import com.example.xbjsb.data.repository.DiaryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: DiaryRepository = DiaryRepository(application)
    
    // UI State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedMood = MutableStateFlow<String?>(null)
    val selectedMood: StateFlow<String?> = _selectedMood.asStateFlow()
    
    private val _selectedGroup = MutableStateFlow<String?>(null)
    val selectedGroup: StateFlow<String?> = _selectedGroup.asStateFlow()
    
    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()
    
    // 草稿保存
    private val _draftTitle = MutableStateFlow("")
    val draftTitle: StateFlow<String> = _draftTitle.asStateFlow()
    
    private val _draftContent = MutableStateFlow("")
    val draftContent: StateFlow<String> = _draftContent.asStateFlow()
    
    private val _draftMood = MutableStateFlow<String?>(null)
    val draftMood: StateFlow<String?> = _draftMood.asStateFlow()
    
    private val _draftTags = MutableStateFlow<List<String>>(emptyList())
    val draftTags: StateFlow<List<String>> = _draftTags.asStateFlow()
    
    private val _hasDraft = MutableStateFlow(false)
    val hasDraft: StateFlow<Boolean> = _hasDraft.asStateFlow()
    
    // 搜索历史
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()
    
    // 用户自定义分组列表
    private val _customGroups = MutableStateFlow<List<String>>(emptyList())
    val customGroups: StateFlow<List<String>> = _customGroups.asStateFlow()
    
    // Combined flow for filtered entries
    val entries: StateFlow<List<DiaryEntry>> = combine(
        repository.allEntries,
        searchQuery,
        selectedMood,
        selectedGroup,
        showFavoritesOnly
    ) { allEntries, query, mood, group, favoritesOnly ->
        var filtered = allEntries
        
        if (favoritesOnly) {
            filtered = filtered.filter { it.isFavorite }
        }
        
        if (mood != null) {
            filtered = filtered.filter { it.mood == mood }
        }
        
        if (group != null) {
            filtered = filtered.filter { it.group == group }
        }
        
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true) ||
                it.tags.contains(query, ignoreCase = true)
            }
        }
        
        filtered
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    
    val allEntries: StateFlow<List<DiaryEntry>> = repository.allEntries.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )
    
    val entryCount: StateFlow<Int> = repository.entryCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setSelectedMood(mood: String?) {
        _selectedMood.value = mood
    }
    
    fun setSelectedGroup(group: String?) {
        _selectedGroup.value = group
    }
    
    fun toggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }
    
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedMood.value = null
        _selectedGroup.value = null
        _showFavoritesOnly.value = false
    }
    
    // 分组管理
    fun addCustomGroup(groupName: String) {
        if (groupName.isBlank() || _customGroups.value.contains(groupName)) return
        _customGroups.value = _customGroups.value + groupName
    }
    
    fun removeCustomGroup(groupName: String) {
        _customGroups.value = _customGroups.value.filter { it != groupName }
    }
    
    // 从所有日记中提取已使用的分组，不受当前筛选条件影响
    fun getUsedGroups(): List<String> {
        return allEntries.value
            .mapNotNull { it.group.takeIf { g -> g.isNotBlank() } }
            .distinct()
            .sorted()
    }
    
    suspend fun getEntryById(id: Long): DiaryEntry? {
        return repository.getEntryById(id)
    }
    
    fun insertEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.insert(entry)
        }
    }
    
    fun updateEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.update(entry)
        }
    }
    
    suspend fun deleteEntry(entry: DiaryEntry) {
        repository.delete(entry)
    }
    
    fun toggleFavorite(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.update(entry.copy(isFavorite = !entry.isFavorite))
        }
    }
    
    // 草稿管理
    fun saveDraft(title: String, content: String, mood: String?, tags: List<String>) {
        _draftTitle.value = title
        _draftContent.value = content
        _draftMood.value = mood
        _draftTags.value = tags
        _hasDraft.value = title.isNotBlank() || content.isNotBlank()
    }
    
    fun clearDraft() {
        _draftTitle.value = ""
        _draftContent.value = ""
        _draftMood.value = null
        _draftTags.value = emptyList()
        _hasDraft.value = false
    }
    
    fun restoreDraft(): DraftData {
        return DraftData(
            title = _draftTitle.value,
            content = _draftContent.value,
            mood = _draftMood.value,
            tags = _draftTags.value
        )
    }
    
    // 搜索历史管理
    fun addSearchHistory(query: String) {
        if (query.isBlank()) return
        val currentHistory = _searchHistory.value.toMutableList()
        currentHistory.remove(query) // 移除重复项
        currentHistory.add(0, query) // 添加到开头
        _searchHistory.value = currentHistory.take(10) // 最多保留10条
    }
    
    fun removeSearchHistory(query: String) {
        _searchHistory.value = _searchHistory.value.filter { it != query }
    }
    
    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }
}

data class DraftData(
    val title: String,
    val content: String,
    val mood: String?,
    val tags: List<String>
)