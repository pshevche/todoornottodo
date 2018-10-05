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

import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.cybermongols.todoornottodo.db.TaskDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity implements AdapterCallback {

    private static final String TAG = "MainActivity";

    private TaskDbHelper mTaskDbHelper;
    private ListView mTaskListView;
    private TaskAdapter mTaskAdapter;
    private String mOrderBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init helper to access database
        mTaskDbHelper = new TaskDbHelper(this);
        // init list view with swipe functionality
        mTaskListView = (SwipeMenuListView) findViewById(R.id.list_todo);
        // init sorting direction for the first db access
        mOrderBy = TaskContract.TaskEntry.COL_TASK_DEADLINE + " ASC, " + TaskContract.TaskEntry.COL_TASK_IMPORTANT + " DESC";
        // init list view
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        // render ui
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
                final EditText taskEditText = (EditText) dialog.findViewById(R.id.add_task_edit_text);
                final CheckBox importantTaskCheckbox = (CheckBox) dialog.findViewById(R.id.add_important_task_checkbox);
                final DatePicker deadlineDatepicker = (DatePicker) dialog.findViewById(R.id.add_task_deadline);

                final Button addTaskButton = (Button) dialog.findViewById(R.id.add_task_button);
                addTaskButton.setOnClickListener((v) -> {
                    String task = String.valueOf(taskEditText.getText());
                    boolean important = importantTaskCheckbox.isChecked();
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(deadlineDatepicker.getYear(), deadlineDatepicker.getMonth(), deadlineDatepicker.getDayOfMonth());
                    String deadline = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
                    SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                    values.put(TaskContract.TaskEntry.COL_TASK_IMPORTANT, important);
                    values.put(TaskContract.TaskEntry.COL_TASK_DEADLINE, deadline);
                    db.insertWithOnConflict(TaskContract.TaskEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    db.close();
                    dialog.cancel();
                    updateUI();
                });
                final Button closeDialogButton = (Button) dialog.findViewById(R.id.close_add_dialog_button);
                closeDialogButton.setOnClickListener((v) -> {
                        dialog.cancel();
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
            int idxId = cursor.getColumnIndex(TaskContract.TaskEntry._ID);
            int idxTitle = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            int idxImportant = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_IMPORTANT);
            int idxDeadline = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_DEADLINE);
            tasks.add(new Task(cursor.getInt(idxId), cursor.getString(idxTitle), cursor.getInt(idxImportant) != 0, cursor.getString(idxDeadline)));
        }

        if (mTaskAdapter == null) {
            mTaskAdapter = new TaskAdapter(this, tasks, this);
            mTaskListView.setAdapter(mTaskAdapter);
        } else {
            mTaskAdapter.clear();
            mTaskAdapter.addAll(tasks);
            mTaskAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    public void changeSortMode(View view) {
        mOrderBy = ((Switch) view).isChecked()
                ? TaskContract.TaskEntry.COL_TASK_IMPORTANT + " DESC, " + "date(" + TaskContract.TaskEntry.COL_TASK_DEADLINE + ") ASC"
                : "date(" + TaskContract.TaskEntry.COL_TASK_DEADLINE + ") ASC, " + TaskContract.TaskEntry.COL_TASK_IMPORTANT + " DESC";
        updateUI();
    }

    @Override
    public void completeTask(Task task) {
        String taskId = "" + task.getId();
        SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry._ID + " = ?",
                new String[]{taskId});
        db.close();
        updateUI();
    }

    @Override
    public void editTask(Task currentTask) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.edit_task_dialog);
        dialog.setTitle("Edit task");
        final EditText taskEditText = (EditText) dialog.findViewById(R.id.edit_task_edit_text);
        taskEditText.setText(currentTask.getTitle());
        final CheckBox importantTaskCheckbox = (CheckBox) dialog.findViewById(R.id.edit_important_task_checkbox);
        importantTaskCheckbox.setChecked(currentTask.isImportant());
        final DatePicker deadlineDatepicker = (DatePicker) dialog.findViewById(R.id.edit_task_deadline);
        Date currentDeadline = currentTask.getDeadline();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(currentDeadline);
        deadlineDatepicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        final Button addTaskButton = (Button) dialog.findViewById(R.id.save_task_button);
        addTaskButton.setOnClickListener((v) -> {
            String task = String.valueOf(taskEditText.getText());
            boolean important = importantTaskCheckbox.isChecked();
            Date d = new Date(deadlineDatepicker.getYear(), deadlineDatepicker.getMonth(), deadlineDatepicker.getDayOfMonth());
            String deadline = new SimpleDateFormat("yyyy-MM-dd").format(d);
            SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
            values.put(TaskContract.TaskEntry.COL_TASK_IMPORTANT, important);
            values.put(TaskContract.TaskEntry.COL_TASK_DEADLINE, deadline);
            db.update(TaskContract.TaskEntry.TABLE, values, TaskContract.TaskEntry._ID + "= ?", new String[]{"" + currentTask.getId()});
            db.close();
            dialog.cancel();
            updateUI();
        });
        final Button closeDialogButton = (Button) dialog.findViewById(R.id.close_edit_dialog_button);
        closeDialogButton.setOnClickListener((v) -> {
            dialog.cancel();
        });

        dialog.show();
    }

}
