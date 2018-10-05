package com.cybermongols.todoornottodo;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.List;

class TaskAdapter extends ArrayAdapter<Task> {

    private final AdapterCallback mListener;

    public TaskAdapter(Context context, List<Task> tasks, AdapterCallback listener) {
        super(context, 0, tasks);
        mListener = listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public View getView(int position, View convertView, ViewGroup parent) {
        Task task = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_todo, parent, false);
        }
        // inflate view: date + title
        TextView task_title = convertView.findViewById(R.id.task_title);
        TextView task_deadline = convertView.findViewById(R.id.task_deadline);
        task_title.setText(task != null ? task.getTitle() : "");
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        task_deadline.setText(df.format(task.getDeadline()));
        if (task.isImportant()) {
            convertView.setBackground(getContext().getDrawable(R.drawable.important_task_background));
        } else {
            convertView.setBackground(getContext().getDrawable(R.drawable.normal_task_background));
        }

        View overflow = convertView.findViewById(R.id.task_overflow);
        overflow.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this.getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.overflow_menu, popupMenu.getMenu());
            // Force icons to show
            Object menuHelper;
            Class[] argTypes;
            try {
                //noinspection JavaReflectionMemberAccess
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
