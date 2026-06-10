package com.example.xbjsb.data.backup

import com.example.xbjsb.data.DiaryEntry

data class DiaryBackup(
    val version: Int = 1,
    val app: String = "xbjsb",
    val createdAt: Long = System.currentTimeMillis(),
    val entryCount: Int = 0,
    val imageCount: Int = 0,
    val entries: List<DiaryEntry> = emptyList()
)

data class DiaryBackupSummary(
    val version: Int,
    val createdAt: Long,
    val entryCount: Int,
    val imageCount: Int
)

enum class RestoreMode {
    MERGE,
    REPLACE
}
