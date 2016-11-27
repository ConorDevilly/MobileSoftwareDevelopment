package com.example.conor.a1rmtracker;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment used to calculate and store a 1 rep maxes.
 * Created by Conor on 16/11/16.
 */
public class CalcFragment extends Fragment {
    /*
    We differentiate between a 'view' format and a 'store' format
    As dates in SQLite are text and the date must be in a certain
    format in order to perform SQLite functions on them
    */
    public static final String VIEW_DATE_FORMAT = "dd/MM/yyyy";
    public static final String ENTRY_DATE_FORMAT = "yyyy-MM-dd";
    private final Locale locale = Locale.getDefault();

    //DB
    ExerciseManager exMngr;
    FormulaManager formulaMngr;

    //Facilitate inter-fragment communication via the activity
    ExerciseComm excomm;
    Integer id;
    boolean updating;

    //UI Items
    EditText weightEntry;
    Spinner repsEntry, exercisePicker;
    Button calcBtn, clearBtn, logBtn, changeBtn, delBtn;
    TextView ormText, ormRes;
    TextView dateText, dateEntry;
    TextView exerciseText;

    //Stores values
    int enteredReps;
    float enteredWeight;

    ArrayList<View> clearableItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_calc, container, false);

        //TODO: Check state after rotation

        //Open DB tables
        exMngr = (ExerciseManager) new ExerciseManager(getContext()).openWriteable();
        formulaMngr = new FormulaManager(getContext());

        //Get various widgets
        weightEntry = (EditText) v.findViewById(R.id.weightEntry);
        repsEntry = (Spinner) v.findViewById(R.id.repsEntry);
        calcBtn = (Button) v.findViewById(R.id.calcButton);
        clearBtn = (Button) v.findViewById(R.id.clearButton);
        ormText = (TextView) v.findViewById(R.id.ormText);
        ormRes = (TextView) v.findViewById(R.id.ormResult);
        exerciseText = (TextView) v.findViewById(R.id.exerciseText);
        exercisePicker = (Spinner) v.findViewById(R.id.exercisePicker);
        dateText = (TextView) v.findViewById(R.id.dateText);
        dateEntry = (TextView) v.findViewById(R.id.dateEntry);
        logBtn = (Button) v.findViewById(R.id.logButton);
        changeBtn = (Button) v.findViewById(R.id.chngeDate);
        delBtn = (Button) v.findViewById(R.id.delBtn);

        //Set onClicklisteners
        calcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calc1RM();
            }
        });
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearInputs();
            }
        });
        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        logBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(updating){
                    //Show confirmation dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Are you sure you wish to change this record?");

                    builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logExercise();
                            Toast.makeText(getContext(), "Record updated", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", null);

                    builder.show();
                }else{
                    logExercise();
                }
            }
        });

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you wish to delete this record?");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete();
                        Toast.makeText(getContext(), "Record deleted", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Cancel", null);

                builder.show();
            }
        });

        //Store an array of items to disappear or appear when we click calc or clear
        clearableItems = new ArrayList<>();
        clearableItems.add(ormText);
        clearableItems.add(ormRes);
        clearableItems.add(exerciseText);
        clearableItems.add(exercisePicker);
        clearableItems.add(dateText);
        clearableItems.add(dateEntry);
        clearableItems.add(logBtn);
        clearableItems.add(changeBtn);

        //Generate array to store rep entries
        Integer[] oneToTen = new Integer[10];
        for(int i = 0; i < 10; i++){
            oneToTen[i] = i + 1;
        }

        //Populate spinners
        //REFERENCE: Lecture Notes: 16 Spinner demo for self study topic on User Input
        ArrayAdapter<Integer> repAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, oneToTen);
        repAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repsEntry.setAdapter(repAdapter);

        //Populate from a values file
        ArrayAdapter<CharSequence> exAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.exercises, R.layout.spinner_item
        );
        exAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exercisePicker.setAdapter(exAdapter);
        //END REFERENCE


        //If there is an id present in the calling activity, it means we're updating an entry
        Activity activity = getActivity();
        if(activity instanceof ExerciseComm){
            excomm = (ExerciseComm) getActivity();
            id = excomm.getExId();
            if(id != null){
                excomm.setExId(null);
                updating = true;
                populate(id);
            }
        }else{
            updating = false;
        }

        return v;
    }

    //Calculates a one rep max
    public void calc1RM(){
        this.hideKeyboard();

        enteredReps = (int) repsEntry.getSelectedItem();
        String weightTxt = weightEntry.getText().toString();

        //Do nothing if the user has not entered a number
        if(weightTxt.isEmpty()){
            return;
        }else{
            enteredWeight = Float.parseFloat(weightTxt);
        }

        //Calc 1RM & Round down to nearest 2.5
        float orm = this.calc1RM(enteredWeight, enteredReps);
        orm = orm - (float)(orm % 2.5);

        //Set res text
        ormRes.setText(String.format(locale, "%.1f", orm));

        //Display today's date.
        //REFERENCE: https://www.mkyong.com/java/java-date-and-calendar-examples/
        Calendar c = Calendar.getInstance();
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(VIEW_DATE_FORMAT, locale);
        dateEntry.setText(sdf.format(d));
        //END REFERENCE

        //Show results
        showInputs();
    }

    //Display items in the clearAbleItems array
    public void showInputs(){
        for(View item : clearableItems){
            item.setVisibility(View.VISIBLE);
        }
    }

    //Hide items in the clearAbleItems array
    public void clearInputs(){
        this.hideKeyboard();

        for(View item : clearableItems){
            item.setVisibility(View.INVISIBLE);
        }

        //Clear Inputs
        weightEntry.setText("");
    }

    //Hides the keyboard
    public void hideKeyboard(){
        //REFERENCE: http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
        View v = getActivity().getCurrentFocus();
        if(v != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        //END REFERENCE
    }

    //Display the date picker fragment
    public void showDatePicker(){
        //REFERENCE: https://developer.android.com/guide/topics/ui/controls/pickers.html#ShowingTheDatePicker
        DialogFragment dateFrag = new DatePickerFragment();
        dateFrag.show(getActivity().getFragmentManager(), "datePicker");
        //END REFERENCE
    }

    //Add / Update an entry
    public void logExercise(){
        //Get values from GUI
        String exercise = (String) exercisePicker.getSelectedItem();
        float orm = Float.parseFloat(ormRes.getText().toString());
        String date = dateEntry.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat(VIEW_DATE_FORMAT, locale);

        //We don't pull these from the GUI because if we did they could be changed after the user
        //clicks calculate
        float weight = enteredWeight;
        int reps = enteredReps;

        //Convert from the view date format to the store date format
        try {
            Date d = sdf.parse(date);
            sdf.applyPattern(ENTRY_DATE_FORMAT);
            date = sdf.format(d);
        }catch(ParseException e){
            Log.e(MainActivity.LOG_TAG, e.getMessage());
        }

        //Run different methods in the DB, depending on whether we are updating or creating
        if(updating) {
            exMngr.updateExercise(id, date, exercise, weight, reps, orm);
            //Go back to previous fragment
            getFragmentManager().popBackStack();
        }else{
            exMngr.logExercise(date, exercise, weight, reps, orm);
        }

        //Display some feedback to the user so they know their entry has been logged
        Toast.makeText(getActivity(), "Exercise Logged", Toast.LENGTH_SHORT).show();
        this.clearInputs();
    }

    //Calc 1RM - Formula
    public float calc1RM(float weight, int reps){
        formulaMngr = (FormulaManager) formulaMngr.openReadable();
        float avg = formulaMngr.getAvg(reps);
        formulaMngr.close();
        return (weight / avg);
    }

    //Delete an entry from the DB
    public void delete(){
        exMngr.deleteExercise(id);
        getFragmentManager().popBackStack();
    }

    //Pre-populate the screen with a db entry
    public void populate(int id){
        Cursor ex = exMngr.getExercise(id);
        ex.moveToFirst();

        int weightCol = ex.getColumnIndex(ExerciseManager.KEY_WEIGHT);
        int repsCol = ex.getColumnIndex(ExerciseManager.KEY_REPS);
        int ormCol = ex.getColumnIndex(ExerciseManager.KEY_ORM);
        int exCol = ex.getColumnIndex(ExerciseManager.KEY_EXERCISE);
        int dateCol = ex.getColumnIndex(ExerciseManager.KEY_DATE);

        enteredWeight = ex.getFloat(weightCol);
        enteredReps = ex.getInt(repsCol) - 1;

        weightEntry.setText(String.format(locale, "%.1f", enteredWeight));
        ormRes.setText(String.format(locale, "%.1f", ex.getFloat(ormCol)));
        repsEntry.setSelection(repsCol);

        switch (ex.getString(exCol)){
            case "Squat":
                exercisePicker.setSelection(0);
                break;
            case "Bench":
                exercisePicker.setSelection(1);
                break;
            case "Deadlift":
                exercisePicker.setSelection(2);
                break;
        }


        String date = ex.getString(dateCol);
        //Convert from the store date format to the view date format
        SimpleDateFormat sdf = new SimpleDateFormat(ENTRY_DATE_FORMAT, locale);
        try {
            Date d = sdf.parse(date);
            sdf.applyPattern(VIEW_DATE_FORMAT);
            date = sdf.format(d);
        }catch(ParseException e){
            Log.e(MainActivity.LOG_TAG, e.getMessage());
        }
        dateEntry.setText(date);

        ex.close();

        //Update UI
        logBtn.setText("UPDATE 1RM");
        delBtn.setVisibility(View.VISIBLE);
        showInputs();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        //Close DB connections
        if(exMngr != null){
            exMngr.close();
        }
        if(formulaMngr != null){
            formulaMngr.close();
        }
    }
}