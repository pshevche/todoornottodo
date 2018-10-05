package com.cybermongols.todoornottodo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TaskAdapter extends ArrayAdapter<Task> {

    private AdapterCallback mListener;

    public TaskAdapter(Context context, ArrayList<Task> items, AdapterCallback listener) {
        super(context, 0, items);
        mListener = listener;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Task task = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_todo, parent, false);
        }
        // inflate view: date + title
        TextView task_title = convertView.findViewById(R.id.task_title);
        TextView task_deadline = convertView.findViewById(R.id.task_deadline);
        task_title.setText(task.getTitle());
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        task_deadline.setText(df.format(task.getDeadline()));
        if (task.isImportant()) {
            convertView.setBackgroundColor(Color.rgb(0xd8, 0x1b, 0x60));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        View overflow = convertView.findViewById(R.id.task_overflow);
        overflow.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this.getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.overflow_menu, popupMenu.getMenu());
            // Force icons to show
            Object menuHelper;
            Class[] argTypes;
            try {
                Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
                fMenuHelper.setAccessible(true);
                menuHelper = fMenuHelper.get(popupMenu);
                argTypes = new Class[]{boolean.class};
                menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
            } catch (Exception e) {
                popupMenu.show();
                return;
            }
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.task_overflow_complete:
                        mListener.completeTask(this.getItem(position));
                        break;
                    case R.id.task_overflow_edit:
                        mListener.editTask(this.getItem(position));
                        break;
                    default:
                        return false;
                }
                return true;
            });
            popupMenu.show();
        });
        return convertView;
    }
}
