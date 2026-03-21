package com.example.timetable.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.timetable.adapters.FragmentsTabAdapter;
import com.example.timetable.fragments.SubjectMaterialsFragment;
import com.example.timetable.fragments.SubjectNotesFragment;
import com.example.timetable.model.Material;
import com.example.timetable.model.Note;
import com.example.timetable.model.Subject;
import com.example.timetable.R;
import com.example.timetable.utils.DbHelper;

public class SubjectDetailActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private Subject subject;
    private DbHelper db;
    private ViewPager viewPager;
    private FragmentsTabAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_detail);

        subject = (Subject) getIntent().getSerializableExtra(NotesActivity.KEY_SUBJECT);
        db = new DbHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(subject.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewPager = findViewById(R.id.subject_viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.subject_tabs);
        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton fab = findViewById(R.id.fab_add_material);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddOptions();
            }
        });
    }

    private void showAddOptions() {
        String[] options = {getString(R.string.add_note), getString(R.string.add_material)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_subject)
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            addNewNote();
                        } else {
                            pickFile();
                        }
                    }
                })
                .show();
    }

    private void addNewNote() {
        Note note = new Note();
        note.setSubjectId(subject.getId());
        note.setTitle("New Note");
        note.setColor(subject.getColor());
        long id = db.insertNote(note);
        note.setId((int) id);

        Intent intent = new Intent(SubjectDetailActivity.this, NoteInfoActivity.class);
        intent.putExtra(NotesActivity.KEY_NOTE, note);
        startActivity(intent);
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_file)), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                saveMaterial(uri);
            }
        }
    }

    private void saveMaterial(Uri uri) {
        String fileName = getFileName(uri);
        String mimeType = getContentResolver().getType(uri);

        Material material = new Material();
        material.setSubjectId(subject.getId());
        material.setPath(uri.toString());
        material.setType(mimeType);
        material.setName(fileName);

        db.insertMaterial(material);
        Toast.makeText(this, "Material added", Toast.LENGTH_SHORT).show();
        refreshCurrentFragment();
    }

    private void refreshCurrentFragment() {
        Fragment currentFragment = adapter.getItem(viewPager.getCurrentItem());
        if (currentFragment instanceof SubjectNotesFragment) {
            ((SubjectNotesFragment) currentFragment).refresh();
        } else if (currentFragment instanceof SubjectMaterialsFragment) {
            ((SubjectMaterialsFragment) currentFragment).refresh();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new FragmentsTabAdapter(getSupportFragmentManager());
        adapter.addFragment(SubjectNotesFragment.newInstance(subject), getString(R.string.notes));
        adapter.addFragment(SubjectMaterialsFragment.newInstance(subject), getString(R.string.materials));
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


