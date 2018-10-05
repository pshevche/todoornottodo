package com.cybermongols.todoornottodo;

interface AdapterCallback {
    void completeTask(Task task);

    void editTask(Task currentTask);
}
