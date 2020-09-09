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

        curLocation = prefs.getString("CURRENT_LOCATION", "SEOUL");
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
        final String[] locationStrList = {getString(R.string.setting_radio_seoul),
                getString(R.string.setting_radio_gyeonggi),
                getString(R.string.setting_radio_busan),
                getString(R.string.setting_radio_daegu),
                getString(R.string.setting_radio_incheon),
                getString(R.string.setting_radio_gwangju),
                getString(R.string.setting_radio_daejeon),
                getString(R.string.setting_radio_ulsan),
                getString(R.string.setting_radio_etc)};
        final String[] locationList = {"SEOUL", "GYEONGGI", "BUSAN", "DAEGU", "INCHEON", "GWANGJU", "DAEJEON", "ULSAN", "ETC"};
        final int[] selectedItem = {0};

        for(int i = 0; i < locationList.length; i++){
            if(locationList[i].equals(curLocation)){
                selectedItem[0] = i;
            }
        }

        AlertDialog.Builder themeDialog = new AlertDialog.Builder(MainActivity.this);
        themeDialog.setTitle("Set Location");
        themeDialog.setSingleChoiceItems(locationStrList, selectedItem[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                selectedItem[0] = index;
            }
        });
        themeDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                curLocation = locationList[selectedItem[0]];

                ed.putString("CURRENT_LOCATION", curLocation);
                ed.apply();

                dialogInterface.dismiss();
            }
        });
        themeDialog.create().show();
    }

    public void theme(View v){
        final String[] themeStrList = {getString(R.string.setting_radio_horse), getString(R.string.setting_radio_circle)};
        final String[] themeList = {"HORSE", "CIRCLE"};
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
