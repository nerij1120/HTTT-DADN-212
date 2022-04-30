package com.example.httt;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;


import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;


import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private static final String USERNAME = "nerij1120";
    private static final String IO_KEY = "aio_SLwZ44d4E4YPfcrVa4lxdiaLVzkR";

    private ExtendedFloatingActionButton floatingActionButton;
    private TextView txtTemp, txtHumidity, txtWater;
    private RelativeLayout tempLayout, humidLayout;
    private ImageView tempImg, humidImg;
    private Integer temp1,temp2,humid1,humid2;
    private boolean coldFlag, hotFlag, dryFlag, wetFlag, pumpFlag, water_auto;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences sharedPreferences;
    private MqttAndroidClient client;
    private DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init Widgets
        dataBaseHelper = new DataBaseHelper(MainActivity.this);

        sharedPreferences = getApplicationContext().getSharedPreferences("preferences", MODE_PRIVATE);

        //Temp and Humidity Threshold
        temp1 = sharedPreferences.getInt("temp1", 20);
        temp2 = sharedPreferences.getInt("temp2", 30);
        humid1 = sharedPreferences.getInt("humid1", 20);
        humid2 = sharedPreferences.getInt("humid2", 60);
        water_auto = sharedPreferences.getBoolean("water_auto", true);

        coldFlag = sharedPreferences.getBoolean("coldFlag", false);
        hotFlag = sharedPreferences.getBoolean("hotFlag", false);
        dryFlag = sharedPreferences.getBoolean("dryFlag", false);
        wetFlag = sharedPreferences.getBoolean("wetFlag", false);
        pumpFlag = sharedPreferences.getBoolean("pumpFlag", false);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        floatingActionButton = findViewById(R.id.waterBtn);

        //Temp and Humidity Text and Last Watering time
        txtTemp = findViewById(R.id.temp);
        txtHumidity = findViewById(R.id.humidity);
        txtWater = findViewById(R.id.water);

        //View & Icon
        tempLayout = findViewById(R.id.tempLayout);
        humidLayout = findViewById(R.id.humidLayout);
        tempImg = findViewById(R.id.tempImg);
        humidImg = findViewById(R.id.humidImg);

        //Create Notification channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "HTTTChannel";
            String description = "Channel for my HTTT app";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("My Notification", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        //MQTT Connect
        mqttConnect();

        // Call API for current temp & humidity & last watering
        apiCall();


        //BOTTOM NAVIGATION
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        break;
                    case R.id.analytics:
                        Intent intent0 = new Intent(MainActivity.this, AnalyticsActivity.class);
                        startActivity(intent0);
                        break;
                    case R.id.history:
                        Intent intent1 = new Intent(MainActivity.this, HistoryActivity.class);
                        startActivity(intent1);
                        break;
                    case R.id.settings:
                        Intent intent2 = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent2);
                        break;
                }
                return false;
            }
        });


        // Update Temp & Humidity & Watering state every 5 Sec
        // & Notify User if Temp & Humidity surpass threshold and when it start automatic pumping
        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                apiCall();
            }
        },0,5000);


    }

    private void mqttConnect() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        options.setUserName(USERNAME);
        options.setPassword(IO_KEY.toCharArray());

        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(getApplicationContext(), "tcp://io.adafruit.com:1883",
                        clientId, Ack.AUTO_ACK);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d("mqtt", "Connection lost");
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("mqtt", topic + " " + message);
                if(topic.equals("nerij1120/feeds/temp/json"))
                {
                    Log.d("mqtt", "Temp" );
                    JSONObject jsonObject = new JSONObject(message.toString());

                    Double temp = jsonObject.getDouble("last_value");

                    txtTemp.setText(temp.toString() + " \u00B0C");

                    Temp tempObj = new Temp(-1, temp);
                    boolean success = dataBaseHelper.addOne(tempObj);
                    Log.d("mqtt", "Success = " + success);

                }
                else if(topic.equals("nerij1120/feeds/humidity/json"))
                {
                    Log.d("mqtt", "Humid" );
                    JSONObject jsonObject = new JSONObject(message.toString());
                    Integer humid = jsonObject.getInt("last_value");
                    txtHumidity.setText(humid.toString() + "%");
//
                    Humid humidObj = new Humid(-1, humid);
                    boolean success = dataBaseHelper.addOne(humidObj);
                    Log.d("mqtt", "Success = " + success);
                }
                else if(topic.equals("nerij1120/feeds/pump/json"))
                {
                    Log.d("mqtt", "Pump" );
                    JSONObject jsonObject = new JSONObject(message.toString());
                    Integer pump = jsonObject.getInt("last_value");

                    if(pump == 1)
                    {
                        addNewHisory();
                    }
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("mqtt", "Delivery Complete");
            }
        });


        IMqttToken token = client.connect(options);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                // We are connected
                Log.d("mqtt", "Connect MQTT on Success");
                subscribeTemp();
                subscribeHumid();
                subscribePump();

