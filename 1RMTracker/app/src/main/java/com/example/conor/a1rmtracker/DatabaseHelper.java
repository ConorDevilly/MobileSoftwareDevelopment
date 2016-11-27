package com.example.conor.a1rmtracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Creates Tables
 * Can only have one SQLiteOpenHelper for multiple tables.
 * Hence, its a public class (as opposed to nested) and all cols / creates are defined here
 * REFERENCE: http://blog.foxxtrot.net/2009/01/a-sqliteopenhelper-is-not-a-sqlitetablehelper.html
 * Created by Conor on 27/11/16.
 */
//REFERENCE: Course Notes: Database detailed class example for Lab 7
public class DatabaseHelper extends SQLiteOpenHelper {
    Context context;

    // DB Info
    private static final String DATABASE_NAME       = "1RM_Tracker";
    private static final int DATABASE_VERSION       = 1;

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) throws Resources.NotFoundException{
        db.execSQL(ExerciseManager.CREATE_TABLE);
        db.execSQL(FormulaManager.CREATE_TABLE);

        //Initial inserts into Formula table
        //Retrieve values from nested array.
        //REFERENCE: http://stackoverflow.com/questions/4326037/android-resource-array-of-arrays
        Resources res = context.getResources();
        TypedArray formulae = res.obtainTypedArray(R.array.formulae);

        for(int i = 0; i < formulae.length(); i++){
            int id = formulae.getResourceId(i, -1);

            if(id != -1) {
                String[] data = res.getStringArray(id);
                ContentValues vals = new ContentValues();

                vals.put(FormulaManager.KEY_NAME, data[0]);
                for (int j = 1; j < data.length; j++) {
                    vals.put(FormulaManager.numMap.get(j), data[j]);
                }
                db.insert(FormulaManager.TABLE_NAME, null, vals);

            }else{
                throw new Resources.NotFoundException("Formula not found");
            }
        }
        formulae.recycle();
        //END REFERENCE
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ExerciseManager.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FormulaManager.TABLE_NAME);
        onCreate(db);
    }
}
//END REFERENCE
