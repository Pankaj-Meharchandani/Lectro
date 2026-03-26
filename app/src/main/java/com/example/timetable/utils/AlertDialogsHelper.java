package com.example.timetable.utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.timetable.adapters.ExamsAdapter;
import com.example.timetable.adapters.FragmentsTabAdapter;
import com.example.timetable.adapters.HomeworksAdapter;
import com.example.timetable.adapters.NotesAdapter;
import com.example.timetable.adapters.TeachersAdapter;
import com.example.timetable.adapters.WeekAdapter;
import com.example.timetable.model.Exam;
import com.example.timetable.model.Homework;
import com.example.timetable.model.Note;
import com.example.timetable.model.Teacher;
import com.example.timetable.model.Week;
import com.example.timetable.R;
import com.example.timetable.utils.TimeUtils;

import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog;
import com.github.dhaval2404.colorpicker.listener.ColorListener;
import com.github.dhaval2404.colorpicker.model.ColorShape;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class AlertDialogsHelper {

    public static void getEditSubjectDialog(final Activity activity, final View alertLayout, final ArrayList<Week> adapter, final ListView listView, int position) {
        final DbHelper dbHelper = new DbHelper(activity);
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final AutoCompleteTextView subject = alertLayout.findViewById(R.id.subject_dialog);
        editTextHashs.put(R.string.subject, subject);
        final AutoCompleteTextView teacher = alertLayout.findViewById(R.id.teacher_dialog);
        editTextHashs.put(R.string.teacher, teacher);

        final ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, dbHelper.getSubjectsList());
        subject.setAdapter(subjectAdapter);
        subject.setThreshold(1);

        final ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, dbHelper.getTeachersList());
        teacher.setAdapter(teacherAdapter);
        teacher.setThreshold(1);

        final EditText room = alertLayout.findViewById(R.id.room_dialog);
        editTextHashs.put(R.string.room, room);
        final TextView from_time = alertLayout.findViewById(R.id.from_time);
        final TextView to_time = alertLayout.findViewById(R.id.to_time);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Week week = adapter.get(position);

        subject.setText(week.getSubject());
        teacher.setText(week.getTeacher());
        room.setText(week.getRoom());
        from_time.setText(TimeUtils.formatTo12Hour(week.getFromTime()));
        to_time.setText(TimeUtils.formatTo12Hour(week.getToTime()));
        select_color.setBackgroundColor(week.getColor() != 0 ? week.getColor() : Color.WHITE);

        subject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = (String) parent.getItemAtPosition(position);
                Week details = dbHelper.getSubjectDetails(selectedSubject);
                if (details != null) {
                    teacher.setText(details.getTeacher());
                    room.setText(details.getRoom());
                    select_color.setBackgroundColor(details.getColor());
                }
            }
        });

        from_time.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                kotlin.Pair<Integer, Integer> time = TimeUtils.parse24Hour(week.getFromTime());
                int mHour = time != null ? time.getFirst() : Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int mMinute = time != null ? time.getSecond() : Calendar.getInstance().get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String time24 = TimeUtils.get24HourString(hourOfDay, minute);
                                from_time.setText(TimeUtils.formatTo12Hour(time24));
                                week.setFromTime(time24);
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.setTitle(R.string.choose_time);
                timePickerDialog.show();
            }
        });

        to_time.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                kotlin.Pair<Integer, Integer> time = TimeUtils.parse24Hour(week.getToTime());
                int hour = time != null ? time.getFirst() : Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int minute = time != null ? time.getSecond() : Calendar.getInstance().get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String time24 = TimeUtils.get24HourString(hourOfDay, minute);
                                to_time.setText(TimeUtils.formatTo12Hour(time24));
                                week.setToTime(time24);
                            }
                        }, hour, minute, false);
                timePickerDialog.setTitle(R.string.choose_time);
                timePickerDialog.show();
            }
        });

        select_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialColorPickerDialog.Builder(activity)
                        .setTitle(R.string.color_picker_default_title)
                        .setColorRes(activity.getResources().getIntArray(R.array.default_colors))
                        .setColorShape(ColorShape.SQAURE)
                        .setColorListener((color, colorHex) -> {
                            select_color.setBackgroundColor(color);
                        })
                        .show();
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.edit_subject);
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(subject.getText()) || TextUtils.isEmpty(teacher.getText()) || TextUtils.isEmpty(room.getText())) {
                    for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                        if(TextUtils.isEmpty(entry.getValue().getText())) {
                            entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                            entry.getValue().requestFocus();
                        }
                    }
                } else if(!from_time.getText().toString().matches(".*\\d+.*") || !to_time.getText().toString().matches(".*\\d+.*")) {
                    Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
                } else {
                    DbHelper db = new DbHelper(activity);
                    String from = week.getFromTime();
                    String to = week.getToTime();
                    
                    if (from != null && to != null && from.compareTo(to) >= 0) {
                        Snackbar.make(alertLayout, "Start time must be before end time", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    ArrayList<Week> existing = db.getWeek(week.getFragment());
                    for (Week ex : existing) {
                        if (ex.getId() != week.getId()) {
                            if (from.compareTo(ex.getToTime()) < 0 && to.compareTo(ex.getFromTime()) > 0) {
                                Snackbar.make(alertLayout, "Time clashes with: " + ex.getSubject(), Snackbar.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }

                    WeekAdapter weekAdapter = (WeekAdapter) listView.getAdapter(); // In order to get notifyDataSetChanged() method.
                    ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                    week.setSubject(subject.getText().toString());
                    week.setTeacher(teacher.getText().toString());
                    week.setRoom(room.getText().toString());
                    week.setColor(buttonColor.getColor());
                    db.updateWeek(week);
                    weekAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });
    }

    public static void getCreateSubjectDialog(final Activity activity, final View alertLayout, final Runnable onSaveCallback) {
        final DbHelper dbHelper = new DbHelper(activity);
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final AutoCompleteTextView subject = alertLayout.findViewById(R.id.subject_dialog);
        editTextHashs.put(R.string.subject, subject);
        final AutoCompleteTextView teacher = alertLayout.findViewById(R.id.teacher_dialog);
        editTextHashs.put(R.string.teacher, teacher);

        final ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, dbHelper.getSubjectsList());
        subject.setAdapter(subjectAdapter);
        subject.setThreshold(1);

        final ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, dbHelper.getTeachersList());
        teacher.setAdapter(teacherAdapter);
        teacher.setThreshold(1);

        final EditText room = alertLayout.findViewById(R.id.room_dialog);
        editTextHashs.put(R.string.room, room);
        final Button select_color = alertLayout.findViewById(R.id.select_color);

        subject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = (String) parent.getItemAtPosition(position);
                Week details = dbHelper.getSubjectDetails(selectedSubject);
                if (details != null) {
                    teacher.setText(details.getTeacher());
                    room.setText(details.getRoom());
                    select_color.setBackgroundColor(details.getColor());
                }
            }
        });

        select_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialColorPickerDialog.Builder(activity)
                        .setTitle(R.string.color_picker_default_title)
                        .setColorRes(activity.getResources().getIntArray(R.array.default_colors))
                        .setColorShape(ColorShape.SQAURE)
                        .setColorListener((color, colorHex) -> {
                            select_color.setBackgroundColor(color);
                        })
                        .show();
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.add_subject);
        alert.setCancelable(false);
        Button cancel = alertLayout.findViewById(R.id.cancel);
        Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(subject.getText()) || TextUtils.isEmpty(teacher.getText()) || TextUtils.isEmpty(room.getText())) {
                    for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                        if(TextUtils.isEmpty(entry.getValue().getText())) {
                            entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                            entry.getValue().requestFocus();
                        }
                    }
                } else {
                    ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                    dbHelper.insertSubject(subject.getText().toString(), buttonColor.getColor(), teacher.getText().toString(), room.getText().toString());
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    subject.getText().clear();
                    teacher.getText().clear();
                    room.getText().clear();
                    select_color.setBackgroundColor(Color.WHITE);
                    subject.requestFocus();
                    dialog.dismiss();
                }
            }
        });
    }

    public static void getAddSubjectDialog(final Activity activity, final View alertLayout, final FragmentsTabAdapter adapter, final ViewPager viewPager, final Runnable onSaveCallback) {
        final DbHelper dbHelper = new DbHelper(activity);
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final AutoCompleteTextView subject = alertLayout.findViewById(R.id.subject_dialog);
        editTextHashs.put(R.string.subject, subject);
        final AutoCompleteTextView teacher = alertLayout.findViewById(R.id.teacher_dialog);
        editTextHashs.put(R.string.teacher, teacher);

        final ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, dbHelper.getSubjectsList());
        subject.setAdapter(subjectAdapter);
        subject.setThreshold(1);

        final ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, dbHelper.getTeachersList());
        teacher.setAdapter(teacherAdapter);
        teacher.setThreshold(1);

        final EditText room = alertLayout.findViewById(R.id.room_dialog);
        editTextHashs.put(R.string.room, room);
        final TextView from_time = alertLayout.findViewById(R.id.from_time);
        final TextView to_time = alertLayout.findViewById(R.id.to_time);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Week week = new Week();

        subject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = (String) parent.getItemAtPosition(position);
                Week details = dbHelper.getSubjectDetails(selectedSubject);
                if (details != null) {
                    teacher.setText(details.getTeacher());
                    room.setText(details.getRoom());
                    select_color.setBackgroundColor(details.getColor());
                }
            }
        });

        from_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kotlin.Pair<Integer, Integer> time = TimeUtils.parse24Hour(week.getFromTime());
                int mHour = time != null ? time.getFirst() : Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int mMinute = time != null ? time.getSecond() : Calendar.getInstance().get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String time24 = TimeUtils.get24HourString(hourOfDay, minute);
                                from_time.setText(TimeUtils.formatTo12Hour(time24));
                                week.setFromTime(time24);
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.setTitle(R.string.choose_time);
                timePickerDialog.show(); }});

        to_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kotlin.Pair<Integer, Integer> time = TimeUtils.parse24Hour(week.getToTime());
                int hour = time != null ? time.getFirst() : Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int minute = time != null ? time.getSecond() : Calendar.getInstance().get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String time24 = TimeUtils.get24HourString(hourOfDay, minute);
                                to_time.setText(TimeUtils.formatTo12Hour(time24));
                                week.setToTime(time24);
                            }
                        }, hour, minute, false);
                timePickerDialog.setTitle(R.string.choose_time);
                timePickerDialog.show();
            }
        });

        select_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialColorPickerDialog.Builder(activity)
                        .setTitle(R.string.color_picker_default_title)
                        .setColorRes(activity.getResources().getIntArray(R.array.default_colors))
                        .setColorShape(ColorShape.SQAURE)
                        .setColorListener((color, colorHex) -> {
                            select_color.setBackgroundColor(color);
                        })
                        .show();
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.add_subject);
        alert.setCancelable(false);
        Button cancel = alertLayout.findViewById(R.id.cancel);
        Button submit = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(subject.getText()) || TextUtils.isEmpty(teacher.getText()) || TextUtils.isEmpty(room.getText())) {
                    for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                        if(TextUtils.isEmpty(entry.getValue().getText())) {
                            entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                            entry.getValue().requestFocus();
                        }
                    }
                } else if(!from_time.getText().toString().matches(".*\\d+.*") || !to_time.getText().toString().matches(".*\\d+.*")) {
                    Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
                } else {
                    String from = week.getFromTime();
                    String to = week.getToTime();

                    if (from != null && to != null && from.compareTo(to) >= 0) {
                        Snackbar.make(alertLayout, "Start time must be before end time", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    if (adapter != null && viewPager != null) {
                        String fragmentName = adapter.getItem(viewPager.getCurrentItem()).getClass().getSimpleName();
                        if (fragmentName.endsWith("Fragment")) {
                            fragmentName = fragmentName.substring(0, fragmentName.length() - "Fragment".length());
                        }

                        ArrayList<Week> existing = dbHelper.getWeek(fragmentName);
                        for (Week ex : existing) {
                            if (from != null && to != null && from.compareTo(ex.getToTime()) < 0 && to.compareTo(ex.getFromTime()) > 0) {
                                Snackbar.make(alertLayout, "Time clashes with: " + ex.getSubject(), Snackbar.LENGTH_LONG).show();
                                return;
                            }
                        }

                        week.setSubject(subject.getText().toString());
                        week.setFragment(fragmentName);
                        week.setTeacher(teacher.getText().toString());
                        week.setRoom(room.getText().toString());
                        ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                        week.setColor(buttonColor.getColor());
                        dbHelper.insertWeek(week);
                        adapter.notifyDataSetChanged();
                    } else {
                        ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                        dbHelper.insertSubject(subject.getText().toString(), buttonColor.getColor(), teacher.getText().toString(), room.getText().toString());
                    }
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    subject.getText().clear();
                    teacher.getText().clear();
                    room.getText().clear();
                    from_time.setText(R.string.select_time);
                    to_time.setText(R.string.select_time);
                    select_color.setBackgroundColor(Color.WHITE);
                    subject.requestFocus();

                    // Update autocomplete adapters
                    subjectAdapter.clear();
                    subjectAdapter.addAll(dbHelper.getSubjectsList());
                    subjectAdapter.notifyDataSetChanged();
                    teacherAdapter.clear();
                    teacherAdapter.addAll(dbHelper.getTeachersList());
                    teacherAdapter.notifyDataSetChanged();

                    dialog.dismiss();
                }
            }
        });
    }

    public static void getEditHomeworkDialog(final Activity activity, final View alertLayout, final ArrayList<Homework> adapter, final ListView listView, int listposition) {
        final DbHelper dbHelper = new DbHelper(activity);
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final AutoCompleteTextView subject = alertLayout.findViewById(R.id.subjecthomework);
        editTextHashs.put(R.string.subject, subject);
        final EditText description = alertLayout.findViewById(R.id.descriptionhomework);
        editTextHashs.put(R.string.desctiption, description);
        final TextView date = alertLayout.findViewById(R.id.datehomework);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Homework homework = adapter.get(listposition);

        final ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, dbHelper.getSubjectsList());
        subject.setAdapter(subjectAdapter);
        subject.setThreshold(1);

        subject.setText(homework.getSubject());
        description.setText(homework.getDescription());
        date.setText(homework.getDate());
        select_color.setBackgroundColor(homework.getColor() != 0 ? homework.getColor() : Color.WHITE);

        subject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = (String) parent.getItemAtPosition(position);
                Week details = dbHelper.getSubjectDetails(selectedSubject);
                if (details != null) {
                    select_color.setBackgroundColor(details.getColor());
                }
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mdayofMonth = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date.setText(String.format("%02d-%02d-%02d", year, month+1, dayOfMonth));
                        homework.setDate(String.format("%02d-%02d-%02d", year, month+1, dayOfMonth));
                    }
                }, mYear, mMonth, mdayofMonth);
                datePickerDialog.setTitle(R.string.choose_date);
                datePickerDialog.show();
            }
        });

        select_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialColorPickerDialog.Builder(activity)
                        .setTitle(R.string.color_picker_default_title)
                        .setColorRes(activity.getResources().getIntArray(R.array.default_colors))
                        .setColorShape(ColorShape.SQAURE)
                        .setColorListener((color, colorHex) -> {
                            select_color.setBackgroundColor(color);
                        })
                        .show();
            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.edit_homework);
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(subject.getText()) || TextUtils.isEmpty(description.getText())) {
                    for (Map.Entry<Integer, EditText> editText : editTextHashs.entrySet()) {
                        if (TextUtils.isEmpty(editText.getValue().getText())) {
                            editText.getValue().setError(activity.getResources().getString(editText.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                            editText.getValue().requestFocus();
                        }
                    }
                } else if (!date.getText().toString().matches(".*\\d+.*")) {
                    Snackbar.make(alertLayout, R.string.deadline_snackbar, Snackbar.LENGTH_LONG).show();
                } else {
                    DbHelper dbHelper = new DbHelper(activity);
                    HomeworksAdapter homeworksAdapter = (HomeworksAdapter) listView.getAdapter();
                    ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                    homework.setSubject(subject.getText().toString());
                    homework.setDescription(description.getText().toString());
                    homework.setColor(buttonColor.getColor());
                    dbHelper.updateHomework(homework);
                    homeworksAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
            });
    }

    public static void getAddHomeworkDialog(final Activity activity, final View alertLayout, final HomeworksAdapter adapter) {
        final DbHelper dbHelper = new DbHelper(activity);
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final AutoCompleteTextView subject = alertLayout.findViewById(R.id.subjecthomework);
        editTextHashs.put(R.string.subject, subject);
        final EditText description = alertLayout.findViewById(R.id.descriptionhomework);
        editTextHashs.put(R.string.desctiption, description);
        final TextView date = alertLayout.findViewById(R.id.datehomework);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Homework homework = new Homework();

        final ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, dbHelper.getSubjectsList());
        subject.setAdapter(subjectAdapter);
        subject.setThreshold(1);
        subject.setThreshold(1);

        subject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = (String) parent.getItemAtPosition(position);
                Week details = dbHelper.getSubjectDetails(selectedSubject);
                if (details != null) {
                    select_color.setBackgroundColor(details.getColor());
                }
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mdayofMonth = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date.setText(String.format("%02d-%02d-%02d", year, month+1, dayOfMonth));
                        homework.setDate(String.format("%02d-%02d-%02d", year, month+1, dayOfMonth));
                    }
                }, mYear, mMonth, mdayofMonth);
                datePickerDialog.setTitle(R.string.choose_date);
                datePickerDialog.show();
            }
        });

        select_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialColorPickerDialog.Builder(activity)
                        .setTitle(R.string.color_picker_default_title)
                        .setColorRes(activity.getResources().getIntArray(R.array.default_colors))
                        .setColorShape(ColorShape.SQAURE)
                        .setColorListener((color, colorHex) -> {
                            select_color.setBackgroundColor(color);
                        })
                        .show();
            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.add_homework);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(subject.getText()) || TextUtils.isEmpty(description.getText())) {
                    for (Map.Entry<Integer, EditText> editText : editTextHashs.entrySet()) {
                        if(TextUtils.isEmpty(editText.getValue().getText())) {
                            editText.getValue().setError(activity.getResources().getString(editText.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                            editText.getValue().requestFocus();
                        }
                    }
                } else if(!date.getText().toString().matches(".*\\d+.*")) {
                    Snackbar.make(alertLayout, R.string.deadline_snackbar, Snackbar.LENGTH_LONG).show();
                } else {
                    DbHelper dbHelper = new DbHelper(activity);
                    ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                    homework.setSubject(subject.getText().toString());
                    homework.setDescription(description.getText().toString());
                    homework.setColor(buttonColor.getColor());
                    dbHelper.insertHomework(homework);

                    adapter.clear();
                    adapter.addAll(dbHelper.getHomework());
                    adapter.notifyDataSetChanged();

                    subject.getText().clear();
                    description.getText().clear();
                    date.setText(R.string.select_date);
                    select_color.setBackgroundColor(Color.WHITE);
                    subject.requestFocus();
                    dialog.dismiss();
                }
            }
        });
    }

    public static void getEditTeacherDialog(final Activity activity, final View alertLayout, final ArrayList<Teacher> adapter, final ListView listView, int listposition) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText name = alertLayout.findViewById(R.id.name_dialog);
        editTextHashs.put(R.string.name, name);
        final EditText post = alertLayout.findViewById(R.id.post_dialog);
        editTextHashs.put(R.string.post, post);
        final EditText phone_number = alertLayout.findViewById(R.id.phonenumber_dialog);
        editTextHashs.put(R.string.phone_number, phone_number);
        final EditText email = alertLayout.findViewById(R.id.email_dialog);
        editTextHashs.put(R.string.email, email);
        final EditText cabinNumber = alertLayout.findViewById(R.id.cabinnumber_dialog);
        editTextHashs.put(R.string.cabin_number, cabinNumber);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Teacher teacher = adapter.get(listposition);

        name.setText(teacher.getName());
        post.setText(teacher.getPost());
        phone_number.setText(teacher.getPhonenumber());
        email.setText(teacher.getEmail());
        cabinNumber.setText(teacher.getCabinNumber());
        select_color.setBackgroundColor(teacher.getColor() != 0 ? teacher.getColor() : Color.WHITE);

        select_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialColorPickerDialog.Builder(activity)
                        .setTitle(R.string.color_picker_default_title)
                        .setColorRes(activity.getResources().getIntArray(R.array.default_colors))
                        .setColorShape(ColorShape.SQAURE)
                        .setColorListener((color, colorHex) -> {
                            select_color.setBackgroundColor(color);
                        })
                        .show();
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.edit_teacher);
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(name.getText()) || TextUtils.isEmpty(post.getText()) || TextUtils.isEmpty(phone_number.getText()) || TextUtils.isEmpty(email.getText())) {
                    for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                        if(TextUtils.isEmpty(entry.getValue().getText())) {
                            entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                            entry.getValue().requestFocus();
                        }
                    }
                } else {
                    DbHelper dbHelper = new DbHelper(activity);
                    TeachersAdapter teachersAdapter = (TeachersAdapter) listView.getAdapter();
                    ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                    teacher.setName(name.getText().toString());
                    teacher.setPost(post.getText().toString());
                    teacher.setPhonenumber(phone_number.getText().toString());
                    teacher.setEmail(email.getText().toString());
                    teacher.setCabinNumber(cabinNumber.getText().toString());
                    teacher.setColor(buttonColor.getColor());
                    dbHelper.updateTeacher(teacher);
                    teachersAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });
    }

    public static void getAddTeacherDialog(final Activity activity, final View alertLayout, final TeachersAdapter adapter) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText name = alertLayout.findViewById(R.id.name_dialog);
        editTextHashs.put(R.string.name, name);
        final EditText post = alertLayout.findViewById(R.id.post_dialog);
        editTextHashs.put(R.string.post, post);
        final EditText phone_number = alertLayout.findViewById(R.id.phonenumber_dialog);
        editTextHashs.put(R.string.phone_number, phone_number);
        final EditText email = alertLayout.findViewById(R.id.email_dialog);
        editTextHashs.put(R.string.email, email);
        final EditText cabinNumber = alertLayout.findViewById(R.id.cabinnumber_dialog);
        editTextHashs.put(R.string.cabin_number, cabinNumber);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Teacher teacher = new Teacher();

        select_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialColorPickerDialog.Builder(activity)
                        .setTitle(R.string.color_picker_default_title)
                        .setColorRes(activity.getResources().getIntArray(R.array.default_colors))
                        .setColorShape(ColorShape.SQAURE)
                        .setColorListener((color, colorHex) -> {
                            select_color.setBackgroundColor(color);
                        })
                        .show();
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(activity.getResources().getString(R.string.add_teacher));
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(name.getText()) || TextUtils.isEmpty(post.getText()) || TextUtils.isEmpty(phone_number.getText()) || TextUtils.isEmpty(email.getText())) {
                    for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                        if(TextUtils.isEmpty(entry.getValue().getText())) {
                            entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                            entry.getValue().requestFocus();
                        }
                    }
                } else {
                    DbHelper dbHelper = new DbHelper(activity);
                    ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                    teacher.setName(name.getText().toString());
                    teacher.setPost(post.getText().toString());
                    teacher.setPhonenumber(phone_number.getText().toString());
                    teacher.setEmail(email.getText().toString());
                    teacher.setCabinNumber(cabinNumber.getText().toString());
                    teacher.setColor(buttonColor.getColor());
                    dbHelper.insertTeacher(teacher);

                    adapter.clear();
                    adapter.addAll(dbHelper.getTeacher());
                    adapter.notifyDataSetChanged();

                    name.getText().clear();
                    post.getText().clear();
                    phone_number.getText().clear();
                    email.getText().clear();
                    cabinNumber.getText().clear();
                    select_color.setBackgroundColor(Color.WHITE);
                    name.requestFocus();
                    dialog.dismiss();
                }
            }
        });
    }

    public static void getEditNoteDialog(final Activity activity, final View alertLayout, final ArrayList<Note> adapter, final ListView listView, int listposition) {
        final EditText title = alertLayout.findViewById(R.id.titlenote);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Note note = adapter.get(listposition);
        title.setText(note.getTitle());
        select_color.setBackgroundColor(note.getColor() != 0 ? note.getColor() : Color.WHITE);

        select_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialColorPickerDialog.Builder(activity)
                        .setTitle(R.string.color_picker_default_title)
                        .setColorRes(activity.getResources().getIntArray(R.array.default_colors))
                        .setColorShape(ColorShape.SQAURE)
                        .setColorListener((color, colorHex) -> {
                            select_color.setBackgroundColor(color);
                        })
                        .show();
            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.edit_note);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(title.getText())) {
                    title.setError(activity.getResources().getString(R.string.title_error));
                    title.requestFocus();
                } else {
                    DbHelper dbHelper = new DbHelper(activity);
                    ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                    note.setTitle(title.getText().toString());
                    note.setColor(buttonColor.getColor());
                    dbHelper.updateNote(note);
                    NotesAdapter notesAdapter = (NotesAdapter) listView.getAdapter();
                    notesAdapter.notifyDataSetChanged();

                    dialog.dismiss();
                }
            }
        });
    }

    public static void getAddNoteDialog(final Activity activity, final View alertLayout, final NotesAdapter adapter) {
        final EditText title = alertLayout.findViewById(R.id.titlenote);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Note note = new Note();

        select_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialColorPickerDialog.Builder(activity)
                        .setTitle(R.string.color_picker_default_title)
                        .setColorRes(activity.getResources().getIntArray(R.array.default_colors))
                        .setColorShape(ColorShape.SQAURE)
                        .setColorListener((color, colorHex) -> {
                            select_color.setBackgroundColor(color);
                        })
                        .show();
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.add_note);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(title.getText())) {
                    title.setError(activity.getResources().getString(R.string.title_error));
                    title.requestFocus();
                } else {
                    DbHelper dbHelper = new DbHelper(activity);
                    ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                    note.setTitle(title.getText().toString());
                    note.setColor(buttonColor.getColor());
                    dbHelper.insertNote(note);

                    adapter.clear();
                    adapter.addAll(dbHelper.getNote());
                    adapter.notifyDataSetChanged();

                    title.getText().clear();
                    select_color.setBackgroundColor(Color.WHITE);
                    dialog.dismiss();
                }
            }
        });
    }

    public static void getEditExamDialog(final Activity activity, final View alertLayout, final ArrayList<Exam> adapter, final ListView listView, int listposition) {
        final DbHelper dbHelper = new DbHelper(activity);
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final AutoCompleteTextView subject = alertLayout.findViewById(R.id.subjectexam_dialog);
        editTextHashs.put(R.string.subject, subject);
        final AutoCompleteTextView teacher = alertLayout.findViewById(R.id.teacherexam_dialog);
        editTextHashs.put(R.string.teacher, teacher);

        final ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, dbHelper.getSubjectsList());
        subject.setAdapter(subjectAdapter);
        subject.setThreshold(1);

        final ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, dbHelper.getTeachersList());
        teacher.setAdapter(teacherAdapter);
        teacher.setThreshold(1);

        final EditText room = alertLayout.findViewById(R.id.roomexam_dialog);
        editTextHashs.put(R.string.room, room);
        final TextView date = alertLayout.findViewById(R.id.dateexam_dialog);
        final TextView time = alertLayout.findViewById(R.id.timeexam_dialog);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Exam exam = adapter.get(listposition);

        subject.setText(exam.getSubject());
        teacher.setText(exam.getTeacher());
        room.setText(exam.getRoom());
        date.setText(exam.getDate());
        time.setText(TimeUtils.formatTo12Hour(exam.getTime()));
        select_color.setBackgroundColor(exam.getColor() != 0 ? exam.getColor() : Color.WHITE);

        subject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = (String) parent.getItemAtPosition(position);
                Week details = dbHelper.getSubjectDetails(selectedSubject);
                if (details != null) {
                    teacher.setText(details.getTeacher());
                    room.setText(details.getRoom());
                    select_color.setBackgroundColor(details.getColor());
                }
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mdayofMonth = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date.setText(String.format("%02d-%02d-%02d", year, month+1, dayOfMonth));
                        exam.setDate(String.format("%02d-%02d-%02d", year, month+1, dayOfMonth));
                    }
                }, mYear, mMonth, mdayofMonth);
                datePickerDialog.setTitle(R.string.choose_date);
                datePickerDialog.show();
            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kotlin.Pair<Integer, Integer> parsedTime = TimeUtils.parse24Hour(exam.getTime());
                int mHour = parsedTime != null ? parsedTime.getFirst() : Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int mMinute = parsedTime != null ? parsedTime.getSecond() : Calendar.getInstance().get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String time24 = TimeUtils.get24HourString(hourOfDay, minute);
                                time.setText(TimeUtils.formatTo12Hour(time24));
                                exam.setTime(time24);
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.setTitle(R.string.choose_time);
                timePickerDialog.show();
            }
        });


        select_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialColorPickerDialog.Builder(activity)
                        .setTitle(R.string.color_picker_default_title)
                        .setColorRes(activity.getResources().getIntArray(R.array.default_colors))
                        .setColorShape(ColorShape.SQAURE)
                        .setColorListener((color, colorHex) -> {
                            select_color.setBackgroundColor(color);
                        })
                        .show();
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(activity.getResources().getString(R.string.add_exam));
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(subject.getText()) || TextUtils.isEmpty(teacher.getText()) || TextUtils.isEmpty(room.getText())) {
                    for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                        if(TextUtils.isEmpty(entry.getValue().getText())) {
                            entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                            entry.getValue().requestFocus();
                        }
                    }
                } else if (!date.getText().toString().matches(".*\\d+.*")) {
                    Snackbar.make(alertLayout, R.string.date_error, Snackbar.LENGTH_LONG).show();
                } else if (!time.getText().toString().matches(".*\\d+.*")) {
                    Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
                } else {
                    DbHelper dbHelper = new DbHelper(activity);
                    ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                    exam.setSubject(subject.getText().toString());
                    exam.setTeacher(teacher.getText().toString());
                    exam.setRoom(room.getText().toString());
                    exam.setColor(buttonColor.getColor());

                    dbHelper.updateExam(exam);

                    ExamsAdapter examsAdapter = (ExamsAdapter) listView.getAdapter();
                    examsAdapter.notifyDataSetChanged();

                    dialog.dismiss();
                }
            }
        });
    }

    public static void getAddExamDialog(final Activity activity, final View alertLayout, final ExamsAdapter adapter) {
        final DbHelper dbHelper = new DbHelper(activity);
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final AutoCompleteTextView subject = alertLayout.findViewById(R.id.subjectexam_dialog);
        editTextHashs.put(R.string.subject, subject);
        final AutoCompleteTextView teacher = alertLayout.findViewById(R.id.teacherexam_dialog);
        editTextHashs.put(R.string.teacher, teacher);

        final ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, dbHelper.getSubjectsList());
        subject.setAdapter(subjectAdapter);
        subject.setThreshold(1);

        final ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, dbHelper.getTeachersList());
        teacher.setAdapter(teacherAdapter);
        teacher.setThreshold(1);

        final EditText room = alertLayout.findViewById(R.id.roomexam_dialog);
        editTextHashs.put(R.string.room, room);
        final TextView date = alertLayout.findViewById(R.id.dateexam_dialog);
        final TextView time = alertLayout.findViewById(R.id.timeexam_dialog);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Exam exam = new Exam();

        subject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = (String) parent.getItemAtPosition(position);
                Week details = dbHelper.getSubjectDetails(selectedSubject);
                if (details != null) {
                    teacher.setText(details.getTeacher());
                    room.setText(details.getRoom());
                    select_color.setBackgroundColor(details.getColor());
                }
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mdayofMonth = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date.setText(String.format("%02d-%02d-%02d", year, month+1, dayOfMonth));
                        exam.setDate(String.format("%02d-%02d-%02d", year, month+1, dayOfMonth));
                    }
                }, mYear, mMonth, mdayofMonth);
                datePickerDialog.setTitle(R.string.choose_date);
                datePickerDialog.show();
            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kotlin.Pair<Integer, Integer> parsedTime = TimeUtils.parse24Hour(exam.getTime());
                int mHour = parsedTime != null ? parsedTime.getFirst() : Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int mMinute = parsedTime != null ? parsedTime.getSecond() : Calendar.getInstance().get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String time24 = TimeUtils.get24HourString(hourOfDay, minute);
                                time.setText(TimeUtils.formatTo12Hour(time24));
                                exam.setTime(time24);
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.setTitle(R.string.choose_time);
                timePickerDialog.show();
            }
        });


        select_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialColorPickerDialog.Builder(activity)
                        .setTitle(R.string.color_picker_default_title)
                        .setColorRes(activity.getResources().getIntArray(R.array.default_colors))
                        .setColorShape(ColorShape.SQAURE)
                        .setColorListener((color, colorHex) -> {
                            select_color.setBackgroundColor(color);
                        })
                        .show();
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(activity.getResources().getString(R.string.add_exam));
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(subject.getText()) || TextUtils.isEmpty(teacher.getText()) || TextUtils.isEmpty(room.getText())) {
                    for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                        if(TextUtils.isEmpty(entry.getValue().getText())) {
                            entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                            entry.getValue().requestFocus();
                        }
                    }
                } else if (!date.getText().toString().matches(".*\\d+.*")) {
                    Snackbar.make(alertLayout, R.string.date_error, Snackbar.LENGTH_LONG).show();
                } else if (!time.getText().toString().matches(".*\\d+.*")) {
                    Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
                } else {
                    DbHelper dbHelper = new DbHelper(activity);
                    ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                    exam.setSubject(subject.getText().toString());
                    exam.setTeacher(teacher.getText().toString());
                    exam.setRoom(room.getText().toString());
                    exam.setColor(buttonColor.getColor());

                    dbHelper.insertExam(exam);

                    adapter.clear();
                    adapter.addAll(dbHelper.getExam());
                    adapter.notifyDataSetChanged();

                    subject.getText().clear();
                    teacher.getText().clear();
                    room.getText().clear();
                    date.setText(R.string.select_date);
                    time.setText(R.string.select_time);
                    select_color.setBackgroundColor(Color.WHITE);
                    subject.requestFocus();
                    dialog.dismiss();
                }
            }
        });
    }
}


