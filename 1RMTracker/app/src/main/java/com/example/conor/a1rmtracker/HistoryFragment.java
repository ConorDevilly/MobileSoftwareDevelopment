package com.example.conor.a1rmtracker;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Displays a list of all entries to the user
 * Created by Conor on 15/11/16.
 */

public class HistoryFragment extends ListFragment {
    ExerciseManager exMngr;
    ExerciseComm excomm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    String[] from = new String[]{
            ExerciseManager.KEY_DATE,
            ExerciseManager.KEY_EXERCISE,
            ExerciseManager.KEY_ORM
    };
    int[] to = new int[]{
            R.id.dateView,
            R.id.exerciseView,
            R.id.ormView
    };
    Cursor res;

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        exMngr = (ExerciseManager) new ExerciseManager(getContext()).openReadable();
        res = exMngr.getAllExerciseLogs();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.exercise_row,
                res, from, to,
                android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        setListAdapter(adapter);
    }

    //Allow for inter-fragment communication via the excomm interface in the Activity
    //REFERENCE: http://stackoverflow.com/questions/38331816/passing-data-between-fragments-in-the-same-activity
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Activity activity = getActivity();
        if(activity instanceof ExerciseComm){
            excomm = (ExerciseComm) getActivity();
        }
    }
    //END REFERENCE

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        //Pass the id to the activity
        res.moveToPosition(position);
        int res_id = res.getInt(0);
        excomm.setExId(res_id);

        //Change screen to a calc fragment
        CalcFragment calcFragment = new CalcFragment();
        this.getFragmentManager().beginTransaction()
                .replace(R.id.container, calcFragment, null)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if(res != null){
            res.close();
        }
        if(exMngr != null){
            exMngr.close();
        }
    }
}
