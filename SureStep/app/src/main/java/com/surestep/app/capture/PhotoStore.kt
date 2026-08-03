package com.surestep.app.capture

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Selfies live in app-private internal storage. They are not in MediaStore, so
 * they never appear in the gallery, and no other app can read them.
 */
@Singleton
class PhotoStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val photoDir: File
        get() = File(context.filesDir, DIR_NAME).apply { if (!exists()) mkdirs() }

    /**
     * Path for a new capture, named after the task and moment so the filename
     * alone reads like the record: `lock_door_20260803_113542.jpg`.
     */
    fun newPhotoFile(taskTitle: String, at: ZonedDateTime): File {
        val slug = taskTitle.lowercase(Locale.ROOT)
            .replace(NON_FILENAME_CHARS, "_")
            .trim('_')
            .take(32)
            .ifBlank { "task" }
        return File(photoDir, "${slug}_${at.format(FILE_TIMESTAMP)}.jpg")
    }

    fun delete(path: String?) {
        path ?: return
        runCatching { File(path).delete() }
    }

    /** Total bytes used by stored selfies, for the Settings screen. */
    fun usedBytes(): Long =
        photoDir.listFiles()?.sumOf { it.length() } ?: 0L

    /**
     * Removes photo files that no record points at any more — for instance if a
     * capture was interrupted after the file was created but before it was saved.
     */
    fun deleteOrphans(referencedPaths: Set<String>) {
        photoDir.listFiles()?.forEach { file ->
            if (file.absolutePath !in referencedPaths) file.delete()
        }
    }

    private companion object {
        const val DIR_NAME = "records"
        val NON_FILENAME_CHARS = Regex("[^a-z0-9]+")
        val FILE_TIMESTAMP: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.ROOT)
    }
}
