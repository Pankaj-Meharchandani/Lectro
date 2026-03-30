package com.example.timetable.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;

import com.example.timetable.fragments.SettingsFragment;
import com.example.timetable.R;

public class SettingsActivity extends AppCompatActivity {
    public static final String
            KEY_SEVEN_DAYS_SETTING = "sevendays";
    public static final String KEY_SCHOOL_WEBSITE_SETTING = "schoolwebsite";
    public static final String KEY_PERSONAL_DETAILS_SETTING = "personal_details_enabled";
    public static final String KEY_ATTENDANCE_SETTING = "attendance_enabled";
    public static final String KEY_MIN_ATTENDANCE_SETTING = "min_attendance";

    public static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    public static final String KEY_SCHEDULE_REMINDER = "schedule_reminder";
    public static final String KEY_ASSIGNMENT_REMINDER = "assignment_reminder";
    public static final String KEY_EXAM_REMINDER = "exam_reminder";
    public static final String KEY_ATTENDANCE_ALERT = "attendance_alert";
    public static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.action_settings);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }
}


