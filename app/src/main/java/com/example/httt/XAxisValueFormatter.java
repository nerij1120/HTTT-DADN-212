package com.example.httt;

import android.util.Log;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class XAxisValueFormatter extends IndexAxisValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        long data = ((long) value) * 1000;

        Date time = new Date(data);
//        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        //Log.d("date", time.toString());
        return dateFormat.format(time);
    }
}
