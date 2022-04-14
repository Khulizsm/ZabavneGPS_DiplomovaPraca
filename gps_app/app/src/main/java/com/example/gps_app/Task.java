package com.example.gps_app;

import android.app.Application;

import java.io.Serializable;
import java.util.ArrayList;

public class Task extends Application implements Serializable {

    double[] TargetCoordinates;
    String TargetDescription;
    ArrayList<Answer> answerArrayList;
    String TaskQuestion;
    int taskID;

    public Task(int id, double[] point, ArrayList<Answer> task, String otazka) {

        this.TargetCoordinates = point;
        this.answerArrayList = task;
        this.TaskQuestion = otazka;
        this.taskID = id;
    }

    public double[] getTargetCoordinates() {
        return TargetCoordinates;
    }

    public String getTaskQuestion() {
        return TaskQuestion;
    }

    public void setTaskQuestion(String taskQuestion) {
        this.TaskQuestion = taskQuestion;
    }

    public ArrayList<Answer> getAnswerArrayList() {
        return answerArrayList;
    }

    public void setAnswerArrayList(ArrayList<Answer> answerArrayList) {
        this.answerArrayList = answerArrayList;
    }

    public String getText() {
        return TargetDescription;
    }

    public void setText(String text) {
        TargetDescription = text;
    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }
}
