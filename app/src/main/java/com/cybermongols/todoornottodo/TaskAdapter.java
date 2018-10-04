package com.cybermongols.todoornottodo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TaskAdapter extends ArrayAdapter<Task> {

    public TaskAdapter(Context context, ArrayList<Task> items) {
        super(context, 0, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Task task = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_todo, parent, false);
        }
        TextView task_title = convertView.findViewById(R.id.task_title);
        TextView task_deadline = convertView.findViewById(R.id.task_deadline);
        task_title.setText(task.getTitle());
        Date deadline = new Date(task.getDeadline());
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        task_deadline.setText(df.format(deadline));
        if (task.isImportant()) {
            convertView.setBackgroundColor(Color.rgb(0xd8, 0x1b, 0x60));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }
}
