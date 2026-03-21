package com.ulan.timetable.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ulan.timetable.R;
import com.ulan.timetable.model.Subject;
import com.ulan.timetable.utils.DbHelper;

import java.util.ArrayList;
import java.util.Collections;

public class SubjectsAdapter extends ArrayAdapter<Subject> {

    private Activity mActivity;
    private int mResource;
    private ArrayList<Subject> subjects;

    public SubjectsAdapter(Activity activity, int resource, ArrayList<Subject> objects) {
        super(activity, resource, objects);
        mActivity = activity;
        mResource = resource;
        subjects = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final Subject subject = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(mResource, parent, false);
        }

        TextView name = convertView.findViewById(R.id.subject_name);
        TextView teacher = convertView.findViewById(R.id.subject_teacher);
        CardView cardView = convertView.findViewById(R.id.subject_cardview);
        ImageView popupBtn = convertView.findViewById(R.id.popupbtn);

        if (subject != null) {
            name.setText(subject.getName());
            teacher.setText(subject.getTeacher());
            cardView.setCardBackgroundColor(subject.getColor());
            popupBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(mActivity, v);
                    final DbHelper db = new DbHelper(mActivity);
                    popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();
                            if (id == R.id.delete_popup) {
                                db.deleteSubjectById(subject.getId());
                                subjects.remove(position);
                                notifyDataSetChanged();
                                return true;
                            } else if (id == R.id.move_up_popup) {
                                if (position > 0) {
                                    swapSubjects(position, position - 1, db);
                                }
                                return true;
                            } else if (id == R.id.move_down_popup) {
                                if (position < subjects.size() - 1) {
                                    swapSubjects(position, position + 1, db);
                                }
                                return true;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });
        }

        return convertView;
    }

    private void swapSubjects(int pos1, int pos2, DbHelper db) {
        Collections.swap(subjects, pos1, pos2);
        for (int i = 0; i < subjects.size(); i++) {
            db.updateSubjectSortOrder(subjects.get(i).getId(), i);
        }
        notifyDataSetChanged();
    }
}
