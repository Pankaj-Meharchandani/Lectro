package com.example.timetable.model;


import java.io.Serializable;

public class Note implements Serializable {
    private String title, text = "";
    private int id, color;
    private int subjectId = -1;

    public Note() {}

    public Note(String title, String text, int color) {
        this.title = title;
        this.text = text;
        this.color = color;
    }

    public Note(String title, String text, int color, int subjectId) {
        this.title = title;
        this.text = text;
        this.color = color;
        this.subjectId = subjectId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
}


