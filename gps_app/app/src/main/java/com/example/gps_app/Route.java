package com.example.gps_app;

import android.app.Application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Route extends Application implements Serializable {
    private int ID;
    private String description;
    private String name;
    private ArrayList<Task> taskList;
    private Date date;
    private int stars;
    private int startCount;

    public Route(int id, String name, String description, Date date, ArrayList<Task> tasks, int stars, int startCount) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.taskList = tasks;
        this.stars = stars;
        this.ID = id;
        this.startCount = startCount;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public ArrayList<Task> getTasks() {
        return taskList;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public void setID(int id) {
        this.ID = id;
    }

    public int getID() {
        return ID;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartCount() {
        return startCount;
    }

    public void setStartCount(int startCount) {
        this.startCount = startCount;
    }

    public void setTaskList(ArrayList<Task> taskList) { this.taskList = taskList; }

    public void setDate(Date date) { this.date = date; }
}

