package com.example.timetable.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Objects;

public class WakeUpAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), "android.intent.action.BOOT_COMPLETED") ||
            Objects.equals(intent.getAction(), "android.intent.action.QUICKBOOT_POWERON")) {
            scheduleAlarm(context);
        }
    }

    public static void scheduleAlarm(Context context) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.scheduleAllReminders();
        notificationHelper.scheduleEventsForToday();
    }

    public static void cancelAlarm(Context context) {
        // Implementation for canceling all alarms if needed
        // For now, let's keep it simple.
    }
}

