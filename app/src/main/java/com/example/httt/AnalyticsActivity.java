package com.example.httt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AnalyticsActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private LineChart lineChart, lineChart1;
    private DataBaseHelper dataBaseHelper;
    private SQLiteDatabase db;
    private LineDataSet lineDataSet = new LineDataSet(null, null);
    private LineDataSet lineDataSet1 = new LineDataSet(null, null);
    private ArrayList<ILineDataSet> dataSets = new ArrayList<>();
    private ArrayList<ILineDataSet> dataSets1 = new ArrayList<>();
    private LineData lineData, lineData1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        dataBaseHelper = new DataBaseHelper(AnalyticsActivity.this);

        db = dataBaseHelper.getReadableDatabase();

        lineChart = findViewById(R.id.tempChart);

        YAxis rightXAxis = lineChart.getAxisRight();
        rightXAxis.setDrawGridLines(false);
        rightXAxis.setDrawAxisLine(false);
//        rightXAxis.mAxisMinimum = 0f;
//        rightXAxis.mAxisMaximum = 40;

        YAxis leftXAxis = lineChart.getAxisLeft();
        leftXAxis.setDrawGridLines(false);
        leftXAxis.setDrawAxisLine(false);
//        leftXAxis.mAxisMinimum = 0f;
//        leftXAxis.mAxisMaximum = 40;

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new XAxisValueFormatter());
//        xAxis.mAxisMinimum = 0f;
//        xAxis.mAxisMaximum = 24f;
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(4f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDescription(null);
        lineChart.getLegend().setEnabled(false);
        lineChart.setNoDataText("No Data Available Yet");

        lineDataSet.setValues(getTempValues());
        lineDataSet.setColor(ContextCompat.getColor(this, R.color.hot));
        lineDataSet.setValueTextColor(ContextCompat.getColor(this, R.color.black));
        lineDataSet.setDrawValues(false);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setLineWidth(3f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillDrawable(ContextCompat.getDrawable(this, R.drawable.bg_spark_line));

        lineData = new LineData(lineDataSet);

        lineChart.setData(lineData);
        lineChart.invalidate();
        lineChart.notifyDataSetChanged();


        lineChart1 = findViewById(R.id.humidChart);

        YAxis rightXAxis1 = lineChart1.getAxisRight();
        rightXAxis1.setDrawGridLines(false);
        rightXAxis1.setDrawAxisLine(false);
//        rightXAxis1.mAxisMinimum = 0f;
//        rightXAxis1.mAxisMaximum = 80;

        YAxis leftXAxis1 = lineChart1.getAxisLeft();
        leftXAxis1.setDrawGridLines(false);
        leftXAxis1.setDrawAxisLine(false);
//        leftXAxis1.mAxisMinimum = 0f;
//        leftXAxis1.mAxisMaximum = 80;

        XAxis xAxis1 = lineChart1.getXAxis();
        xAxis1.setValueFormatter(new XAxisValueFormatter());
//        xAxis1.mAxisMinimum = 0f;
//        xAxis1.mAxisMaximum = 24f;
        xAxis1.setGranularityEnabled(true);
        xAxis1.setGranularity(4f);
        xAxis1.setDrawGridLines(false);
        xAxis1.setDrawAxisLine(false);
        xAxis1.setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChart1.setTouchEnabled(true);
        lineChart1.setDragEnabled(true);
        lineChart1.setScaleEnabled(false);
        lineChart1.setPinchZoom(false);
        lineChart1.setDescription(null);
        lineChart1.getLegend().setEnabled(false);
        lineChart1.setNoDataText("No Data Available Yet");


        lineDataSet1.setValues(getHumidValues());
        lineDataSet1.setColor(ContextCompat.getColor(this, R.color.wet));
        lineDataSet1.setValueTextColor(ContextCompat.getColor(this, R.color.black));
        lineDataSet1.setDrawValues(false);
        lineDataSet1.setHighlightEnabled(true);
        lineDataSet1.setDrawHighlightIndicators(false);
        lineDataSet1.setDrawCircles(false);
        lineDataSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet1.setLineWidth(3f);
        lineDataSet1.setDrawFilled(true);
        lineDataSet1.setFillDrawable(ContextCompat.getDrawable(this, R.drawable.bg_spark_line_humid));

        lineData1 = new LineData(lineDataSet1);

        lineChart1.setData(lineData1);
        lineChart1.invalidate();
        lineChart1.notifyDataSetChanged();




        bottomNavigationView = findViewById(R.id.bottom_navigation);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem item = menu.getItem(1);
        item.setChecked(true);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        Intent intent2 = new Intent(AnalyticsActivity.this, MainActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.analytics:
                        break;
                    case R.id.history:
                        Intent intent0 = new Intent(AnalyticsActivity.this, HistoryActivity.class);
                        startActivity(intent0);
                        break;
                    case R.id.settings:
                        Intent intent1 = new Intent(AnalyticsActivity.this, SettingsActivity.class);
                        startActivity(intent1);
                        break;
                }
                return false;
            }
        });
    }


    private ArrayList<Entry> getTempValues()
    {
        ArrayList<Entry> entries = new ArrayList<>();
        String[] columns = {"TEMPER", "DATETIME"};

        Cursor cursor = db.query("TEMP_TABLE", columns, "DATETIME BETWEEN datetime('now','localtime','-1 day') AND datetime('now','localtime')", null, null, null, null);

        for(int i =0; i< cursor.getCount(); i++){
            cursor.moveToNext();
            String dateString = cursor.getString(1);
            long timeMilliseconds = new Date().getTime();
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
                timeMilliseconds = date.getTime();
                //Log.d("date", String.valueOf(timeMilliseconds));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            entries.add(new Entry(timeMilliseconds/1000, cursor.getFloat(0)));
//            Log.d("temp", String.valueOf(cursor.getFloat(0)));
        }
        return entries;
    }

    private ArrayList<Entry> getHumidValues()
    {
        ArrayList<Entry> entries = new ArrayList<>();
        String[] columns = {"HUMID", "DATETIME"};

        Cursor cursor = db.query("HUMID_TABLE", columns, " DATETIME BETWEEN datetime('now','localtime','-1 day') AND datetime('now','localtime')", null, null, null, null);

        for(int i =0; i< cursor.getCount(); i++){
            cursor.moveToNext();
            String dateString = cursor.getString(1);
            long timeMilliseconds = new Date().getTime();
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
                timeMilliseconds = date.getTime();
                //Log.d("date", String.valueOf(timeMilliseconds));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            entries.add(new Entry(timeMilliseconds/1000, cursor.getFloat(0)));
//            Log.d("temp", String.valueOf(cursor.getFloat(0)));
        }
        return entries;
    }
}