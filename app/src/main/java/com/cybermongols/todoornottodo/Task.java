package com.cybermongols.todoornottodo;

public class Task {
    private int id;
    private String title;
    private boolean important;
    private long deadline;

    public Task(int id, String title, boolean important, long deadline) {
        this.id = id;
        this.title = title;
        this.important = important;
        this.deadline = deadline;
    }

    public int getId() {
        return this.id;
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
