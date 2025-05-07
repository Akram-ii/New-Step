package com.example.newstep.Models;



public class DailyNoteModel {
    private int id, habitId;
    private String date, note, mood;
    private Boolean resisted;
    private int number;


    public Boolean getResisted() {
        return resisted;
    }

    public void setResisted(Boolean resisted) {
        this.resisted = resisted;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public DailyNoteModel(int id, int habitId, String date, String note, String mood, int number) {
        this.id = id;
        this.habitId = habitId;
        this.date = date;
        this.note = note;
        this.mood = mood;
        this.number = number;
    }

    public DailyNoteModel(String date, String note, String mood) {
        this.date = date;
        this.note = note;
        this.mood = mood;
    }

    public DailyNoteModel(int id, int habitId, String date, String note, String mood) {
        this.id = id;
        this.habitId = habitId;
        this.date = date;
        this.note = note;
        this.mood = mood;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHabitId() {
        return habitId;
    }

    public void setHabitId(int habitId) {
        this.habitId = habitId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }
    public int getId() {
        return id;
    }
    public void setNote(String note) {
        this.note = note;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }
}
