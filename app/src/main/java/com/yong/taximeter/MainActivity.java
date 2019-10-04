package com.yong.taximeter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if(prefs.getBoolean("isFirst", true)){
            startActivity(new Intent(this, WelcomeActivity.class));
        }
    }


    public void start(View v){
        startActivity(new Intent(this, MeterActivity.class));
    }

    public void donation(View v){
        startActivity(new Intent(this, DonationActivity.class));
    }

    public void setting(View v){
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void info(View v){
        startActivity(new Intent(this, InfoActivity.class));
    }
}
