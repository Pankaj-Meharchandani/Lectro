package com.example.timetable.activities;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.timetable.adapters.SubjectsAdapter;
import com.example.timetable.model.Subject;
import com.example.timetable.R;
import com.example.timetable.utils.AlertDialogsHelper;
import com.example.timetable.utils.DbHelper;

import java.util.ArrayList;

public class NotesActivity extends AppCompatActivity {

    public static String KEY_SUBJECT = "subject";
    public static String KEY_NOTE = "note";
    private Context context = this;
    private ListView listView;
    private DbHelper db;
    private SubjectsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.subjects);
        }
        initAll();
    }

    private void initAll() {
        setupAdapter();
        setupListViewMultiSelect();
        setupCustomDialog();
    }

    private void setupAdapter() {
        db = new DbHelper(context);
        listView = findViewById(R.id.notelist);
        adapter = new SubjectsAdapter(NotesActivity.this, R.layout.listview_subjects_adapter, db.getAllSubjects());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, SubjectDetailActivity.class);
                intent.putExtra(KEY_SUBJECT, adapter.getItem(position));
                startActivity(intent);
            }
        });
    }

    private void setupListViewMultiSelect() {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = listView.getCheckedItemCount();
                mode.setTitle(checkedCount + " " + getResources().getString(R.string.selected));
                if(checkedCount == 0) mode.finish();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.toolbar_action_mode, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                        for (int i = 0; i < checkedItems.size(); i++) {
                            int key = checkedItems.keyAt(i);
                            if (checkedItems.get(key)) {
                                db.deleteSubjectById(adapter.getItem(key).getId());
                            }
                        }
                        refreshList();
                        mode.finish();
                        return true;

                    default:
                        return false;
                }
            }
            @Override
            public void onDestroyActionMode(ActionMode mode) { }
        });
    }

    private void setupCustomDialog() {
        final View alertLayout = getLayoutInflater().inflate(R.layout.dialog_create_subject, null);
        AlertDialogsHelper.getCreateSubjectDialog(NotesActivity.this, alertLayout, new Runnable() {
            @Override
            public void run() {
                refreshList();
            }
        });
    }

    private void refreshList() {
        adapter.clear();
        adapter.addAll(db.getAllSubjects());
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }
}


