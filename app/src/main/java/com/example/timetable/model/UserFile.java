package com.example.timetable.model;

public class UserFile {
    private int id;
    private String title, path;

    public UserFile() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}
