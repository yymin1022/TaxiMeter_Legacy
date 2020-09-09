package com.yong.taximeter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    String curLocation = "";
    String curTheme = "";

    SharedPreferences prefs;
    SharedPreferences.Editor ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        ed = prefs.edit();
        ed.apply();

        if(prefs.getBoolean("isFirst", true)){
            startActivity(new Intent(this, WelcomeActivity.class));
        }

        curLocation = prefs.getString("CURRENT_LOCATION", "Seoul");
        curTheme = prefs.getString("CURRENT_THEME", "Horse");
    }


    public void start(View v){
        startActivity(new Intent(this, MeterActivity.class));
    }

    public void donation(View v){
        startActivity(new Intent(this, DonationActivity.class));
    }

    public void info(View v){
        startActivity(new Intent(this, InfoActivity.class));
    }

    public void location(View v){
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void theme(View v){
        final String[] themeStrList = {getString(R.string.setting_radio_horse), getString(R.string.setting_radio_circle)};
        final String[] themeList = {"Horse", "Circle"};
        final int[] selectedItem = {0};

        for(int i = 0; i < themeList.length; i++){
            if(themeList[i].equals(curTheme)){
                selectedItem[0] = i;
            }
        }

        AlertDialog.Builder themeDialog = new AlertDialog.Builder(MainActivity.this);
        themeDialog.setTitle("Set Theme");
        themeDialog.setSingleChoiceItems(themeStrList, selectedItem[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                selectedItem[0] = index;
            }
        });
        themeDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                curTheme = themeList[selectedItem[0]];
                ed.putString("CURRENT_THEME", curTheme);
                ed.apply();

                dialogInterface.dismiss();
            }
        });
        themeDialog.create().show();
    }
}
