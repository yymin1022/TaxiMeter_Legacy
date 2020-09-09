package com.yong.taximeter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stopService(new Intent(this, MeterService.class));

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

    public void info(View v){
        startActivity(new Intent(this, InfoActivity.class));
    }

    public void location(View v){
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void theme(View v){
        final String[] themeList = {"Horse", "Circle"};
        final int[] selectedItem = {0};

        AlertDialog.Builder themeDialog = new AlertDialog.Builder(MainActivity.this);
        themeDialog.setTitle("Set Theme");
        themeDialog.setSingleChoiceItems(themeList, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                selectedItem[0] = index;
            }
        });
        themeDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), String.valueOf(selectedItem[0]), Toast.LENGTH_LONG).show();
                dialogInterface.dismiss();
            }
        });
        themeDialog.create().show();
    }
}
