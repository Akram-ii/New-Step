package com.example.newstep.Models;

public class GoalModel {
    private int id,progress;
    private String title,desc;
public GoalModel(){}
    public GoalModel(int id, String title, String desc, int progress) {
        this.id = id;
        this.progress = progress;
        this.title = title;
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
