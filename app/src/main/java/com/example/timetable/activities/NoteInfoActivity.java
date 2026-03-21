package com.example.timetable.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog;
import com.github.dhaval2404.colorpicker.listener.ColorListener;
import com.github.dhaval2404.colorpicker.model.ColorShape;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.timetable.model.Note;
import com.example.timetable.R;
import com.example.timetable.utils.DbHelper;

import java.io.InputStream;

public class NoteInfoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 2;
    private DbHelper db;
    private Note note;
    private EditText titleEdit;
    private EditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        setupIntent();
        setupToolbar();
        setupCheckboxLogic();
        setupAutoContinuation();
    }

    private void setupAutoContinuation() {
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1 && s.charAt(start) == '\n') {
                    String content = s.toString();
                    int lastNewLine = content.lastIndexOf('\n', start - 1);
                    int lineStart = (lastNewLine == -1) ? 0 : lastNewLine + 1;
                    String lastLine = content.substring(lineStart, start);

                    if (lastLine.startsWith("• ")) {
                        if (lastLine.length() > 2) {
                            text.post(() -> text.getText().insert(text.getSelectionStart(), "• "));
                        } else {
                            text.post(() -> text.getText().delete(lineStart, start + 1));
                        }
                    } else if (lastLine.startsWith("☐ ")) {
                        if (lastLine.length() > 2) {
                            text.post(() -> text.getText().insert(text.getSelectionStart(), "☐ "));
                        } else {
                            text.post(() -> text.getText().delete(lineStart, start + 1));
                        }
                    } else if (lastLine.startsWith("☑ ")) {
                        if (lastLine.length() > 2) {
                            text.post(() -> text.getText().insert(text.getSelectionStart(), "☐ "));
                        } else {
                            text.post(() -> text.getText().delete(lineStart, start + 1));
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupIntent() {
        db = new DbHelper(NoteInfoActivity.this);
        note = (Note) getIntent().getSerializableExtra(NotesActivity.KEY_NOTE);
        titleEdit = findViewById(R.id.edittextNoteTitle);
        text = findViewById(R.id.edittextNote);

        if (note != null) {
            titleEdit.setText(note.getTitle());
            if(!TextUtils.isEmpty(note.getText())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    text.setText(Html.fromHtml(note.getText(), Html.FROM_HTML_MODE_COMPACT, new Html.ImageGetter() {
                        @Override
                        public Drawable getDrawable(String source) {
                            return getDrawableFromUri(source);
                        }
                    }, null));
                } else {
                    text.setText(Html.fromHtml(note.getText(), new Html.ImageGetter() {
                        @Override
                        public Drawable getDrawable(String source) {
                            return getDrawableFromUri(source);
                        }
                    }, null));
                }
            }
        }
    }

    private Drawable getDrawableFromUri(String source) {
        try {
            Uri uri = Uri.parse(source);
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int padding = (int) (64 * getResources().getDisplayMetrics().density); 
            int width = screenWidth - padding;
            if (width <= 0) width = 500;
            
            float aspectRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
            int height = (int) (width * aspectRatio);
            drawable.setBounds(0, 0, width, height);
            return drawable;
        } catch (Exception e) {
            return null;
        }
    }

    private void setupToolbar() {
        findViewById(R.id.btn_bold).setOnClickListener(v -> toggleSpan(new StyleSpan(android.graphics.Typeface.BOLD)));
        findViewById(R.id.btn_bullet).setOnClickListener(v -> insertPrefix("• "));
        findViewById(R.id.btn_heading).setOnClickListener(v -> applySpanToLine(new RelativeSizeSpan(1.5f)));
        findViewById(R.id.btn_subheading).setOnClickListener(v -> applySpanToLine(new RelativeSizeSpan(1.2f)));
        findViewById(R.id.btn_divider).setOnClickListener(v -> insertTextAtCursor("\n--------------------------------\n"));
        findViewById(R.id.btn_todo).setOnClickListener(v -> insertPrefix("☐ "));
        findViewById(R.id.btn_image).setOnClickListener(v -> pickImage());
        findViewById(R.id.btn_color).setOnClickListener(v -> showColorPicker());
        findViewById(R.id.btn_share).setOnClickListener(v -> shareNote());
        findViewById(R.id.btn_delete).setOnClickListener(v -> deleteNote());
    }

    private void showColorPicker() {
        new MaterialColorPickerDialog.Builder(this)
                .setTitle(getString(R.string.pick_note_color))
                .setColorShape(ColorShape.CIRCLE)
                .setDefaultColor(note.getColor())
                .setColorListener((color, colorHex) -> {
                    note.setColor(color);
                })
                .show();
    }

    private void shareNote() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, titleEdit.getText().toString());
        intent.putExtra(Intent.EXTRA_TEXT, text.getText().toString());
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    private void deleteNote() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_note_title)
                .setMessage(R.string.delete_note_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.deleteNoteById(note.getId());
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void insertPrefix(String prefix) {
        int start = text.getSelectionStart();
        String content = text.getText().toString();
        int lineStart = content.lastIndexOf("\n", start - 1) + 1;
        
        if (start >= lineStart + prefix.length()) {
            String sub = content.substring(lineStart, lineStart + prefix.length());
            if (sub.equals(prefix)) {
                text.getText().delete(lineStart, lineStart + prefix.length());
                return;
            }
        }

        if (start == lineStart) {
            text.getText().insert(start, prefix);
        } else {
            text.getText().insert(start, "\n" + prefix);
        }
    }

    private void setupCheckboxLogic() {
        text.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int offset = text.getOffsetForPosition(event.getX(), event.getY());
                if (offset != -1) {
                    String content = text.getText().toString();
                    if (offset < content.length()) {
                        if (content.charAt(offset) == '☐') {
                            text.getText().replace(offset, offset + 1, "☑");
                            return true;
                        } else if (content.charAt(offset) == '☑') {
                            text.getText().replace(offset, offset + 1, "☐");
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    private void insertTextAtCursor(String textToInsert) {
        int start = text.getSelectionStart();
        if (start < 0) start = 0;
        text.getText().insert(start, textToInsert);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                insertImage(uri);
            }
        }
    }

    private void insertImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int padding = (int) (64 * getResources().getDisplayMetrics().density);
            int width = screenWidth - padding;
            if (width <= 0) width = 500;
            
            float aspectRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
            int height = (int) (width * aspectRatio);
            drawable.setBounds(0, 0, width, height);

            int start = text.getSelectionStart();
            if (start < 0) start = 0;
            text.getText().insert(start, "\n \n");
            text.getText().setSpan(new ImageSpan(drawable, uri.toString()), start + 1, start + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSelection(start + 3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleSpan(Object span) {
        int start = text.getSelectionStart();
        int end = text.getSelectionEnd();
        if (start != end) {
            text.getText().setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void applySpanToLine(Object span) {
        int start = text.getSelectionStart();
        int end = text.getSelectionEnd();
        String content = text.getText().toString();
        int lineStart = content.lastIndexOf("\n", start - 1) + 1;
        int lineEnd = content.indexOf("\n", end);
        if (lineEnd == -1) lineEnd = content.length();
        
        text.getText().setSpan(span, lineStart, lineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void saveNote() {
        if (note == null) return;
        String htmlText;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            htmlText = Html.toHtml(text.getText(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        } else {
            htmlText = Html.toHtml(text.getText());
        }
        note.setTitle(titleEdit.getText().toString());
        note.setText(htmlText);
        db.updateNote(note);
    }

    @Override
    public void onBackPressed() {
        saveNote();
        Toast.makeText(NoteInfoActivity.this, getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            saveNote();
            Toast.makeText(NoteInfoActivity.this, getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


