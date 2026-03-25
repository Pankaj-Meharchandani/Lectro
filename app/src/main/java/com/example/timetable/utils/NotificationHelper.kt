package com.example.timetable.utils

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.timetable.R
import com.example.timetable.activities.MainActivity
import com.example.timetable.activities.SettingsActivity
import com.example.timetable.model.Exam
import com.example.timetable.model.Homework
import com.example.timetable.model.Week
import java.text.SimpleDateFormat
import java.util.*

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        const val CHANNEL_ID_SCHEDULE = "schedule_reminders"
        const val CHANNEL_ID_ASSIGNMENTS = "assignment_reminders"
        const val CHANNEL_ID_EXAMS = "exam_reminders"
        const val CHANNEL_ID_ATTENDANCE = "attendance_alerts"

        const val NOTIFICATION_ID_SCHEDULE = 1001
        const val NOTIFICATION_ID_ASSIGNMENTS = 1002
        const val NOTIFICATION_ID_EXAMS = 1003
        const val NOTIFICATION_ID_ATTENDANCE = 1004

        const val ACTION_EVENT_ALARM = "com.example.timetable.ACTION_EVENT_ALARM"
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"

        const val TYPE_CLASS = "class"
        const val TYPE_EXAM = "exam"
        const val TYPE_ASSIGNMENT = "assignment"
        const val TYPE_ATTENDANCE = "attendance"
        const val TYPE_SCHEDULER = "scheduler"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(CHANNEL_ID_SCHEDULE, "Schedule Reminders", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_ID_ASSIGNMENTS, "Assignment Reminders", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_ID_EXAMS, "Exam Reminders", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_ID_ATTENDANCE, "Attendance Alerts", NotificationManager.IMPORTANCE_HIGH)
            )
            notificationManager.createNotificationChannels(channels)
        }
    }

    private fun areNotificationsEnabled(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return sharedPref.getBoolean(SettingsActivity.KEY_NOTIFICATIONS_ENABLED, false)
    }

    fun showScheduleNotification(message: String) {
        if (!areNotificationsEnabled() || !sharedPref.getBoolean(SettingsActivity.KEY_SCHEDULE_REMINDER, true)) return

        showNotification(
            CHANNEL_ID_SCHEDULE,
            NOTIFICATION_ID_SCHEDULE,
            context.getString(R.string.notification_title),
            message
        )
    }

    fun showAssignmentNotification(assignments: List<Homework>) {
        if (!areNotificationsEnabled() || !sharedPref.getBoolean(SettingsActivity.KEY_ASSIGNMENT_REMINDER, true) || assignments.isEmpty()) return

        val message = "You have ${assignments.size} upcoming assignments."
        val bigText = assignments.joinToString("\n") { "${it.subject}: ${it.title} (Due: ${it.date})" }

        showNotification(
            CHANNEL_ID_ASSIGNMENTS,
            NOTIFICATION_ID_ASSIGNMENTS,
            "Upcoming Assignments",
            message,
            bigText
        )
    }

    fun showExamNotification(exams: List<Exam>) {
        if (!areNotificationsEnabled() || !sharedPref.getBoolean(SettingsActivity.KEY_EXAM_REMINDER, true) || exams.isEmpty()) return

        val message = "You have ${exams.size} upcoming exams."
        val bigText = exams.joinToString("\n") { "${it.subject}: ${it.date} at ${it.time}" }

        showNotification(
            CHANNEL_ID_EXAMS,
            NOTIFICATION_ID_EXAMS,
            "Upcoming Exams",
            message,
            bigText
        )
    }

    fun showAttendanceAlert(subjectName: String, attendance: Double) {
        if (!areNotificationsEnabled() || !sharedPref.getBoolean(SettingsActivity.KEY_ATTENDANCE_ALERT, true)) return

        showNotification(
            CHANNEL_ID_ATTENDANCE,
            NOTIFICATION_ID_ATTENDANCE,
            "Low Attendance Alert",
            "Your attendance for $subjectName is low: ${String.format("%.1f", attendance)}%"
        )
    }

    fun showNotification(
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        bigText: String? = null
    ) {
        if (!areNotificationsEnabled()) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources,
                R.drawable.ic_launcher_foreground
            ))
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)

        if (bigText != null) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
        }

        notificationManager.notify(notificationId, builder.build())
    }

    fun scheduleAllReminders() {
        if (!areNotificationsEnabled()) return
        scheduleDailyScheduler()
        scheduleAttendanceAlert()
    }

    private fun scheduleDailyScheduler() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        scheduleAlarm(calendar.timeInMillis, TYPE_SCHEDULER, 0, "Scheduler", "Daily Alarm Scheduler")
    }

    private fun scheduleAttendanceAlert() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }
        scheduleAlarm(calendar.timeInMillis, TYPE_ATTENDANCE, 0, "Attendance Alert", "Check your weekly attendance.")
    }

    fun scheduleEventsForToday() {
        val db = DbHelper(context)
        val today = Calendar.getInstance()
        val dayName = SimpleDateFormat("EEEE", Locale.ENGLISH).format(today.time)

        // Class Reminders (15 mins before)
        if (sharedPref.getBoolean(SettingsActivity.KEY_SCHEDULE_REMINDER, true)) {
            db.getWeek(dayName).forEach { week ->
                val classTime = parseTimeToday(week.fromTime) ?: return@forEach
                val alarmTime = classTime.timeInMillis - 15 * 60 * 1000
                if (alarmTime > System.currentTimeMillis()) {
                    scheduleAlarm(alarmTime, TYPE_CLASS, week.id, "Class Reminder", "${week.subject} starts in 15 mins at ${week.room}")
                }
            }
        }

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)

        // Exam Reminders (1 hr before)
        if (sharedPref.getBoolean(SettingsActivity.KEY_EXAM_REMINDER, true)) {
            db.exam.filter { it.date == dateStr }.forEach { exam ->
                val examTime = parseDateTime(exam.date, exam.time) ?: return@forEach
                val alarmTime = examTime.timeInMillis - 60 * 60 * 1000
                if (alarmTime > System.currentTimeMillis()) {
                    scheduleAlarm(alarmTime, TYPE_EXAM, exam.id, "Upcoming Exam", "${exam.subject} at ${exam.time}")
                }
            }
        }

        // Assignment Reminders (8 AM on due date)
        if (sharedPref.getBoolean(SettingsActivity.KEY_ASSIGNMENT_REMINDER, true)) {
            db.homework.filter { it.date == dateStr && it.completed == 0 }.forEach { homework ->
                val alarmTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 8)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                if (alarmTime > System.currentTimeMillis()) {
                    scheduleAlarm(alarmTime, TYPE_ASSIGNMENT, homework.id, "Assignment Due", "${homework.subject}: ${homework.title}")
                }
            }
        }
    }

    private fun scheduleAlarm(timeInMillis: Long, type: String, id: Int, title: String, message: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_EVENT_ALARM
            putExtra(EXTRA_TYPE, type)
            putExtra(EXTRA_ID, id)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, (type + id).hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExact) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } else {
            // Fallback to inexact alarm if permission is missing
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        }
    }

    private fun parseTimeToday(timeStr: String?): Calendar? {
        if (timeStr == null) return null
        return try {
            val parts = timeStr.split(":")
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                set(Calendar.MINUTE, parts[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseDateTime(dateStr: String?, timeStr: String?): Calendar? {
        if (dateStr == null || timeStr == null) return null
        return try {
            val dateParts = dateStr.split("-")
            val timeParts = timeStr.split(":")
            Calendar.getInstance().apply {
                set(Calendar.YEAR, dateParts[0].toInt())
                set(Calendar.MONTH, dateParts[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, dateParts[2].toInt())
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun checkAndNotifyUpcomingEvents() {
        // This was for the daily morning summary, user said "no not every morning"
        // keeping it empty or removing it.
    }

    fun checkAttendanceAndNotify() {
        val db = DbHelper(context)
        val minAttendanceStr = sharedPref.getString(SettingsActivity.KEY_MIN_ATTENDANCE_SETTING, "75")
        val minAttendance = minAttendanceStr?.toDoubleOrNull() ?: 75.0
        val subjects = db.allSubjects
        subjects.forEach { subject ->
            val total = subject.attended + subject.missed
            if (total > 0) {
                val percentage = (subject.attended.toDouble() / total) * 100
                if (percentage < minAttendance) {
                    showAttendanceAlert(subject.name, percentage)
                }
            }
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
