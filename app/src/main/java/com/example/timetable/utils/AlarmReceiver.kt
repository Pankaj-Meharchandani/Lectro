package com.example.timetable.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(NotificationHelper.EXTRA_TYPE) ?: return
        val id = intent.getIntExtra(NotificationHelper.EXTRA_ID, -1)
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: ""
        val message = intent.getStringExtra(NotificationHelper.EXTRA_MESSAGE) ?: ""

        val notificationHelper = NotificationHelper(context)
        when (type) {
            NotificationHelper.TYPE_SCHEDULER -> {
                notificationHelper.scheduleEventsForToday()
                notificationHelper.scheduleAllReminders() // Reschedule for tomorrow
            }
            NotificationHelper.TYPE_CLASS -> {
                notificationHelper.showNotification(NotificationHelper.CHANNEL_ID_SCHEDULE, id, title, message)
            }
            NotificationHelper.TYPE_EXAM -> {
                notificationHelper.showNotification(NotificationHelper.CHANNEL_ID_EXAMS, id, title, message)
            }
            NotificationHelper.TYPE_ASSIGNMENT -> {
                notificationHelper.showNotification(NotificationHelper.CHANNEL_ID_ASSIGNMENTS, id, title, message)
            }
            NotificationHelper.TYPE_ATTENDANCE -> {
                notificationHelper.checkAttendanceAndNotify()
                notificationHelper.scheduleAllReminders() // Reschedule for next week
            }
        }
    }
}
