package com.example.conor.a1rmtracker;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

/**
 * Fragment used to calculate and store a 1 rep max.
 * Created by Conor on 16/11/16.
 */
public class CalcFragment extends Fragment {
    /*We differentiate between a 'view' format and a 'store' format
    As dates in SQLite as simply text and the date must be in a certain
    format in order to perform SQLite functions on them*/
    public static final String VIEW_DATE_FORMAT = "dd/MM/yyyy";
    public static final String ENTRY_DATE_FORMAT = "yyyy-MM-dd";

    //DB
    DatabaseHelper db;

    //Facilitate inter-fragment communication via the activity
    ExerciseComm excomm;
    Integer id;
    boolean updating;

    //UI Items
    EditText weightEntry;
    Spinner repsEntry;
    Button calcBtn;
    Button clearBtn;
    TextView ormText;
    TextView ormRes;
    TextView exerciseText;
    Spinner exercisePicker;
    TextView dateText;
    TextView dateEntry;
    Button logBtn;
    Button changeBtn;
    Button delBtn;

    ArrayList<View> clearableItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_calc, container, false);

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

        //Set onClicklisteners (Cannot be set in XML as I want it to call a method in a fragment)
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
        logBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logExercise();
            }
        });
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });
        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        //Store an array of items to disappear or appear when we click calc or clear
        clearableItems = new ArrayList<View>();
        clearableItems.add(ormText);
        clearableItems.add(ormRes);
        clearableItems.add(exerciseText);
        clearableItems.add(exercisePicker);
        clearableItems.add(dateText);
        clearableItems.add(dateEntry);
        clearableItems.add(logBtn);
        clearableItems.add(changeBtn);

        db = new DatabaseHelper(getActivity());

        //Generate array to store rep entries
        Integer[] oneToTen = new Integer[10];
        for(int i = 0; i < 10; i++){
            oneToTen[i] = i + 1;
        }

        //Populate spinners
        //REFERENCE: Lecture Notes: 16 Spinner demo for self study topic on User Input
        ArrayAdapter<Integer> repAdapter = new ArrayAdapter<Integer>(getActivity(), R.layout.spinner_item, oneToTen);
        repAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repsEntry.setAdapter(repAdapter);

        //Populate from a values file
        ArrayAdapter<CharSequence> exAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.exercises, R.layout.spinner_item
        );
        exAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exercisePicker.setAdapter(exAdapter);
        //END REFERENCE


        Activity activity = getActivity();

        //If there is an id present in the calling activity, it means we're updating an entry
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
        //Close keyboard
        this.hideKeyboard();

        //Parse entries
        float weight = -1;
        String weightTxt = weightEntry.getText().toString();

        //Do nothing if the user has not entered a number
        if(weightTxt.isEmpty()){
            return;
        }else{
            weight = Float.parseFloat(weightTxt);
        }

        int reps = (int) repsEntry.getSelectedItem();

        //Calc 1RM
        float orm = this.calc1RM(weight, reps);

        //Rounding: Round down to nearest 2.5
        orm = orm - (float)(orm % 2.5);

        //Set res text
        ormRes.setText(String.format("%.1f", orm));

        //Display today's date.
        //REFERENCE: https://www.mkyong.com/java/java-date-and-calendar-examples/
        Calendar c = Calendar.getInstance();
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(VIEW_DATE_FORMAT);
        dateEntry.setText(sdf.format(d));
        //END REFERENCE

        //Set widgets visible
        showInputs();
    }

    //Display items in the clearableItems array
    public void showInputs(){
        for(View item : clearableItems){
            item.setVisibility(View.VISIBLE);
        }
    }

    //Hide items in the clearableItems array
    public void clearInputs(){
        this.hideKeyboard();

        //Hide results
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
        float weight = Float.parseFloat(weightEntry.getText().toString()); //TODO: Need to save this after its entered
        int reps = (int) repsEntry.getSelectedItem(); //TODO: Same as above
        float orm = (float) Float.parseFloat(ormRes.getText().toString());
        String date = dateEntry.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat(VIEW_DATE_FORMAT);

        //Convert from the view date format to the store date format
        try {
            Date d = sdf.parse(date);
            sdf.applyPattern(ENTRY_DATE_FORMAT);
            date = sdf.format(d);
        }catch(ParseException e){
            Log.d("1RM_Tracker", e.getMessage());
        }

        //Run different methods in the DB, depending on whether we are updating or creating
        if(updating) {
            //TODO: If updating, go back to history screen with edited entry highlighted
            db.updateExercise(id, date, exercise, weight, reps, orm);
        }else{
            db.logExercise(date, exercise, weight, reps, orm);
        }

        //Display some feedback to the user so they know their entry has been logged
        Toast.makeText(getActivity(), "Exercise Logged", Toast.LENGTH_SHORT).show();
        this.clearInputs();
    }

    //Calc 1RM - Formula
    public float calc1RM(float weight, int reps){
        return (weight / db.getAvg(reps));
    }

    //Delete an entry from the DB
    public void delete(){
        db.deleteExercise(id);

        //Reset GUI elements
        this.clearInputs();
        delBtn.setVisibility(View.INVISIBLE);
        logBtn.setText("Log 1RM");
        updating = false;
    }

    //Pre-populate the screen with a db entry
    public void populate(int id){
        Cursor ex = db.getExercise(id);
        ex.moveToFirst();

        int weightCol = ex.getColumnIndex(DatabaseHelper.KEY_WEIGHT);
        int repsCol = ex.getColumnIndex(DatabaseHelper.KEY_REPS);
        int ormCol = ex.getColumnIndex(DatabaseHelper.KEY_ORM);
        int exCol = ex.getColumnIndex(DatabaseHelper.KEY_EXERCISE);
        int dateCol = ex.getColumnIndex(DatabaseHelper.KEY_DATE);

        weightEntry.setText(Float.toString(ex.getFloat(weightCol)));
        repsEntry.setSelection(ex.getInt(repsCol) - 1);
        ormRes.setText(Float.toString(ex.getFloat(ormCol)));

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
        dateEntry.setText(ex.getString(dateCol));
        logBtn.setText("UPDATE 1RM");
        showInputs();
        delBtn.setVisibility(View.VISIBLE);
    }
}
