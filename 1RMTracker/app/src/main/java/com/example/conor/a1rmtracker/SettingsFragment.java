package com.example.conor.a1rmtracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Settings screen
 * Allows users to reset, import and export their data.
 * Created by Conor on 22/11/16.
 */
public class SettingsFragment extends Fragment {
    ExerciseManager exMngr;
    SharedPreferences prefs;
    String name;

    private static final int PERM_EXTERNAL_STOREAGE = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        final Context ctx = getContext();

        exMngr = new ExerciseManager(getContext());

        //Get name from shared pref & display on screen
        prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        name = prefs.getString("name", "");
        TextView nameEntry = (TextView) v.findViewById(R.id.nameEntry);
        nameEntry.setText(name);

        Button clearRecords = (Button) v.findViewById(R.id.clearRecords);
        clearRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you wish to delete all records?");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exMngr = (ExerciseManager) exMngr.openWriteable();
                        exMngr.deleteAllExercises();
                        //db.deleteAllExercises();
                        Toast.makeText(getContext(), "Exercises deleted", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Cancel", null);

                builder.show();
            }
        });

        Button exportBtn = (Button) v.findViewById(R.id.exportBtn);
        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you wish to export data to " + name + ".csv?");

                builder.setPositiveButton("Export", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = prefs.getString("name", null);

                        if(name == null){
                            Log.e("1RM_Tracker", "No name for csv file");
                            Toast.makeText(ctx, "ERROR: No name for CSV file", Toast.LENGTH_SHORT).show();
                        }else{
                            new ExportDB().execute(name);
                        }
                    }
                });

                builder.setNegativeButton("Cancel", null);

                builder.show();
            }
        });

        Button importBtn = (Button) v.findViewById(R.id.importBtn);
        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = prefs.getString("name", null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you wish to import " + name + ".csv?");

                builder.setPositiveButton("Import", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(name == null){
                            Log.e("1RM_Tracker", "Could not find a name for the CSV file");
                            Toast.makeText(ctx, "ERROR: Could not find a name for the CSV file", Toast.LENGTH_SHORT).show();
                        }else{
                            new ImportDB().execute(name);
                        }
                    }
                });

                builder.setNegativeButton("Cancel", null);

                builder.show();
            }
        });

        Button changeBtn = (Button) v.findViewById(R.id.changeBtn);
        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //REFERENCE: http://stackoverflow.com/questions/10903754/input-text-dialog-android
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                final EditText input = new EditText(ctx);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(name);

                builder.setTitle("Enter name");
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        name = input.getText().toString();

                        //REFERENCE: https://developer.android.com/training/basics/data-storage/shared-preferences.html
                        SharedPreferences.Editor ed = prefs.edit();
                        ed.putString("name", name);
                        ed.apply();
                        //END REFERENCE

                        TextView nameEntry = (TextView) getView().findViewById(R.id.nameEntry);
                        if(nameEntry != null) {
                            nameEntry.setText(name);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                //END REFERENCE


            }
        });

        //Permissions check if API over 23
        //REFERENCE: https://developer.android.com/training/permissions/requesting.html
        Activity a = getActivity();
        int writePerm = ContextCompat.checkSelfPermission(a, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPerm =  ContextCompat.checkSelfPermission(a, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePerm != PackageManager.PERMISSION_GRANTED
                || readPerm != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERM_EXTERNAL_STOREAGE);
        }

        return v;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERM_EXTERNAL_STOREAGE: {
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(),
                            "ERROR: Importing / exporting will not work without permissions",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    //END REFERENCE

    //REFERENCE: https://developer.android.com/reference/android/os/AsyncTask.html
    private class ExportDB extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            if (strings.length != 1) {
                Log.e(MainActivity.LOG_TAG, "ERROR: Invalid number of args passed to ExportDB");
            }
            String prefix = strings[0];

            //REFERENCE: Taken from a mixture of a number of different places, mainly:
            //http://stackoverflow.com/questions/31367270/exporting-sqlite-database-to-csv-file-in-android
            File storage = Environment.getExternalStorageDirectory();
            File csvFile = new File(storage, prefix + ".csv");

            //Cursor recs = db.getAllExerciseDetails();
            exMngr = (ExerciseManager) exMngr.openReadable();
            Cursor recs = exMngr.getAllExerciseDetails();
            int weightCol = recs.getColumnIndex(ExerciseManager.KEY_WEIGHT);
            int repsCol = recs.getColumnIndex(ExerciseManager.KEY_REPS);
            int ormCol = recs.getColumnIndex(ExerciseManager.KEY_ORM);
            int exCol = recs.getColumnIndex(ExerciseManager.KEY_EXERCISE);
            int dateCol = recs.getColumnIndex(ExerciseManager.KEY_DATE);

            try {
                PrintWriter writer = new PrintWriter(new FileWriter(csvFile));

                while (recs.moveToNext()) {
                    String date = recs.getString(dateCol);
                    String exercise = recs.getString(exCol);
                    String weight = Float.toString(recs.getFloat(weightCol));
                    String reps = Integer.toString(recs.getInt(repsCol));
                    String orm = Float.toString(recs.getFloat(ormCol));

                    String row = date + "," + exercise + "," + weight + "," + reps + "," + orm;
                    writer.println(row);
                }

                recs.close();
                writer.close();
                exMngr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //END REFERENCE
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Toast.makeText(getContext(), "DB export complete", Toast.LENGTH_SHORT).show();
        }
    }

    private class ImportDB extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... strings) {
            if (strings.length != 1) {
                Log.e(MainActivity.LOG_TAG, "ERROR: Invalid number of args passed to ImportDB");
            }
            String prefix = strings[0];

            File storage = Environment.getExternalStorageDirectory();
            File csvFile = new File(storage, prefix + ".csv");
            exMngr = (ExerciseManager) exMngr.openWriteable();

            try {
                BufferedReader br = new BufferedReader(new FileReader(csvFile));

                String row;
                while ((row = br.readLine()) != null) {
                    String[] cols = row.split(",");
                    if (cols.length != 5) {
                        Log.e(MainActivity.LOG_TAG, "Incorrect file format when importing db");
                        return null;
                    }

                    String date = cols[0];
                    String exercise = cols[1];
                    float weight = Float.parseFloat(cols[2]);
                    int reps = Integer.parseInt(cols[3]);
                    float orm = Float.parseFloat(cols[4]);
                    exMngr.logExercise(date, exercise, weight, reps, orm);
                }

                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            exMngr.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Toast.makeText(getContext(), "DB import complete", Toast.LENGTH_SHORT).show();
        }

    }
    //END REFERENCE

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if(exMngr != null){
            exMngr.close();
        }
    }
}