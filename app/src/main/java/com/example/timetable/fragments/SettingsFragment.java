package com.example.timetable.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.example.timetable.R;
import com.example.timetable.utils.DbHelper;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        Preference resetPreference = findPreference("resetdata");
        if (resetPreference != null) {
            resetPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showResetConfirmation();
                    return true;
                }
            });
        }
    }

    private void showResetConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.reset_data)
                .setMessage(R.string.reset_warning)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DbHelper db = new DbHelper(getContext());
                        db.resetAllData();
                        Toast.makeText(getContext(), "Data reset successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}


