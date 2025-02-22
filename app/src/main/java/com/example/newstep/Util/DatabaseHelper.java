package com.example.newstep.Util;

import static android.app.DownloadManager.COLUMN_ID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;


import com.example.newstep.Models.DailyNoteModel;
import com.example.newstep.Models.HabitModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "habit_tracker.db";
    private static final int DATABASE_VERSION = 3;

    // Habit Table
    private static final String TABLE_HABITS = "habits";
    private static final String COLUMN_HABIT_ID = "id";
    private static final String COLUMN_HABIT_NAME = "name";
    private static final String COLUMN_HABIT_CATEGORY = "category";
    private static final String COLUMN_HABIT_EMERGENCY_MSG = "emergency_message";
    private static final String COLUMN_HABIT_DAYS_RESISTED = "days_resisted";

    // Daily Entries Table
    private static final String TABLE_NOTES = "notes";
    private static final String COLUMN_NOTE_ID = "note_id";

    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_NOTE = "note";
    private static final String COLUMN_MOOD = "mood";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createHabitsTable = "CREATE TABLE " + TABLE_HABITS + " (" +
                COLUMN_HABIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HABIT_NAME + " TEXT, " +
                COLUMN_HABIT_CATEGORY + " TEXT, " +
                COLUMN_HABIT_EMERGENCY_MSG + " TEXT, " +
                COLUMN_HABIT_DAYS_RESISTED + " INTEGER DEFAULT 0)";

        // Create Notes Table (with fixed Foreign Key)
        String createNotesTable = "CREATE TABLE " + TABLE_NOTES + " (" +
                COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HABIT_ID + " INTEGER, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_NOTE + " TEXT, " +
                COLUMN_MOOD + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_HABIT_ID + ") REFERENCES " + TABLE_HABITS + "(" + COLUMN_HABIT_ID + ") ON DELETE CASCADE)";

        db.execSQL(createHabitsTable);
        db.execSQL(createNotesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }
    public void updateDailyNote(int noteId, String newNote, String newMood) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("note", newNote);
        values.put("mood", newMood);

        db.update("notes", values, "note_id = ?", new String[]{String.valueOf(noteId)});
        db.close();
    }
    public long addHabit(HabitModel habit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_NAME, habit.getHabit_name());
        values.put(COLUMN_HABIT_CATEGORY, habit.getCat());
        values.put(COLUMN_HABIT_EMERGENCY_MSG, habit.getEmergencyMsg());
        values.put(COLUMN_HABIT_DAYS_RESISTED, habit.getTotalDays());
        long id = db.insert(TABLE_HABITS, null, values);
        db.close();
        return id;
    }
    public boolean hasNoteForToday(int habitId, String todayDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_NOTES +
                        " WHERE " + COLUMN_HABIT_ID + " = ? AND " + COLUMN_DATE + " = ?",
                new String[]{String.valueOf(habitId), todayDate}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }
    public void updateDaysResisted(int habitId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int newDaysResisted = 1;

        Cursor cursor = db.rawQuery("SELECT " + COLUMN_HABIT_DAYS_RESISTED +
                        " FROM " + TABLE_HABITS +
                        " WHERE " + COLUMN_HABIT_ID + " = ?",
                new String[]{String.valueOf(habitId)});
        if (cursor.moveToFirst()) {
            newDaysResisted = cursor.getInt(0) + 1;
        }
        cursor.close();
        db.close();
        SQLiteDatabase writeDb = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_DAYS_RESISTED, newDaysResisted);

        writeDb.update(TABLE_HABITS, values, COLUMN_HABIT_ID + " = ?", new String[]{String.valueOf(habitId)});
        writeDb.close();
    }

    public void insertDailyNote(int habitId, String date, String note, String mood) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_ID, habitId);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_NOTE, note);
        values.put(COLUMN_MOOD, mood);
        long result = db.insert(TABLE_NOTES, null, values);
        db.close();

        if (result == -1) {

            Log.e("DB_ERROR", "Failed to insert daily note!");
        } else {
            updateDaysResisted(habitId);
            Log.d("DB_SUCCESS", "Note inserted for habit ID: " + habitId);
        }
    }
    public List<DailyNoteModel> getNotesForHabit(int habitId) {
        List<DailyNoteModel> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Log.d("DB_QUERY", "Fetching notes for habit ID: " + habitId);

        Cursor cursor = db.query(TABLE_NOTES,
                new String[]{COLUMN_NOTE_ID, COLUMN_HABIT_ID, COLUMN_DATE, COLUMN_NOTE, COLUMN_MOOD},
                COLUMN_HABIT_ID + "=?",
                new String[]{String.valueOf(habitId)},
                null, null, COLUMN_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                int noteId = cursor.getInt(0);
                int retrievedHabitId = cursor.getInt(1);
                String date = cursor.getString(2);
                String note = cursor.getString(3);
                String mood = cursor.getString(4);

                notes.add(new DailyNoteModel(noteId, retrievedHabitId, date, note, mood));
            } while (cursor.moveToNext());
        } else {
            Log.d("DB_DATA", "No notes found for habit ID: " + habitId);
        }

        cursor.close();
        db.close();
        return notes;
    }
    public List<HabitModel> getAllHabits() {
        List<HabitModel> habits = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HABITS, null);

        if (cursor.moveToFirst()) {
            do {
                HabitModel habit = new HabitModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("emergency_message")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("days_resisted"))
                );
                habits.add(habit);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return habits;
    }

    public void deleteHabit(int habitId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("habits", "id = ?", new String[]{String.valueOf(habitId)});
        db.close();
    }
    public void deleteEverything() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_HABITS);
        db.execSQL("DELETE FROM " + TABLE_NOTES);
        db.close();
        Log.d("DB_SUCCESS", "All database data deleted.");
    }


}

