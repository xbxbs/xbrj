package com.example.xbjsb.data.backup

import android.content.Context
import android.net.Uri
import com.example.xbjsb.data.DiaryEntry
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class DiaryBackupManager(private val context: Context) {
    private val gson = Gson()

    suspend fun exportBackup(
        entries: List<DiaryEntry>,
        outputUri: Uri
    ): Result<DiaryBackupSummary> = runCatching {
        val imageMap = linkedMapOf<String, File>()
        val backupEntries = entries.map { entry ->
            val backupImagePaths = entry.getImageList().mapNotNull { path ->
                val file = File(path)
                if (!file.exists() || !file.isFile) return@mapNotNull null
                val zipPath = buildZipImagePath(entry.id, file, imageMap.size)
                imageMap[zipPath] = file
                zipPath
            }
            entry.copy(images = backupImagePaths.joinToString(","))
        }

        val backup = DiaryBackup(
            version = SUPPORTED_VERSION,
            app = APP_ID,
            createdAt = System.currentTimeMillis(),
            entryCount = backupEntries.size,
            imageCount = imageMap.size,
            entries = backupEntries
        )

        context.contentResolver.openOutputStream(outputUri)?.use { output ->
            ZipOutputStream(output.buffered()).use { zip ->
                zip.putNextEntry(ZipEntry(BACKUP_JSON))
                zip.write(gson.toJson(backup).toByteArray(Charsets.UTF_8))
                zip.closeEntry()

                imageMap.forEach { (zipPath, file) ->
                    zip.putNextEntry(ZipEntry(zipPath))
                    file.inputStream().use { input ->
                        input.copyTo(zip)
                    }
                    zip.closeEntry()
                }
            }
        } ?: error("无法写入备份文件")

        DiaryBackupSummary(
            version = backup.version,
            createdAt = backup.createdAt,
            entryCount = backup.entryCount,
            imageCount = backup.imageCount
        )
    }

    suspend fun readBackupSummary(inputUri: Uri): Result<DiaryBackupSummary> = runCatching {
        val backup = readBackup(inputUri)
        DiaryBackupSummary(
            version = backup.version,
            createdAt = backup.createdAt,
            entryCount = backup.entryCount,
            imageCount = backup.imageCount
        )
    }

    suspend fun restoreBackup(
        inputUri: Uri,
        currentEntries: List<DiaryEntry>,
        mode: RestoreMode
    ): Result<List<DiaryEntry>> = runCatching {
        val backup = readBackup(inputUri)
        validateBackup(backup)

        val currentIds = currentEntries.map { it.id }.toSet()
        val entriesToRestore = when (mode) {
            RestoreMode.REPLACE -> backup.entries
            RestoreMode.MERGE -> backup.entries.filterNot { it.id in currentIds }
        }

        val restoredEntries = restoreImagesForEntries(inputUri, entriesToRestore)
        val finalEntries = when (mode) {
            RestoreMode.REPLACE -> restoredEntries
            RestoreMode.MERGE -> currentEntries + restoredEntries
        }

        finalEntries.sortedByDescending { it.timestamp }
    }

    private fun readBackup(inputUri: Uri): DiaryBackup {
        context.contentResolver.openInputStream(inputUri)?.use { input ->
            ZipInputStream(input.buffered()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name == BACKUP_JSON) {
                        val json = zip.readBytes().toString(Charsets.UTF_8)
                        val backup = gson.fromJson(json, DiaryBackup::class.java)
                            ?: error("备份文件格式无效")
                        validateBackup(backup)
                        return backup
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: error("无法读取备份文件")
        error("备份文件格式无效：缺少 $BACKUP_JSON")
    }

    private fun validateBackup(backup: DiaryBackup) {
        if (backup.app != APP_ID) error("不是有效的拾光札记备份文件")
        if (backup.version > SUPPORTED_VERSION) error("备份版本过高，请升级应用后再恢复")
    }

    private fun restoreImagesForEntries(
        inputUri: Uri,
        entries: List<DiaryEntry>
    ): List<DiaryEntry> {
        if (entries.isEmpty()) return entries

        val neededImagePaths = entries
            .flatMap { it.getImageList() }
            .filter { it.startsWith(IMAGE_DIR) }
            .toSet()
        if (neededImagePaths.isEmpty()) return entries

        val restoredPathMap = mutableMapOf<String, String>()
        val outputDir = getRestoredImageDir()

        context.contentResolver.openInputStream(inputUri)?.use { input ->
            ZipInputStream(input.buffered()).use { zip ->
                var zipEntry = zip.nextEntry
                while (zipEntry != null) {
                    val zipPath = zipEntry.name
                    if (!zipEntry.isDirectory && zipPath in neededImagePaths) {
                        runCatching {
                            val outputFile = File(outputDir, buildRestoredFileName(zipPath))
                            FileOutputStream(outputFile).use { output ->
                                zip.copyTo(output)
                            }
                            restoredPathMap[zipPath] = outputFile.absolutePath
                        }
                    }
                    zip.closeEntry()
                    zipEntry = zip.nextEntry
                }
            }
        } ?: error("无法读取备份文件")

        return entries.map { entry ->
            val restoredImages = entry.getImageList().mapNotNull { imagePath ->
                if (imagePath.startsWith(IMAGE_DIR)) restoredPathMap[imagePath] else imagePath
            }
            entry.copy(images = restoredImages.joinToString(","))
        }
    }

    private fun getRestoredImageDir(): File {
        return File(context.filesDir, "images").apply {
            if (!exists()) mkdirs()
        }
    }

    private fun buildZipImagePath(entryId: Long, file: File, index: Int): String {
        val safeName = file.name.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return "$IMAGE_DIR/${entryId}_${index}_$safeName"
    }

    private fun buildRestoredFileName(zipPath: String): String {
        val name = zipPath.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"), "_")
        return "restored_${System.currentTimeMillis()}_${UUID.randomUUID()}_$name"
    }

    companion object {
        private const val APP_ID = "xbjsb"
        private const val SUPPORTED_VERSION = 1
        private const val BACKUP_JSON = "backup.json"
        private const val IMAGE_DIR = "images"
    }
}