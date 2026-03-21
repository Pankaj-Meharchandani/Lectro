package com.ulan.timetable.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ulan.timetable.adapters.MaterialsAdapter;
import com.ulan.timetable.model.Material;
import com.ulan.timetable.model.Subject;
import com.ulan.timetable.R;
import com.ulan.timetable.utils.DbHelper;

public class SubjectMaterialsFragment extends Fragment {

    private Subject subject;
    private DbHelper db;
    private MaterialsAdapter adapter;
    private ListView listView;

    public static SubjectMaterialsFragment newInstance(Subject subject) {
        SubjectMaterialsFragment fragment = new SubjectMaterialsFragment();
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
        View view = inflater.inflate(R.layout.fragment_subject_materials, container, false);
        db = new DbHelper(getContext());
        listView = view.findViewById(R.id.subject_materials_list);
        
        if (subject != null) {
            adapter = new MaterialsAdapter(getActivity(), R.layout.listview_materials_adapter, db.getMaterialsBySubject(subject.getId()));
            listView.setAdapter(adapter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Material material = adapter.getItem(position);
                if (material != null) {
                    openFile(material);
                }
            }
        });

        return view;
    }

    private void openFile(Material material) {
        try {
            Uri uri = Uri.parse(material.getPath());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, material.getType());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Cannot open file", Toast.LENGTH_SHORT).show();
        }
    }

    public void refresh() {
        if (adapter != null && subject != null) {
            adapter.clear();
            adapter.addAll(db.getMaterialsBySubject(subject.getId()));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
}
