package com.example.conor.a1rmtracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.Locale;


/**
 * Gets highest 1RMs in each exercise & displays them
 * Created by Conor on 17/11/16.
 */
public class PRsFragment extends HistoryFragment{

    String[] from = new String[]{
            ExerciseManager.KEY_DATE,
            ExerciseManager.KEY_EXERCISE,
            "MAX(" + ExerciseManager.KEY_ORM + ")"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_pr, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        exMngr = (ExerciseManager) new ExerciseManager(getContext()).openReadable();
        TextView totalText = (TextView) getView().findViewById(R.id.totalView);
        String[] exercises = getActivity().getResources().getStringArray(R.array.exercises);
        res = exMngr.getPRs(exercises);

        //Calculate users's total
        int ormCol = res.getColumnIndex("MAX(" + ExerciseManager.KEY_ORM + ")");
        float total = 0;
        while(res.moveToNext()){
            total += res.getFloat(ormCol);
        }

        totalText.setText(String.format(Locale.getDefault(), "%.1f", total));

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