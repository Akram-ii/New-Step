package com.example.newstep.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.example.newstep.Models.DailyNoteModel;
import com.example.newstep.Models.HabitModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyHabitsDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "habit_tracker.db";
    private static final int DATABASE_VERSION = 4;

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
    private static final String COLUMN_RESISTED = "resisted";

    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_NOTE = "note";
    private static final String COLUMN_MOOD = "mood";
    public MyHabitsDatabaseHelper(Context context) {
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


        String createNotesTable = "CREATE TABLE " + TABLE_NOTES + " (" +
                COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HABIT_ID + " INTEGER, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_NOTE + " TEXT, " +
                COLUMN_MOOD + " TEXT, " +
                "resisted INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COLUMN_HABIT_ID + ") REFERENCES " + TABLE_HABITS + "(" + COLUMN_HABIT_ID + ") ON DELETE CASCADE)";

        db.execSQL(createHabitsTable);
        db.execSQL(createNotesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN resisted INTEGER DEFAULT 0");
        }
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
    public void allResisted(){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("resisted", 1);
        db.update("notes", values, null, null); // updates all rows
        db.close();
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

    public void insertDailyNote(int habitId, String date, String note, String mood, boolean resisted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_ID, habitId);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_NOTE, note);
        values.put(COLUMN_MOOD, mood);
        values.put(COLUMN_RESISTED, resisted ? 1 : 0);  // store boolean as integer

        long result = db.insert(TABLE_NOTES, null, values);
        db.close();

        if (result == -1) {
            Log.e("DB_ERROR", "Failed to insert daily note!");
        } else {
            if (resisted) updateDaysResisted(habitId);
            Log.d("DB_SUCCESS", "Note inserted for habit ID: " + habitId);
        }
    }

    public Set<String> getLoggedDatesForHabit(int habitId) {
        Set<String> dates = new HashSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT date FROM notes WHERE id = ?",
                new String[]{String.valueOf(habitId)}
        );

        if (cursor.moveToFirst()) {
            do {
                dates.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return dates;
    }
    public String getEmergencyMessage(int habitId) {
        String message = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_HABITS,
                new String[]{COLUMN_HABIT_EMERGENCY_MSG},
                COLUMN_HABIT_ID + " = ?",
                new String[]{String.valueOf(habitId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_EMERGENCY_MSG));
        }

        cursor.close();
        db.close();
        return message;
    }
    public void updateEmergencyMessage(int habitId, String newMessage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_EMERGENCY_MSG, newMessage);

        db.update(TABLE_HABITS, values, COLUMN_HABIT_ID + " = ?", new String[]{String.valueOf(habitId)});
        db.close();
    }
    public List<DailyNoteModel> getNotesForHabit(int habitId) {
        List<DailyNoteModel> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Log.d("DB_QUERY", "Fetching notes for habit ID: " + habitId);

        Cursor cursor = db.query(TABLE_NOTES,
                new String[]{COLUMN_NOTE_ID, COLUMN_HABIT_ID, COLUMN_DATE, COLUMN_NOTE, COLUMN_MOOD, COLUMN_RESISTED},
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
                boolean resisted = cursor.getInt(5) == 1;

                DailyNoteModel model = new DailyNoteModel(noteId, retrievedHabitId, date, note, mood);
                model.setResisted(resisted);
                notes.add(model);
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

