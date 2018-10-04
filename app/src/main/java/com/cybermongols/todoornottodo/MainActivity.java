package com.cybermongols.todoornottodo;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.cybermongols.todoornottodo.db.TaskDbHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TaskDbHelper mTaskDbHelper;
    private SwipeMenuListView mTaskListView;
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
        initSwipeListView();
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
                final EditText taskEditText = (EditText) dialog.findViewById(R.id.task_edit_text);
                final CheckBox importantTaskCheckbox = (CheckBox) dialog.findViewById(R.id.important_task_checkbox);
                final DatePicker deadlineDatepicker = (DatePicker) dialog.findViewById(R.id.task_deadline);

                final Button addTaskButton = (Button) dialog.findViewById(R.id.add_task_button);
                addTaskButton.setOnClickListener((v) -> {
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
                });
                final Button closeDialogButton = (Button) dialog.findViewById(R.id.close_dialog_button);
                closeDialogButton.setOnClickListener((v) -> {
                        dialog.cancel();
                });

                dialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initSwipeListView() {
        SwipeMenuCreator creator = (menu) -> {
            // create "complete" item
            SwipeMenuItem completeItem = new SwipeMenuItem(getApplicationContext());
            // set item background
            completeItem.setBackground(new ColorDrawable(Color.rgb(0x5c, 0xd6, 0x5c)));
            // set item width
            completeItem.setWidth((int) Math.floor(findViewById(R.id.list_todo).getWidth() * 0.25));
            // set item's icon
            completeItem.setIcon(R.drawable.ic_complete_icon);
            // add to menu
            menu.addMenuItem(completeItem);

//            // create "edit" item
//            SwipeMenuItem editItem = new SwipeMenuItem(getApplicationContext());
//            // set item's background
//            editItem.setBackground(new ColorDrawable(Color.rgb(0x4d,0xa6, 0xff)));
//            // set item's width
//            editItem.setWidth((int)Math.ceil(findViewById(R.id.list_todo).getWidth() * 0.25));
//            // set item's icon
//            editItem.setIcon(R.drawable.ic_edit_icon);
//            // add to menu
//            menu.addMenuItem(editItem);
        };

        mTaskListView.setMenuCreator(creator);

        mTaskListView.setOnMenuItemClickListener((int position, SwipeMenu menu, int index) -> {
            switch (index) {
                case 0:
                    completeTask(position);
                    break;
                case 1:
                    Log.d(TAG, "Edit Task: not implemented");
                    break;
            }
            // false : close the menu; true : not close the menu
            return false;
        });
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
            tasks.add(new Task(cursor.getInt(idxId), cursor.getString(idxTitle), cursor.getInt(idxImportant) != 0, cursor.getLong(idxDeadline)));
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

    public void changeSortMode(View view) {
        mOrderBy = ((Switch) view).isChecked()
                ? TaskContract.TaskEntry.COL_TASK_IMPORTANT + " DESC, " + TaskContract.TaskEntry.COL_TASK_DEADLINE + " ASC"
                : TaskContract.TaskEntry.COL_TASK_DEADLINE + " ASC, " + TaskContract.TaskEntry.COL_TASK_IMPORTANT + " DESC";
        updateUI();
    }

    private void completeTask(int position) {
        String taskId = "" + mTaskAdapter.getItem(position).getId();
        SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry._ID + " = ?",
                new String[]{taskId});
        db.close();
        updateUI();
    }

}
