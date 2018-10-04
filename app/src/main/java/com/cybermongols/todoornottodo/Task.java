package com.cybermongols.todoornottodo;

public class Task {
    private String title;
    private boolean important;
    private long deadline;

    public Task(String title, boolean important, long deadline) {
        this.title = title;
        this.important = important;
        this.deadline = deadline;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean isImportant() {
        return this.important;
    }

    public long getDeadline() {
        return this.deadline;
    }
}
