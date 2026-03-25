package com.example.timetable.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.example.timetable.R;

import java.util.Calendar;


public class DailyReceiver extends BroadcastReceiver {

    Context context;
    DbHelper db;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        NotificationHelper notificationHelper = new NotificationHelper(context);

        db = new DbHelper(context);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        String message = getLessons(day);
        notificationHelper.showScheduleNotification(message);

        // Also check for other upcoming events
        notificationHelper.checkAndNotifyUpcomingEvents();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getLessons(int day) {
        StringBuilder lessons = new StringBuilder("");
        String currentDay = getCurrentDay(day);

        db.getWeek(currentDay).forEach(week -> {
            if(week != null) {
                lessons.append(week.getSubject()).append(" ")
                        .append(week.getFromTime())
                        .append(" - ")
                        .append(week.getToTime()).append(" ")
                        .append(week.getRoom())
                        .append("\n");
            }
        });

        return !lessons.toString().equals("") ? lessons.toString() : context.getString(R.string.do_not_have_lessons);
    }

    private String getCurrentDay(int day) {
        String currentDay = null;
        switch (day) {
            case 1:
                currentDay = "Sunday";
                break;
            case 2:
                currentDay = "Monday";
                break;
            case 3:
                currentDay = "Tuesday";
                break;
            case 4:
                currentDay = "Wednesday";
                break;
            case 5:
                currentDay = "Thursday";
                break;
            case 6:
                currentDay = "Friday";
                break;
            case 7:
                currentDay = "Saturday";
                break;
        }
        return currentDay;
    }
}

