package com.example.newstep.Models;

public class BadgeModel {
    private String name;
    private int imageId,points;

    public BadgeModel(String name, int points,int imageId) {
        this.name = name;
        this.imageId = imageId;
        this.points=points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
