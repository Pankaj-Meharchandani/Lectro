package com.ulan.timetable.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ulan.timetable.activities.NoteInfoActivity;
import com.ulan.timetable.activities.NotesActivity;
import com.ulan.timetable.adapters.NotesAdapter;
import com.ulan.timetable.model.Subject;
import com.ulan.timetable.R;
import com.ulan.timetable.utils.DbHelper;

public class SubjectNotesFragment extends Fragment {

    private Subject subject;
    private DbHelper db;
    private NotesAdapter adapter;
    private ListView listView;

    public static SubjectNotesFragment newInstance(Subject subject) {
        SubjectNotesFragment fragment = new SubjectNotesFragment();
        Bundle args = new Bundle();
        args.putSerializable("subject", subject);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            subject = (Subject) getArguments().getSerializable("subject");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subject_notes, container, false);
        db = new DbHelper(getContext());
        listView = view.findViewById(R.id.subject_notes_list);
        
        if (subject != null) {
            adapter = new NotesAdapter(getActivity(), listView, R.layout.listview_notes_adapter, db.getNotesBySubject(subject.getId()));
            listView.setAdapter(adapter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), NoteInfoActivity.class);
                intent.putExtra(NotesActivity.KEY_NOTE, adapter.getItem(position));
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        if (adapter != null && subject != null) {
            adapter.clear();
            adapter.addAll(db.getNotesBySubject(subject.getId()));
            adapter.notifyDataSetChanged();
        }
    }
}
