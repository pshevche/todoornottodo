package com.cybermongols.todoornottodo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class Task {
    private final int id;
    private final String title;
    private final boolean important;
    private Date deadline;

    public Task(int id, String title, boolean important, String deadline) {
        this.id = id;
        this.title = title;
        this.important = important;
        try {
            this.deadline = (new SimpleDateFormat("yyyy-MM-dd")).parse(deadline);
        } catch (ParseException e) {
            this.deadline = new Date();
        }
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

    public Date getDeadline() {
        return this.deadline;
    }
}
