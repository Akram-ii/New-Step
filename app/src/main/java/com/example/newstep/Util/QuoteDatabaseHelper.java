package com.example.newstep.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class QuoteDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "quotes.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_QUOTE = "quote";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_QUOTE = "quote";
    private static final String COLUMN_DATE = "date";

    public QuoteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_QUOTE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_QUOTE + " TEXT, " +
                COLUMN_DATE + " INTEGER)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUOTE);
        onCreate(db);
    }
    public void saveQuote(String quote) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_QUOTE, null, null);
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUOTE, quote);
        values.put(COLUMN_DATE, System.currentTimeMillis());
        db.insert(TABLE_QUOTE, null, values);
        db.close();

    }
    public String[] getQuote() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_QUOTE,
                new String[]{COLUMN_QUOTE, COLUMN_DATE},
                null, null, null, null,
                COLUMN_DATE + " DESC",
                "1"
        );

        String[] result = new String[2];
        if (cursor != null && cursor.moveToFirst()) {
            result[0] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUOTE));
            result[1] = String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
            cursor.close();
        } else {
            result[0] = "Write your quote";
            result[1] = String.valueOf(System.currentTimeMillis());
        }
        db.close();
        return result;
    }
}
