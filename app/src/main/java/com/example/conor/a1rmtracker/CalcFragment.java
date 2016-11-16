package com.example.conor.a1rmtracker;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Conor on 16/11/16.
 */
public class CalcFragment extends Fragment {
    //DB Table Manager
    DatabaseHelper db;

    //UI Items
    EditText weightEntry;
    Spinner repsEntry;
    TextView ormText;
    TextView ormRes;
    TextView exerciseText;
    Spinner exercisePicker;
    TextView dateText;
    TextView dateEntry;
    Button logBtn;
    Button changeBtn;

    ArrayList<View> clearableItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_calc, container, false);

        //Get various widgets
        weightEntry = (EditText) v.findViewById(R.id.weightEntry);
        repsEntry = (Spinner) v.findViewById(R.id.repsEntry);
        ormText = (TextView) v.findViewById(R.id.ormText);
        ormRes = (TextView) v.findViewById(R.id.ormResult);
        exerciseText = (TextView) v.findViewById(R.id.exerciseText);
        exercisePicker = (Spinner) v.findViewById(R.id.exercisePicker);
        dateText = (TextView) v.findViewById(R.id.dateText);
        dateEntry = (TextView) v.findViewById(R.id.dateEntry);
        logBtn = (Button) v.findViewById(R.id.logButton);
        changeBtn = (Button) v.findViewById(R.id.chngeDate);

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

        //Gen array to store rep entries
        Integer[] oneToTen = new Integer[10];
        for(int i = 0; i < 10; i++) oneToTen[i] = i + 1;

        //Ref: Lab4
        ArrayAdapter<Integer> repAdapter = new ArrayAdapter<Integer>(getActivity(), R.layout.spinner_item, oneToTen);
        repAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repsEntry.setAdapter(repAdapter);

        //Ref: https://developer.android.com/guide/topics/ui/controls/spinner.html#SelectListener
        ArrayAdapter<CharSequence> exAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.exercises, R.layout.spinner_item
        );
        exAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exercisePicker.setAdapter(exAdapter);

        return v;
    }

    public void calc1RM(View v){
        //Close keyboard
        this.hideKeyboard();

        //Parse entries
        float weight = Float.parseFloat(weightEntry.getText().toString());
        int reps = (int) repsEntry.getSelectedItem();

        //Calc 1RM
        float orm = this.calc1RM(weight, reps);

        //Rounding: Round down to nearest 2.5
        orm = orm - (float)(orm % 2.5);

        //Set res text
        ormRes.setText(String.format("%.1f", orm));

        //Display today's date.
        //Ref: https://www.mkyong.com/java/java-date-and-calendar-examples/
        Calendar c = Calendar.getInstance();
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        dateEntry.setText(sdf.format(d));

        //Set widgets visible
        showInputs();
    }

    public void showInputs(){
        for(View item : clearableItems){
            item.setVisibility(View.VISIBLE);
        }
    }

    public void clearInputs(View v){
        //Hide results
        for(View item : clearableItems){
            item.setVisibility(View.INVISIBLE);
        }

        //Clear Inputs
        weightEntry.setText("");
    }

    //Ref: http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    public void hideKeyboard(){
        View v = getActivity().getCurrentFocus();
        if(v != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    //Display the date picker fragment
    public void showDatePicker(View v){
        DialogFragment dateFrag = new DatePickerFragment();
        dateFrag.show(getActivity().getFragmentManager(), "datePicker");
    }

    public void logExercise(View v){
        String date = dateEntry.getText().toString();
        String exercise = (String) exercisePicker.getSelectedItem();
        float weight = Float.parseFloat(weightEntry.getText().toString()); //TODO: Need to save this after its entered
        int reps = (int) repsEntry.getSelectedItem(); //TODO: Same as above

        db.logExercise(date, exercise, weight, reps);
        //TODO: Give message the exercise has been logged
    }

    //Calc 1RM
    public float calc1RM(float weight, int reps){
        return weight / db.getAvg(reps);
    }
}
