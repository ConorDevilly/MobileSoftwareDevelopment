package com.example.conor.a1rmtracker;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Gets highest 1RMs in each exercise & displays them
 * Created by Conor on 17/11/16.
 */
public class PRsFragment extends HistoryFragment{

    String[] from = new String[]{
            DatabaseHelper.KEY_DATE,
            DatabaseHelper.KEY_EXERCISE,
            "MAX(" + DatabaseHelper.KEY_ORM + ")"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_pr, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        db = new DatabaseHelper(getActivity());
        TextView totalText = (TextView) getView().findViewById(R.id.totalView);
        String[] exercises = getActivity().getResources().getStringArray(R.array.exercises);
        res = db.getPRs(exercises);

        //Calculate users's total
        int ormCol = res.getColumnIndex("MAX(" + DatabaseHelper.KEY_ORM + ")");
        float total = 0;
        while(res.moveToNext()){
            total += res.getFloat(ormCol);
        }
        totalText.setText(Float.toString(total));

        //Set adapater for list
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.exercise_row,
                res, from, to,
                android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        setListAdapter(adapter);
    }
}