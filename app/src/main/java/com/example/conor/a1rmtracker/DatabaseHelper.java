package com.example.conor.a1rmtracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.SparseArray;

/**
 * Created by Conor on 15/11/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    Context context;

    // DB Info
    private static final String DATABASE_NAME       = "1RM_Tracker";
    private static final int DATABASE_VERSION       = 1;

    //List of tables
    private static final String TABLE_FORMULA       = "Formula";
    private static final String TABLE_EXERCISELOG   = "ExerciseLog";

    //Column List
    private static final String KEY_ID              = "_id";
    private static final String KEY_NAME            = "Name";
    public static final String KEY_DATE             = "date";
    public static final String KEY_EXERCISE         = "exercise";
    public static final String KEY_WEIGHT           = "weight";
    public static final String KEY_REPS             = "reps";

    //Sparse array to map int -> string equivalent
    //SparseArray: More efficient HashMap for mapping ints -> objects. Suggested by Android Studio when I tried using a HashMap
    public static final SparseArray<String> numMap = new SparseArray<String>() {{
        put(1, "One");
        put(2, "Two");
        put(3, "Three");
        put(4, "Four");
        put(5, "Five");
        put(6, "Six");
        put(7, "Seven");
        put(8, "Eight");
        put(9, "Nine");
        put(10, "Ten");
    }};

    //Creates
    private static final String CREATE_TABLE_FORMULA =
            "CREATE TABLE " + TABLE_FORMULA + "(" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_NAME + " TEXT UNIQUE NOT NULL, " +
                    numMap.get(1) + " REAL, " +
                    numMap.get(2) + " REAL, " +
                    numMap.get(3) + " REAL, " +
                    numMap.get(4) + " REAL, " +
                    numMap.get(5) + " REAL, " +
                    numMap.get(6) + " REAL, " +
                    numMap.get(7) + " REAL, " +
                    numMap.get(8) + " REAL, " +
                    numMap.get(9) + " REAL, " +
                    numMap.get(10) + " REAL" +
                    ");";
    private static final String CREATE_TABLE_EXERCISELOG =
            "CREATE TABLE " + TABLE_EXERCISELOG + "(" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_DATE + " TEXT NOT NULL, " +
                    KEY_EXERCISE + " TEXT NOT NULL, " +
                    KEY_WEIGHT + " REAL NOT NULL, " +
                    KEY_REPS + " INTEGER NOT NULL" +
                    ");";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE_EXERCISELOG);
        db.execSQL(CREATE_TABLE_FORMULA);

        //Initial inserts into Formula table
        //Retrieve values from nested array. Ref: http://stackoverflow.com/questions/4326037/android-resource-array-of-arrays
        Resources res = context.getResources();
        TypedArray formulae = res.obtainTypedArray(R.array.formulae);

        for(int i = 0; i < formulae.length(); i++){
            int id = formulae.getResourceId(i, -1);

            if(id != -1) {
                String[] data = res.getStringArray(id);
                ContentValues vals = new ContentValues();

                vals.put(KEY_NAME, data[0]);
                for (int j = 1; j < data.length; j++) {
                    vals.put(numMap.get(j), data[j]);
                }
                db.insert(TABLE_FORMULA, null, vals);

            }else{
                //TODO: Throw exception
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISELOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FORMULA);
        onCreate(db);
    }

    /*****  EXERCISE METHODS    ******/
    public long logExercise(String date, String exercise, float weight, int reps){
        ContentValues vals = new ContentValues();
        vals.put(KEY_DATE, date);
        vals.put(KEY_EXERCISE, exercise);
        vals.put(KEY_WEIGHT, weight);
        vals.put(KEY_REPS, reps);
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_EXERCISELOG, null, vals);
    }

    public Cursor getAllExerciseLogs(){
        String query = "SELECT * FROM " + TABLE_EXERCISELOG;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        return c;
    }

    /*****  FORMULA METHODS *****/
    //Get the average percent for a given rep
    public float getAvg(int rep){
        float ret = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT AVG(" + numMap.get(rep) + ") FROM " + TABLE_FORMULA;
        Cursor res = db.rawQuery(query, null);

        if(res != null){
            res.moveToFirst();
            ret = res.getFloat(0);
        }else{
            //Throw exception
        }

        return ret;
    }
}
