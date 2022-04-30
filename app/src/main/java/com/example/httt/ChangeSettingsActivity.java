package com.example.httt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangeSettingsActivity extends AppCompatActivity {
    private EditText temp1, temp2, humid1, humid2;
    private Button button;
    private SharedPreferences preferences;

    private Integer temp1Int,temp2Int, humid1Int,humid2Int;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_settings);

        temp1 = findViewById(R.id.txtTemp1);
        temp2 = findViewById(R.id.txtTemp2);
        humid1 = findViewById(R.id.txtHumid1);
        humid2 = findViewById(R.id.txtHumid2);

        button = findViewById(R.id.changeSettingsBtn);

        preferences = getSharedPreferences("preferences", MODE_PRIVATE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                temp1Int = Integer.parseInt(temp1.getText().toString());
                temp2Int = Integer.parseInt(temp2.getText().toString());
                humid1Int = Integer.parseInt(humid1.getText().toString());
                humid2Int = Integer.parseInt(humid2.getText().toString());

                SharedPreferences.Editor editor = preferences.edit();

                editor.putInt("temp1", temp1Int);
                editor.putInt("temp2", temp2Int);
                editor.putInt("humid1", humid1Int);
                editor.putInt("humid2", humid2Int);
                editor.commit();

                Toast.makeText(ChangeSettingsActivity.this, "Apply settings successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ChangeSettingsActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}