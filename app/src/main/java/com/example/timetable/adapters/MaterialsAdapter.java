package com.example.timetable.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.timetable.R;
import com.example.timetable.model.Material;
import com.example.timetable.utils.DbHelper;

import java.util.ArrayList;
import java.util.Collections;

public class MaterialsAdapter extends ArrayAdapter<Material> {

    private Activity mActivity;
    private int mResource;
    private ArrayList<Material> materials;

    public MaterialsAdapter(Activity activity, int resource, ArrayList<Material> objects) {
        super(activity, resource, objects);
        mActivity = activity;
        mResource = resource;
        materials = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final Material material = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(mResource, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.material_icon);
        final TextView name = convertView.findViewById(R.id.material_name);
        ImageView popupBtn = convertView.findViewById(R.id.popupbtn);

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
                                db.deleteMaterialById(material.getId());
                                materials.remove(position);
                                notifyDataSetChanged();
                                return true;
                            } else if (id == R.id.edit_popup) {
                                showRenameDialog(material, db);
                                return true;
                            } else if (id == R.id.move_up_popup) {
                                if (position > 0) {
                                    swapMaterials(position, position - 1, db);
                                }
                                return true;
                            } else if (id == R.id.move_down_popup) {
                                if (position < materials.size() - 1) {
                                    swapMaterials(position, position + 1, db);
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

    private void showRenameDialog(final Material material, final DbHelper db) {
        final EditText editText = new EditText(mActivity);
        editText.setText(material.getName());
        new AlertDialog.Builder(mActivity)
                .setTitle("Rename Material")
                .setView(editText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = editText.getText().toString();
                        db.updateMaterialName(material.getId(), newName);
                        material.setName(newName);
                        notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void swapMaterials(int pos1, int pos2, DbHelper db) {
        Collections.swap(materials, pos1, pos2);
        for (int i = 0; i < materials.size(); i++) {
            db.updateMaterialSortOrder(materials.get(i).getId(), i);
        }
        notifyDataSetChanged();
    }
}


