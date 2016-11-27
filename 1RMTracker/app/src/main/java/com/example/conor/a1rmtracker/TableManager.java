package com.example.conor.a1rmtracker;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Generic class for managing tables
 * Created by Conor on 27/11/16.
 */
public abstract class TableManager {
    protected SQLiteDatabase db;
    protected DatabaseHelper dbHelper;

    public TableManager(Context ctx){
        dbHelper = new DatabaseHelper(ctx);
    }

    public TableManager openWriteable() throws SQLException{
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public TableManager openReadable() throws SQLException{
        db = dbHelper.getReadableDatabase();
        return this;
    }

    public void close() throws SQLException{
        dbHelper.close();
    }
}
