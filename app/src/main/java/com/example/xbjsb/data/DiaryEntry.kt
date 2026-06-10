package com.example.xbjsb.data

import java.util.Date

data class DiaryEntry(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mood: String = "neutral", // happy, sad, excited, calm, neutral
    val tags: String = "", // comma-separated tags
    val isFavorite: Boolean = false,
    val images: String = "", // comma-separated image paths
    val group: String = "" // 分组名称
) {
    fun getFormattedDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm", java.util.Locale.CHINA)
        return sdf.format(Date(timestamp))
    }
    
    fun getTagList(): List<String> {
        return tags.split(",").filter { it.isNotBlank() }
    }
    
    fun getImageList(): List<String> {
        return images.split(",").filter { it.isNotBlank() }
    }
}
