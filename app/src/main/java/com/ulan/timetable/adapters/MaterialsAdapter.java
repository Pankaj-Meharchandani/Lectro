package com.ulan.timetable.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ulan.timetable.R;
import com.ulan.timetable.model.Material;

import java.util.ArrayList;

public class MaterialsAdapter extends ArrayAdapter<Material> {

    private Activity mActivity;
    private int mResource;

    public MaterialsAdapter(Activity activity, int resource, ArrayList<Material> objects) {
        super(activity, resource, objects);
        mActivity = activity;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Material material = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(mResource, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.material_icon);
        TextView name = convertView.findViewById(R.id.material_name);

        if (material != null) {
            name.setText(material.getName());
            if (material.getType() != null) {
                if (material.getType().contains("image")) {
                    icon.setImageResource(R.drawable.ic_menu_gallery);
                } else {
                    icon.setImageResource(R.drawable.baseline_book_24);
                }
            } else {
                icon.setImageResource(R.drawable.baseline_book_24);
            }
        }

        return convertView;
    }
}
