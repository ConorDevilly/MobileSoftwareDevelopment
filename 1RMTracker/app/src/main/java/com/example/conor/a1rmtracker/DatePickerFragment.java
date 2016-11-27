package com.example.conor.a1rmtracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Displays a 'Date Picker' dialog
 * REFERENCE: https://developer.android.com/guide/topics/ui/controls/pickers.html
 */
public class DatePickerFragment extends DialogFragment
                                implements DatePickerDialog.OnDateSetListener{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceBundle){
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(this.getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day){
        TextView dateText = (TextView) this.getActivity().findViewById(R.id.dateEntry);
        //TODO: Use resource strings
        dateText.setText(day + "/" + (month + 1) + "/" + year);
    }
}
//END REFERENCE
