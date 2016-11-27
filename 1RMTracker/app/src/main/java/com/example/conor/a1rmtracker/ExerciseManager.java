package com.example.conor.a1rmtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.SQLException;

import java.util.ArrayList;

/**
 * Exercise Table Manager
 * Created by Conor on 27/11/16.
 */
public class ExerciseManager extends TableManager{
    //Table name
    public static final String TABLE_NAME   = "ExerciseLog";

    //List of cols
    public static final String KEY_ID              = "_id";
    public static final String KEY_DATE             = "date";
    public static final String KEY_EXERCISE         = "exercise";
    public static final String KEY_WEIGHT           = "weight";
    public static final String KEY_REPS             = "reps";
    public static final String KEY_ORM              = "orm";

    //Create
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_DATE + " TEXT NOT NULL, " +
                    KEY_EXERCISE + " TEXT NOT NULL, " +
                    KEY_WEIGHT + " REAL NOT NULL, " +
                    KEY_REPS + " INTEGER NOT NULL, " +
                    KEY_ORM + " REAL NOT NULL" +
                    ");";
    
    public ExerciseManager(Context ctx){
        super(ctx);
    }

    //Create an entry
    public long logExercise(String date, String exercise, float weight, int reps, float orm)
        throws SQLException{
        ContentValues vals = new ContentValues();
        vals.put(KEY_DATE, date);
        vals.put(KEY_EXERCISE, exercise);
        vals.put(KEY_WEIGHT, weight);
        vals.put(KEY_REPS, reps);
        vals.put(KEY_ORM, orm);
        return db.insert(TABLE_NAME, null, vals);
    }

    //TODO: Refactor. This is pretty much the same as logExercise.
    //Update an entry
    public int updateExercise(int id, String date, String exercise, float weight, int reps, float orm)
        throws SQLException{
        ContentValues vals = new ContentValues();
        vals.put(KEY_DATE, date);
        vals.put(KEY_EXERCISE, exercise);
        vals.put(KEY_WEIGHT, weight);
        vals.put(KEY_REPS, reps);
        vals.put(KEY_ORM, orm);
        return db.update(TABLE_NAME, vals, "_id = " + id, null);
    }

    //Delete an entry
    public int deleteExercise(int id) throws SQLException{
        return db.delete(TABLE_NAME, "_id = " + id, null);
    }

    public Cursor getAllExerciseDetails() throws SQLException{
        String query = "SELECT * FROM " + TABLE_NAME + ";";
        return db.rawQuery(query, null);
    }

    //Return all entries
    public Cursor getAllExerciseLogs() throws SQLException{
        return db.query(true, TABLE_NAME, new String[]{
                KEY_ID,
                KEY_DATE,
                KEY_EXERCISE,
                KEY_ORM
        }, null, null, null, null, "date(" + KEY_DATE + ") DESC", null);
    }

    //Return a specific entry
    public Cursor getExercise(int id) throws SQLException{
        return db.query(true, TABLE_NAME, new String[]{
                KEY_ID,
                KEY_WEIGHT,
                KEY_REPS,
                KEY_ORM,
                KEY_EXERCISE,
                KEY_DATE
        }, "_id = " + id, null, null, null, null, null);
    }

    //Get Max's for each exercies
    public Cursor getPRs(String[] exercises) throws SQLException{
        Cursor mergedCursor;
        ArrayList<Cursor> cursors = new ArrayList<>();

        for(String s : exercises){
            Cursor c =  db.query(true, TABLE_NAME, new String[]{
                    KEY_ID,
                    KEY_DATE,
                    KEY_EXERCISE,
                    KEY_WEIGHT,
                    KEY_REPS,
                    "MAX("+KEY_ORM+")"
            }, KEY_EXERCISE + " = '" + s + "'", null, null, null, null, null);
            cursors.add(c);
        }

        //Merge cursors
        Cursor[] cursors1 = new Cursor[cursors.size()];
        mergedCursor = new MergeCursor(cursors.toArray(cursors1));
        return mergedCursor;
    }

    //Return all entries for a given exercies
    public Cursor getAllExercise(String exercise) throws SQLException{
        return db.query(true, TABLE_NAME, new String[]{
                KEY_ID,
                KEY_DATE,
                KEY_EXERCISE,
                KEY_WEIGHT,
                KEY_REPS,
                KEY_ORM
        }, KEY_EXERCISE + " = '" + exercise + "'", null, null, null, "date(" + KEY_DATE + ") ASC", null);
    }

    //Delete all entries
    public void deleteAllExercises() throws SQLException{
        db.delete(TABLE_NAME, null, null);
    }
}