//                new Timer().scheduleAtFixedRate(new TimerTask(){
//                    @Override
//                    public void run(){
//                        Random r= new Random();
//                        Double randomValue = 28 + (30-28) * r.nextDouble();
//                        randomValue = Math.round(randomValue * 100.0) / 100.0;
//                        Integer randomInt = r.nextInt(70 - 60) + 60;
//                        try {
//                            publishAPI("nerij1120/feeds/temp/json", randomValue.toString());
//                            publishAPI("nerij1120/feeds/humidity/json", randomInt.toString());
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                },0,5000);
            }
            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                // Something went wrong e.g. connection timeout or firewall problems
                Log.d("mqtt", asyncActionToken.toString());
                Log.d("mqtt", exception.toString());
            }
        });
    }


    private void addNewHisory() {
        String url = "https://io.adafruit.com/api/v2/nerij1120/feeds";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONArray response) {
                        Double temp = -99.0;
                        Integer humidity = -1;
                        try {
                            JSONObject tempInfo = response.getJSONObject(1);
                            temp = tempInfo.getDouble("last_value");

                            JSONObject humidInfo = response.getJSONObject(0);
                            humidity = humidInfo.getInt("last_value");

                            //Toast.makeText(MainActivity.this, temp.toString(), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        LocalDateTime localDateTime = LocalDateTime.now();
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        DateTimeFormatter dateTimeFormatter1 = DateTimeFormatter.ofPattern("HH:mm:ss");

                        History history = new History(-1, dateTimeFormatter1.format(localDateTime), dateTimeFormatter.format(localDateTime), temp, humidity);

                        boolean success = dataBaseHelper.addOne(history);

                        Toast.makeText(MainActivity.this, "Success = " + success, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Something wrong", Toast.LENGTH_SHORT).show();
                    }
                });
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsonArrayRequest);
    }

    private void subscribePump() {
        String topic = "nerij1120/feeds/pump/json";
        int qos = 1;
        IMqttToken subToken = client.subscribe(topic, qos);
        subToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                // The message was published
                Log.d("mqtt", "Subscribe to Pump success");
            }
            @Override
            public void onFailure(IMqttToken asyncActionToken,
                                  Throwable exception) {
                // The subscription could not be performed, maybe the user was not
                // authorized to subscribe on the specified topic e.g. using wildcards
                Log.d("mqtt", "Subscribe to Pump failed");
            }
        });
    }

    private void subscribeHumid() {
        String topic = "nerij1120/feeds/humidity/json";
        int qos = 1;
        IMqttToken subToken = client.subscribe(topic, qos);
        subToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                // The message was published
                Log.d("mqtt", "Subscribe to Humid success");
            }
            @Override
            public void onFailure(IMqttToken asyncActionToken,
                                  Throwable exception) {
                // The subscription could not be performed, maybe the user was not
                // authorized to subscribe on the specified topic e.g. using wildcards
                Log.d("mqtt", "Subscribe to Humid failed");
            }
        });
    }

    private void subscribeTemp() {
        String topic = "nerij1120/feeds/temp/json";
        int qos = 1;
        IMqttToken subToken = client.subscribe(topic, qos);
        subToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                // The message was published
                Log.d("mqtt", "Subscribe to Temp success");
            }
            @Override
            public void onFailure(IMqttToken asyncActionToken,
                                  Throwable exception) {
                // The subscription could not be performed, maybe the user was not
                // authorized to subscribe on the specified topic e.g. using wildcards
                Log.d("mqtt", "Subscribe to Temp failed");
            }
        });
    }

    private void publishAPI(String topic, String payload) throws UnsupportedEncodingException {
        byte[] encodedPayload = new byte[0];
        encodedPayload = payload.getBytes("UTF-8");
        MqttMessage message = new MqttMessage(encodedPayload);
        client.publish(topic, message);
    }


    private void apiCall(){
        String url = "https://io.adafruit.com/api/v2/nerij1120/feeds";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Double temp = -99.0;
                        Integer humidity = -1;
                        int pump = 0;

                        Date date = new Date();
                        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        DateFormat localFormat = new SimpleDateFormat("dd MMMM HH:mm");
                        localFormat.setTimeZone(TimeZone.getDefault());

                        try {
                            JSONObject tempInfo = response.getJSONObject(1);
                            temp = tempInfo.getDouble("last_value");

                            JSONObject humidInfo = response.getJSONObject(0);
                            humidity = humidInfo.getInt("last_value");

                            JSONObject pumpInfo = response.getJSONObject(2);
                            pump = pumpInfo.getInt("last_value");
                            date = utcFormat.parse(pumpInfo.getString("last_value_at"));

                            Date time = new Date();

                            if(time.getTime() - date.getTime() > 3600000)
                            {
                                pumpFlag = false;
                            }

                            //Toast.makeText(MainActivity.this, temp.toString(), Toast.LENGTH_SHORT).show();
                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }

                        if(temp > temp2) {
                            tempLayout.setBackground(getDrawable(R.drawable.gradient_card_bg_hot));
                            tempImg.setImageResource(R.mipmap.sad);
                            if (!hotFlag) {
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "My Notification")
                                        .setSmallIcon(R.drawable.ic_launcher_background)
                                        .setContentTitle("It is too hot!")
                                        .setContentText("The temperature is sky-rocking")
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                NotificationManagerCompat manager = NotificationManagerCompat.from(MainActivity.this);
                                manager.notify(1, builder.build());
                                hotFlag = true;
                            }
                            if (!pumpFlag && water_auto)
                            {
                                pumpWater();
                            }
                        }else if(temp > temp1 && temp <= temp2)
                        {
                            tempLayout.setBackground(getDrawable(R.drawable.gradient_card_bg));
                            tempImg.setImageResource(R.mipmap.img);
                            if(hotFlag)
                            {
                                hotFlag = false;
                            }
                            if(coldFlag)
                            {
                                coldFlag = false;
                            }
                        }else{
                            if(temp != -99.0){
                                tempLayout.setBackground(getDrawable(R.drawable.gradient_card_2_bg));
                                tempImg.setImageResource(R.mipmap.sad);
                                if(!coldFlag)
                                {
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "My Notification")
                                            .setSmallIcon(R.drawable.ic_launcher_background)
                                            .setContentTitle("Its too cold out here!")
                                            .setContentText("Its probably too cold for your plants")
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                    NotificationManagerCompat manager = NotificationManagerCompat.from(MainActivity.this);
                                    manager.notify(2, builder.build());
                                    coldFlag = true;
                                }
                            }
                        }
                        if(humidity > humid2){
                            humidLayout.setBackground(getDrawable(R.drawable.gradient_card_2_bg));
                            humidImg.setImageResource(R.mipmap.sad);
                            if(!wetFlag)
                            {
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "My Notification")
                                        .setSmallIcon(R.drawable.ic_launcher_background)
                                        .setContentTitle("Wet like water!")
                                        .setContentText("Its too wet out there, can do anything about it though!")
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                NotificationManagerCompat manager = NotificationManagerCompat.from(MainActivity.this);
                                manager.notify(4, builder.build());
                                wetFlag = true;
                            }
                        }else if(humidity > humid1 && humidity <= humid2)
                        {
                            humidLayout.setBackground(getDrawable(R.drawable.gradient_card_bg));
                            humidImg.setImageResource(R.mipmap.img);
                            if(dryFlag)
                            {
                                dryFlag = false;
                            }
                            if(wetFlag)
                            {
                                wetFlag = false;
                            }
                        }else{
                            if(humidity != -99.0){
                                humidLayout.setBackground(getDrawable(R.drawable.gradient_card_bg_dry));
                                humidImg.setImageResource(R.mipmap.sad);
                                if(!dryFlag)
                                {
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "My Notification")
                                            .setSmallIcon(R.drawable.ic_launcher_background)
                                            .setContentTitle("Dry like a desert!")
                                            .setContentText("Your plants is definitely getting some water!")
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                    NotificationManagerCompat manager = NotificationManagerCompat.from(MainActivity.this);
                                    manager.notify(3, builder.build());
                                    dryFlag = true;
                                }
                                if(!pumpFlag && water_auto)
                                {
                                    pumpWater();
                                }
                            }
                        }

                        txtWater.setText(localFormat.format(date));
                        txtTemp.setText(temp.toString() + "\u00B0C");
                        txtHumidity.setText(humidity.toString() + "%");

                        SharedPreferences.Editor editor = getSharedPreferences("preferences", MODE_PRIVATE).edit();

                        editor.putBoolean("coldFlag", coldFlag);
                        editor.putBoolean("hotFlag", hotFlag);
                        editor.putBoolean("dryFlag", dryFlag);
                        editor.putBoolean("wetFlag", wetFlag);
                        editor.putBoolean("pumpFlag", pumpFlag);
                        editor.commit();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Something wrong", Toast.LENGTH_SHORT).show();
                    }
                });
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsonArrayRequest);
    }

    private void pumpWater() {
        pumpFlag = true;
        try {
            publishAPI("nerij1120/feeds/pump/json", "1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        floatingActionButton.setText("ON");
        floatingActionButton.setBackgroundColor(getResources().getColor(R.color.on));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "My Notification")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("It is pumping in here!")
                .setContentText("Your plants is getting some water as needed!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat manager = NotificationManagerCompat.from(MainActivity.this);
        manager.notify(30, builder.build());

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        try {
                            publishAPI("nerij1120/feeds/pump/json", "0");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        floatingActionButton.setText("OFF");
                        floatingActionButton.setBackgroundColor(getResources().getColor(R.color.off));
                    }
                }, 20000);
    }
}
