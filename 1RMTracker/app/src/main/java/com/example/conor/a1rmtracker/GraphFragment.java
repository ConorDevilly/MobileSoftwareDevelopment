package com.example.conor.a1rmtracker;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Displays a graph for a given exercise
 * Created by Conor on 20/11/16.
 */
public class GraphFragment extends Fragment {
    GraphView graph;
    DatabaseHelper db;
    Spinner exercisePicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        //Get various variables
        View v = inflater.inflate(R.layout.fragment_graph, container, false);
        graph = (GraphView) v.findViewById(R.id.graph);
        db = new DatabaseHelper(getActivity());
        exercisePicker = (Spinner) v.findViewById(R.id.exerciseGraphPicker);

        ArrayAdapter<CharSequence> exAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.exercises, R.layout.spinner_item
        );
        exAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exercisePicker.setAdapter(exAdapter);

        //Initalise the graph
        createGraph((String) exAdapter.getItem(0));

        return v;
    }

    /* Setting the spinner listener in the onCreate causes the graph to draw strangely
    To avoid this, we set the listener in the onCreate */
    @Override
    public void onStart(){
        super.onStart();
        exercisePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String exercise = (String) exercisePicker.getItemAtPosition(position);
                createGraph(exercise);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    //Display a graph
    public void createGraph(String exercise){

        //Clear the graph
        if(graph.getSeries() != null){
            graph.removeAllSeries();
        }

        //Get exercise from DB
        Cursor c = db.getAllExercise(exercise);
        int ormCol = c.getColumnIndex(DatabaseHelper.KEY_ORM);
        int dateCol = c.getColumnIndex(DatabaseHelper.KEY_DATE);
        SimpleDateFormat sdf = new SimpleDateFormat(CalcFragment.ENTRY_DATE_FORMAT);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

        //Add each point to the db
        while(c.moveToNext()){
            String date = c.getString(dateCol);
            float orm = c.getFloat(ormCol);

            try {
                Date d = sdf.parse(date);
                sdf.format(d);
                series.appendData(new DataPoint(sdf.parse(date), orm), true, 100);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        sdf.applyPattern("dd/MM");

        //Add points to the graph
        graph.addSeries(series);

        //Set the X axis to be dates
        graph.getGridLabelRenderer().setLabelFormatter(
                new DateAsXAxisLabelFormatter(this.getActivity(), sdf)
        );

        //The automatic bounds were wrong (only showed two points), so we set them ourselves
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(series.getLowestValueX());
        graph.getViewport().setMaxX(series.getHighestValueX());

        //Rounding makes it look prettier
        graph.getGridLabelRenderer().setHumanRounding(false);

        //Allow the user to zoom in on a graph
        graph.getViewport().setScalable(true);
    }
}
