package com.example.newstep.Models;

public class HabitModel {

    int id;
    int totalDays;

    String habit_name,emergencyMsg,cat;

    public void setHabit_name(String habit_name) {
        this.habit_name = habit_name;
    }

    public HabitModel(String habitName, String cat, String emergencyMsg, int totalDays) {
        this.habit_name = habitName;
        this.emergencyMsg = emergencyMsg;
        this.cat = cat;
        this.totalDays = totalDays;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HabitModel(int id, String habitName, String cat, String emergencyMsg, int totalDays) {

        this.id=id;
        this.habit_name = habitName;
        this.emergencyMsg = emergencyMsg;
        this.cat = cat;
        this.totalDays = totalDays;
    }



    public String getHabit_name() {
        return habit_name;
    }



    public String getEmergencyMsg() {
        return emergencyMsg;
    }

    public void setEmergencyMsg(String emergencyMsg) {
        this.emergencyMsg = emergencyMsg;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }
}
