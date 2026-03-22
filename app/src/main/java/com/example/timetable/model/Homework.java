package com.example.timetable.model;
public class Homework {
    private String subject, title, description, date;
    private int id, color;
    private int completed; // 0 for false, 1 for true

    public Homework() {}

    public Homework(String subject, String title, String description, String date, int color, int completed) {
        this.subject = subject;
        this.title = title;
        this.description = description;
        this.date = date;
        this.color = color;
        this.completed = completed;
    }
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }
}
