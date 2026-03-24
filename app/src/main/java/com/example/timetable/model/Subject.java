package com.example.timetable.model;

import java.io.Serializable;

public class Subject implements Serializable {
    private int id;
    private String name;
    private int color;
    private String teacher;
    private String room;
    private int attended;
    private int missed;
    private int skipped;

    public Subject() {}

    public Subject(String name, int color, String teacher, String room) {
        this.name = name;
        this.color = color;
        this.teacher = teacher;
        this.room = room;
    }

    public Subject(String name, int color, String teacher, String room, int attended, int missed, int skipped) {
        this.name = name;
        this.color = color;
        this.teacher = teacher;
        this.room = room;
        this.attended = attended;
        this.missed = missed;
        this.skipped = skipped;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getAttended() {
        return attended;
    }

    public void setAttended(int attended) {
        this.attended = attended;
    }

    public int getMissed() {
        return missed;
    }

    public void setMissed(int missed) {
        this.missed = missed;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }
}


