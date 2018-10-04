package com.cybermongols.todoornottodo;

import android.provider.BaseColumns;

public class TaskContract {
    public static final String DB_NAME = "com.cybermongols.todoornottodo.db";
    public static final int DB_VERSION = 4;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "tasks";
        public static final String COL_TASK_TITLE = "title";
        public static final String COL_TASK_IMPORTANT = "important";
        public static final String COL_TASK_DEADLINE = "deadline";
    }
}
