package com.surestep.app.export

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.surestep.app.data.repository.LogRepository
import com.surestep.app.domain.model.TaskLog
import com.surestep.app.ui.components.Formatters
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class ExportFormat(val extension: String, val mimeType: String) {
    CSV("csv", "text/csv"),
    PDF("pdf", "application/pdf"),
}

sealed interface ExportResult {
    data class Success(val file: File, val recordCount: Int) : ExportResult
    data object NoRecords : ExportResult
    data class Failed(val message: String) : ExportResult
}

/**
 * Writes the full history to a file inside app storage. Nothing is uploaded and
 * nothing is shared automatically — the file sits on the device until the user
 * picks it up with [shareIntent].
 */
@Singleton
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logRepository: LogRepository,
) {
    private val exportDir: File
        get() = File(context.filesDir, "exports").apply { if (!exists()) mkdirs() }

    suspend fun export(format: ExportFormat): ExportResult = withContext(Dispatchers.IO) {
        val logs = logRepository.getAllForExport()
        if (logs.isEmpty()) return@withContext ExportResult.NoRecords

        runCatching {
            val stamp = LocalDateTime.now().format(FILE_STAMP)
            val file = File(exportDir, "surestep_history_$stamp.${format.extension}")
            when (format) {
                ExportFormat.CSV -> writeCsv(logs, file)
                ExportFormat.PDF -> writePdf(logs, file)
            }
            ExportResult.Success(file, logs.size)
        }.getOrElse { error ->
            ExportResult.Failed(error.message ?: "Export failed")
        }
    }

    /** Hands the file to Android's share sheet; the user chooses the destination. */
    fun shareIntent(file: File, format: ExportFormat): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = format.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "SureStep history")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            "Share history export",
        )
    }

    fun clearExports() {
        exportDir.listFiles()?.forEach { it.delete() }
    }

    // --- CSV ---------------------------------------------------------------

    private fun writeCsv(logs: List<TaskLog>, file: File) {
        file.bufferedWriter().use { writer ->
            writer.appendLine(CSV_HEADER)
            logs.forEach { log ->
                val at = log.recordedAt
                writer.appendLine(
                    listOf(
                        log.taskTitle,
                        Formatters.machineTimestamp(at),
                        log.zoneId,
                        log.recordedAtMillis.toString(),
                        log.latitude?.let { String.format(Locale.ROOT, "%.6f", it) }.orEmpty(),
                        log.longitude?.let { String.format(Locale.ROOT, "%.6f", it) }.orEmpty(),
                        log.accuracyMeters?.toInt()?.toString().orEmpty(),
                        log.address.orEmpty(),
                        log.deviceModel.orEmpty(),
                        log.batteryPercent?.toString().orEmpty(),
                        log.networkSummary.orEmpty(),
                        log.photoPath?.let { File(it).name }.orEmpty(),
                        log.notes.orEmpty(),
                    ).joinToString(",") { it.csvEscaped() },
                )
            }
        }
    }

    /**
     * RFC 4180 quoting. A note containing a comma, a quote, or a newline would
     * otherwise shift every later column in a spreadsheet.
     */
    private fun String.csvEscaped(): String =
        if (any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + replace("\"", "\"\"") + "\""
        } else {
            this
        }

    // --- PDF ---------------------------------------------------------------

    private fun writePdf(logs: List<TaskLog>, file: File) {
        val document = PdfDocument()
        val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
        val headingPaint = Paint().apply { textSize = 12f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 10f }
        val mutedPaint = Paint().apply { textSize = 9f; color = 0xFF666666.toInt() }

        var pageNumber = 1
        var page = document.startPage(pageInfo(pageNumber))
        var canvas = page.canvas
        var y = MARGIN + 24f

        canvas.drawText("SureStep — task history", MARGIN, y, titlePaint)
        y += 18f
        canvas.drawText(
            "${logs.size} records · exported ${Formatters.date(java.time.LocalDate.now())}",
            MARGIN,
            y,
            mutedPaint,
        )
        y += 24f

        logs.forEach { log ->
            val lines = pdfLinesFor(log)
            val blockHeight = ENTRY_TITLE_HEIGHT + lines.size * LINE_HEIGHT + ENTRY_GAP

            if (y + blockHeight > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(pageInfo(pageNumber))
                canvas = page.canvas
                y = MARGIN + 16f
            }

            canvas.drawText(log.taskTitle, MARGIN, y, headingPaint)
            y += ENTRY_TITLE_HEIGHT
            lines.forEach { line ->
                canvas.drawText(line, MARGIN + 8f, y, bodyPaint)
                y += LINE_HEIGHT
            }
            y += ENTRY_GAP
        }

        document.finishPage(page)
        file.outputStream().use { document.writeTo(it) }
        document.close()
    }

    private fun pdfLinesFor(log: TaskLog): List<String> = buildList {
        val at = log.recordedAt
        add("${Formatters.date(at)} at ${Formatters.preciseTime(at)}")
        if (log.hasLocation) {
            add("Location: ${Formatters.coordinates(log.latitude!!, log.longitude!!)}")
            log.address?.let { add("Address: $it") }
        }
        val device = listOfNotNull(
            log.deviceModel,
            log.batteryPercent?.let { "battery $it%" },
            log.networkSummary,
        )
        if (device.isNotEmpty()) add("Device: ${device.joinToString(" · ")}")
        log.photoPath?.let { add("Photo: ${File(it).name}") }
        log.notes?.takeIf { it.isNotBlank() }?.let { add("Notes: $it") }
    }

    private fun pageInfo(pageNumber: Int) =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNumber).create()

    private companion object {
        // A4 at 72dpi.
        const val PAGE_WIDTH = 595f
        const val PAGE_HEIGHT = 842f
        const val MARGIN = 40f
        const val LINE_HEIGHT = 13f
        const val ENTRY_TITLE_HEIGHT = 16f
        const val ENTRY_GAP = 8f

        const val CSV_HEADER =
            "task,recorded_at_local,time_zone,epoch_millis,latitude,longitude," +
                "accuracy_metres,address,device,battery_percent,network,photo_file,notes"

        val FILE_STAMP: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.ROOT)
    }
}
