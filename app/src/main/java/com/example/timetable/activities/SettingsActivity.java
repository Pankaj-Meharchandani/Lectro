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


