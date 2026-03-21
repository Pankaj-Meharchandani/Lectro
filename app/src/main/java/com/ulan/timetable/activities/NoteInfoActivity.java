package com.ulan.timetable.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ulan.timetable.model.Note;
import com.ulan.timetable.R;
import com.ulan.timetable.utils.DbHelper;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class NoteInfoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 2;
    private DbHelper db;
    private Note note;
    private EditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_info);
        setupIntent();
        setupToolbar();
    }

    private void setupIntent() {
        db = new DbHelper(NoteInfoActivity.this);
        note = (Note) getIntent().getSerializableExtra(NotesActivity.KEY_NOTE);
        text = findViewById(R.id.edittextNote);
        if(note.getText() != null) {
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

    private Drawable getDrawableFromUri(String source) {
        try {
            Uri uri = Uri.parse(source);
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            return drawable;
        } catch (Exception e) {
            return null;
        }
    }

    private void setupToolbar() {
        ImageButton btnBold = findViewById(R.id.btn_bold);
        ImageButton btnBullet = findViewById(R.id.btn_bullet);
        ImageButton btnHeading = findViewById(R.id.btn_heading);
        ImageButton btnSubheading = findViewById(R.id.btn_subheading);
        ImageButton btnDivider = findViewById(R.id.btn_divider);
        ImageButton btnImage = findViewById(R.id.btn_image);

        btnBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applySpan(new StyleSpan(android.graphics.Typeface.BOLD));
            }
        });

        btnBullet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applySpan(new BulletSpan(20));
            }
        });

        btnHeading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applySpan(new RelativeSizeSpan(1.5f));
            }
        });

        btnSubheading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applySpan(new RelativeSizeSpan(1.2f));
            }
        });

        btnDivider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.append("\n\n--------------------------------\n\n");
            }
        });

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
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
                insertImage(uri);
            }
        }
    }

    private void insertImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

            int start = text.getSelectionStart();
            SpannableStringBuilder ssb = new SpannableStringBuilder(text.getText());
            ssb.insert(start, "\n");
            ssb.setSpan(new ImageSpan(drawable, uri.toString()), start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setText(ssb);
            text.setSelection(start + 1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void applySpan(Object span) {
        int start = text.getSelectionStart();
        int end = text.getSelectionEnd();
        if (start != end) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(text.getText());
            ssb.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setText(ssb);
            text.setSelection(end);
        }
    }

    private void saveNote() {
        String htmlText;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            htmlText = Html.toHtml(text.getText(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        } else {
            htmlText = Html.toHtml(text.getText());
        }
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
        switch (item.getItemId()) {
            case android.R.id.home:
                saveNote();
                Toast.makeText(NoteInfoActivity.this, getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
