package com.example.conor.a1rmtracker;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.SparseArray;

/**
 * Manager for Formula Table
 * Created by Conor on 27/11/16.
 */
public class FormulaManager extends TableManager{

    //Table Name
    public static final String TABLE_NAME       = "Formula";

    //Column List
    public static final String KEY_ID              = "_id";
    public static final String KEY_NAME            = "Name";

    //SparseArray: More efficient HashMap for mapping ints -> objects.
    //Suggested by Android Studio when I tried using a HashMap
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

    //Create
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
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

    //Constructor
    public FormulaManager(Context ctx){
        super(ctx);
    }

    //Get the average percent for a given rep
    public float getAvg(int rep) throws SQLException{
        float ret = -1;

        String query =
                "SELECT AVG(" + numMap.get(rep) + ") " +
                "FROM " + TABLE_NAME;

        Cursor res = db.rawQuery(query, null);

        if(res != null){
            res.moveToFirst();
            ret = res.getFloat(0);
            res.close();
        }

        return ret;
    }
}
