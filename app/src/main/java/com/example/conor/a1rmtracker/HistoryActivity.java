package com.example.conor.a1rmtracker;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by Conor on 15/11/16.
 */

//Ref: www.vogella.com/tutorials/AndroidListView/article.html#listactivity
public class HistoryActivity extends ListFragment {
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        String[] vals = {"Some", "God", "Damn", "Text"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, vals);
        setListAdapter(adapter);
    }
}
