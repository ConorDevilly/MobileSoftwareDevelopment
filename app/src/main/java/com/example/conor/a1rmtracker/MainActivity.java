package com.example.conor.a1rmtracker;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
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

/*
Reference: Navigation code genearted by Android Studio
*/

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Get various widgets
        weightEntry = (EditText) findViewById(R.id.weightEntry);
        repsEntry = (Spinner) findViewById(R.id.repsEntry);
        ormText = (TextView) findViewById(R.id.ormText);
        ormRes = (TextView) findViewById(R.id.ormResult);
        exerciseText = (TextView) findViewById(R.id.exerciseText);
        exercisePicker = (Spinner) findViewById(R.id.exercisePicker);
        dateText = (TextView) findViewById(R.id.dateText);
        dateEntry = (TextView) findViewById(R.id.dateEntry);
        logBtn = (Button) findViewById(R.id.logButton);
        changeBtn = (Button) findViewById(R.id.chngeDate);

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

        db = new DatabaseHelper(this);


        //Gen array to store rep entries
        Integer[] oneToTen = new Integer[10];
        for(int i = 0; i < 10; i++) oneToTen[i] = i + 1;

        //Ref: Lab4
        ArrayAdapter<Integer> repAdapter = new ArrayAdapter<Integer>(this, R.layout.spinner_item, oneToTen);
        repAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repsEntry.setAdapter(repAdapter);

        //Ref: https://developer.android.com/guide/topics/ui/controls/spinner.html#SelectListener
        ArrayAdapter<CharSequence> exAdapter = ArrayAdapter.createFromResource(
                this, R.array.exercises, R.layout.spinner_item
        );
        exAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exercisePicker.setAdapter(exAdapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        switch (id){
            case R.id.nav_calculator:
                fragment = new CalcFragment();
                break;
            case R.id.nav_history:
                fragment = new HistoryActivity();
                break;
        }

        //TODO: This container shite
        fragmentManager.beginTransaction().replace(R.id.nav_view, fragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        View v = this.getCurrentFocus();
        if(v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    //Display the date picker fragment
    public void showDatePicker(View v){
        DialogFragment dateFrag = new DatePickerFragment();
        dateFrag.show(getFragmentManager(), "datePicker");
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