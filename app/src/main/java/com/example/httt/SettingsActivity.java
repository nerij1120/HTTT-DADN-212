package com.example.httt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private SwitchMaterial switchMaterial;
    private TextView txtTemp1, txtTemp2, txtHumid1, txtHumid2;
    private Button button;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        switchMaterial = findViewById(R.id.switchBtn);
        txtTemp1 = findViewById(R.id.txtTemp1);
        txtTemp2 = findViewById(R.id.txtTemp2);
        txtHumid1 = findViewById(R.id.txtHumid1);
        txtHumid2 = findViewById(R.id.txtHumid2);

        sharedPreferences = getApplicationContext().getSharedPreferences("preferences", MODE_PRIVATE);

        Integer temp1 = sharedPreferences.getInt("temp1", 20);
        Integer temp2 = sharedPreferences.getInt("temp2", 30);
        Integer humid1 = sharedPreferences.getInt("humid1", 20);
        Integer humid2 = sharedPreferences.getInt("humid2", 60);
        boolean water_auto = sharedPreferences.getBoolean("water_auto", true);

        txtTemp1.setText(temp1.toString() + " \u00B0C");
        txtTemp2.setText(temp2.toString() + " \u00B0C");
        txtHumid1.setText(humid1.toString() + "%");
        txtHumid2.setText(humid2.toString() + "%");
        switchMaterial.setChecked(water_auto);

        button = findViewById(R.id.changeSettingsBtn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent9 = new Intent(SettingsActivity.this, ChangeSettingsActivity.class);
                startActivity(intent9);
            }
        });


        Menu menu = bottomNavigationView.getMenu();
        MenuItem item = menu.getItem(3);
        item.setChecked(true);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        Intent intent2 = new Intent(SettingsActivity.this, MainActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.analytics:
                        Intent intent0 = new Intent(SettingsActivity.this, AnalyticsActivity.class);
                        startActivity(intent0);
                        break;
                    case R.id.history:
                        Intent intent1 = new Intent(SettingsActivity.this, HistoryActivity.class);
                        startActivity(intent1);
                        break;
                    case R.id.settings:
                        break;
                }
                return false;
            }
        });

        switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("water_auto", b);
                editor.commit();
                Toast.makeText(SettingsActivity.this, "Watering Automatic set to " + b, Toast.LENGTH_SHORT).show();
            }
        });
    }


}