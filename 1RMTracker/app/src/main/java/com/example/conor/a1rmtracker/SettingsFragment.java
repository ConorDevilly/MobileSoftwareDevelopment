package com.example.conor.a1rmtracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Conor on 22/11/16.
 */
public class SettingsFragment extends Fragment {
    DatabaseHelper db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        db = new DatabaseHelper(getActivity());
        Button clearRecords = (Button) v.findViewById(R.id.clearRecords);
        clearRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteAllExercises();
            }
        });

        return v;
    }
}
