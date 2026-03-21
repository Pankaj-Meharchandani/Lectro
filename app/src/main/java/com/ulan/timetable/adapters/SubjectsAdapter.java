package com.ulan.timetable.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ulan.timetable.R;
import com.ulan.timetable.model.Subject;

import java.util.ArrayList;

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
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Subject subject = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(mResource, parent, false);
        }

        TextView name = convertView.findViewById(R.id.subject_name);
        TextView teacher = convertView.findViewById(R.id.subject_teacher);
        CardView cardView = convertView.findViewById(R.id.subject_cardview);

        if (subject != null) {
            name.setText(subject.getName());
            teacher.setText(subject.getTeacher());
            cardView.setCardBackgroundColor(subject.getColor());
        }

        return convertView;
    }
}
