package com.surestep.app.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.surestep.app.MainActivity
import com.surestep.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun ensureChannel() {
        val manager = context.getSystemService<NotificationManager>() ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
            enableVibration(true)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * POST_NOTIFICATIONS only exists from API 33. Below that, notifications need
     * no runtime grant, so the constant is compared by name rather than gated on
     * a version check that would read as dead code on newer devices.
     */
    fun canPost(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * One notification per scheduled reminder, and the id is derived from the
     * reminder so a second post replaces the first rather than stacking. The
     * copy names what is unmarked once and then gets out of the way — it never
     * asks the user to go and check.
     */
    @SuppressLint("MissingPermission") // Guarded by canPost() on the first line.
    fun postReminder(reminderId: Long, title: String, body: String) {
        if (!canPost()) return
        ensureChannel()

        val contentIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_BASE + reminderId.toInt(), notification)
        }
    }

    private companion object {
        const val CHANNEL_ID = "surestep_reminders"
        const val NOTIFICATION_ID_BASE = 1000
    }
}
