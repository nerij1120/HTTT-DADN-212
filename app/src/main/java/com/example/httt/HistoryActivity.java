package com.example.httt;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecView;
    private HistoryRecViewAdapter adapter;
    private BottomNavigationView bottomNavigationView;
    private  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DataBaseHelper dataBaseHelper;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        adapter = new HistoryRecViewAdapter(this);
        historyRecView = findViewById(R.id.historyRecView);

        historyRecView.setAdapter(adapter);
        historyRecView.setLayoutManager(new LinearLayoutManager(this));


        dataBaseHelper = new DataBaseHelper(HistoryActivity.this);
        ArrayList<History> everyone;

        everyone = dataBaseHelper.getHistoryLast3days();

//        Toast.makeText(this, everyone.toString(), Toast.LENGTH_SHORT).show();



//        History history;
//        Double temp = -99.0;
//        Integer humid = -1;
//
//        try {
//
//            LocalDateTime localDateTime = LocalDateTime.now();
//
//            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//            DateTimeFormatter dateTimeFormatter1 = DateTimeFormatter.ofPattern("HH:mm:ss");
//
//            //Toast.makeText(this, dateTimeFormatter1.format(localDateTime), Toast.LENGTH_SHORT).show();
//
//            history = new History(-1, dateTimeFormatter1.format(localDateTime), dateTimeFormatter.format(localDateTime), temp, humid);
//            //Toast.makeText(HistoryActivity.this, history.toString(), Toast.LENGTH_SHORT).show();
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//            Toast.makeText(HistoryActivity.this, "Error", Toast.LENGTH_SHORT).show();
//            history = new History(-1, "error", "error", temp, humid);
//
//        }
//
//        boolean success = dataBaseHelper.addOne(history);
//
//        Toast.makeText(this, "Success = " + success, Toast.LENGTH_SHORT).show();

        adapter.setHistory(everyone);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem item = menu.getItem(2);
        item.setChecked(true);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        Intent intent2 = new Intent(HistoryActivity.this, MainActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.analytics:
                        Intent intent0 = new Intent(HistoryActivity.this, AnalyticsActivity.class);
                        startActivity(intent0);
                        break;
                    case R.id.history:
                        break;
                    case R.id.settings:
                        Intent intent1 = new Intent(HistoryActivity.this, SettingsActivity.class);
                        startActivity(intent1);
                        break;
                }
                return false;
            }
        });
    }

}