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
import android.widget.TextView;

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

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(170);
                // set item title
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(170);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        mTaskListView.setMenuCreator(creator);

        mTaskListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        Log.d(TAG, "onMenuItemClick: " + index);
                        break;
                    case 1:
                        Log.d(TAG, "onMenuItemClick: " + index);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
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
