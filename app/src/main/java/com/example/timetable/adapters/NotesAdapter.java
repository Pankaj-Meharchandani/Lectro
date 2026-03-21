package com.example.timetable.adapters;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.PopupMenu;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.timetable.model.Note;
import com.example.timetable.R;
import com.example.timetable.utils.AlertDialogsHelper;
import com.example.timetable.utils.DbHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;


public class NotesAdapter extends ArrayAdapter<Note> {

    private Activity mActivity;
    private int mResource;
    private ArrayList<Note> notelist;
    private ListView mListView;

    private static class ViewHolder {
        TextView title;
        ImageView popup;
        CardView cardView;
    }

    public NotesAdapter(Activity activity, ListView listView, int resource, ArrayList<Note> objects) {
        super(activity, resource, objects);
        mActivity = activity;
        mListView = listView;
        mResource = resource;
        notelist = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Note note = getItem(position);
        final ViewHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.titlenote);
            holder.popup = convertView.findViewById(R.id.popupbtn);
            holder.cardView = convertView.findViewById(R.id.notes_cardview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (note != null) {
            holder.title.setText(note.getTitle());
            holder.cardView.setCardBackgroundColor(note.getColor());
            holder.popup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu popup = new PopupMenu(mActivity, holder.popup);
                    final DbHelper db = new DbHelper(mActivity);
                    popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();
                            if (id == R.id.delete_popup) {
                                db.deleteNoteById(note.getId());
                                notelist.remove(position);
                                notifyDataSetChanged();
                                return true;
                            } else if (id == R.id.edit_popup) {
                                final View alertLayout = mActivity.getLayoutInflater().inflate(R.layout.dialog_add_note, null);
                                AlertDialogsHelper.getEditNoteDialog(mActivity, alertLayout, notelist, mListView, position);
                                return true;
                            } else if (id == R.id.move_up_popup) {
                                if (position > 0) {
                                    swapNotes(position, position - 1, db);
                                }
                                return true;
                            } else if (id == R.id.move_down_popup) {
                                if (position < notelist.size() - 1) {
                                    swapNotes(position, position + 1, db);
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

        hidePopUpMenu(holder);

        return convertView;
    }

    private void swapNotes(int pos1, int pos2, DbHelper db) {
        Note n1 = notelist.get(pos1);
        Note n2 = notelist.get(pos2);
        
        // Simple swap in list
        Collections.swap(notelist, pos1, pos2);
        
        // Update sort order in DB
        // We'll just assign their positions as sort order for simplicity
        for (int i = 0; i < notelist.size(); i++) {
            db.updateNoteSortOrder(notelist.get(i).getId(), i);
        }
        
        notifyDataSetChanged();
    }

    public ArrayList<Note> getNoteList() {
        return notelist;
    }

     private void hidePopUpMenu(ViewHolder holder) {
        SparseBooleanArray checkedItems = mListView.getCheckedItemPositions();
        if (checkedItems != null && checkedItems.size() > 0) {
            for (int i = 0; i < checkedItems.size(); i++) {
                int key = checkedItems.keyAt(i);
                if (checkedItems.get(key)) {
                    holder.popup.setVisibility(View.INVISIBLE);
                    }
            }
        } else {
            holder.popup.setVisibility(View.VISIBLE);
        }
    }
}


