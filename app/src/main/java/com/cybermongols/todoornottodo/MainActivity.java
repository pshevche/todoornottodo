package com.cybermongols.todoornottodo;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.cybermongols.todoornottodo.db.TaskDbHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TaskDbHelper mTaskDbHelper;
    private ListView mTaskListView;
    private TaskAdapter mTaskAdapter;
    private String mOrderBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTaskDbHelper = new TaskDbHelper(this);
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        mOrderBy = TaskContract.TaskEntry.COL_TASK_DEADLINE + " ASC, " + TaskContract.TaskEntry.COL_TASK_IMPORTANT + " DESC";

        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.add_task_dialog);
                dialog.setTitle("Add new task");
                final EditText taskEditText = (EditText) dialog.findViewById(R.id.task_edit_text);
                final CheckBox importantTaskCheckbox = (CheckBox) dialog.findViewById(R.id.important_task_checkbox);
                final DatePicker deadlineDatepicker = (DatePicker) dialog.findViewById(R.id.task_deadline);

                final Button addTaskButton = (Button) dialog.findViewById(R.id.add_task_button);
                addTaskButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String task = String.valueOf(taskEditText.getText());
                        boolean important = importantTaskCheckbox.isChecked();
                        Calendar c = Calendar.getInstance();
                        c.set(deadlineDatepicker.getYear(), deadlineDatepicker.getMonth(), deadlineDatepicker.getDayOfMonth());
                        long deadline = c.getTimeInMillis();
                        SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                        values.put(TaskContract.TaskEntry.COL_TASK_IMPORTANT, important);
                        values.put(TaskContract.TaskEntry.COL_TASK_DEADLINE, deadline);
                        db.insertWithOnConflict(TaskContract.TaskEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                        db.close();
                        dialog.cancel();
                        updateUI();
                    }
                });
                final Button closeDialogButton = (Button) dialog.findViewById(R.id.close_dialog_button);
                closeDialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });

                dialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateUI() {
        ArrayList<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = mTaskDbHelper.getReadableDatabase();
        Switch sortMode = (Switch) findViewById(R.id.sort_mode);
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID,
                        TaskContract.TaskEntry.COL_TASK_TITLE,
                        TaskContract.TaskEntry.COL_TASK_IMPORTANT,
                        TaskContract.TaskEntry.COL_TASK_DEADLINE},
                null,
                null,
                null,
                null,
                mOrderBy);
        while (cursor.moveToNext()) {
            int idxTitle = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            int idxImportant = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_IMPORTANT);
            int idxDeadline = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_DEADLINE);
            tasks.add(new Task(cursor.getString(idxTitle), cursor.getInt(idxImportant) != 0, cursor.getLong(idxDeadline)));
        }

        if (mTaskAdapter == null) {
            mTaskAdapter = new TaskAdapter(this, tasks);
            mTaskListView.setAdapter(mTaskAdapter);
        } else {
            mTaskAdapter.clear();
            mTaskAdapter.addAll(tasks);
            mTaskAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    public void completeTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();
        updateUI();
    }

    public void changeSortMode(View view) {
        mOrderBy = ((Switch) view).isChecked()
                ? TaskContract.TaskEntry.COL_TASK_IMPORTANT + " DESC, " + TaskContract.TaskEntry.COL_TASK_DEADLINE + " ASC"
                : TaskContract.TaskEntry.COL_TASK_DEADLINE + " ASC, " + TaskContract.TaskEntry.COL_TASK_IMPORTANT + " DESC";
        updateUI();
    }

}
