package com.example.newstep.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.newstep.Models.GoalModel;

import java.util.ArrayList;
import java.util.List;

public class MyGoalsDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "goals.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_GOALS = "goals";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_PROGRESS = "progress";
    private static final String COLUMN_ICON = "icon";

    public MyGoalsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_GOALS_TABLE = "CREATE TABLE " + TABLE_GOALS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_PROGRESS + " INTEGER,"
                + COLUMN_ICON + " TEXT"
                + ")";
        db.execSQL(CREATE_GOALS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        onCreate(db);
    }

    public void addGoal(GoalModel goal) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, goal.getTitle());
        values.put(COLUMN_DESCRIPTION, goal.getDesc());
        values.put(COLUMN_PROGRESS, goal.getProgress());
        values.put(COLUMN_ICON, goal.getIcon());

        db.insert(TABLE_GOALS, null, values);
        db.close();
    }

    public List<GoalModel> getAllGoals() {
        List<GoalModel> goalList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GOALS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                GoalModel goal = new GoalModel();
                goal.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                goal.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                goal.setDesc(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                goal.setProgress(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS)));
                goal.setIcon(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ICON)));
                goalList.add(goal);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return goalList;
    }

    public void updateGoal(int goalId, String newTitle, String newDescription, int newProgress,String newIcon) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, newTitle);
        values.put(COLUMN_DESCRIPTION, newDescription);
        values.put(COLUMN_PROGRESS, newProgress);
        values.put(COLUMN_ICON, newIcon);

        db.update(TABLE_GOALS, values, COLUMN_ID + "=?", new String[]{String.valueOf(goalId)});
        db.close();
    }
    public void deleteGoal(int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GOALS, COLUMN_ID + "=?", new String[]{String.valueOf(goalId)});
        db.close();
    }
    public int getTotalGoals() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM goals", null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    public int getCompletedGoals() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM goals WHERE progress >= 100", null);
        int completed = 0;
        if (cursor.moveToFirst()) {
            completed = cursor.getInt(0);
        }
        cursor.close();
        return completed;
    }

    public int getInProgressGoals() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM goals WHERE progress < 100", null);
        int inProgress = 0;
        if (cursor.moveToFirst()) {
            inProgress = cursor.getInt(0);
        }
        cursor.close();
        return inProgress;
    }

    public int getAverageProgress() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT AVG(progress) FROM goals", null);
        int avg = 0;
        if (cursor.moveToFirst()) {
            avg = cursor.getInt(0);
        }
        cursor.close();
        return avg;
    }
}