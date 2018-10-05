package com.cybermongols.todoornottodo;

public interface AdapterCallback {
    void completeTask(Task task);

    void editTask(Task currentTask);
}
