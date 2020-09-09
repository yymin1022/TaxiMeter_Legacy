package com.yong.taximeter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
        curTheme = prefs.getString("CURRENT_THEME", "HORSE");
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

        AlertDialog.Builder locationDialog = new AlertDialog.Builder(MainActivity.this);
        locationDialog.setTitle(getString(R.string.main_dialog_title_locaion));
        locationDialog.setSingleChoiceItems(locationStrList, selectedItem[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                selectedItem[0] = index;
            }
        });
        locationDialog.setPositiveButton(getString(R.string.main_dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                curLocation = locationList[selectedItem[0]];

                ed.putString("CURRENT_LOCATION", curLocation);
                ed.apply();

                updateCostData(curLocation);

                dialogInterface.dismiss();
            }
        });
        locationDialog.create().show();
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
        themeDialog.setTitle(getString(R.string.main_dialog_title_theme));
        themeDialog.setSingleChoiceItems(themeStrList, selectedItem[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                selectedItem[0] = index;
            }
        });
        themeDialog.setPositiveButton(getString(R.string.main_dialog_ok), new DialogInterface.OnClickListener() {
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

    public void updateCostData(String curLocation){
        boolean isSeoul = false;
        int defaultCost = 3800;          // 기본요금
        int runningCost = 100;          // 주행요금
        int timeCost = 100;             // 시간요금 (시속 15km 이하)
        int defaultCostDistance = 2000;  // 기본요금 주행 거리
        int runningCostDistance = 132;  // 주행요금 추가 기준 거리
        int timeCostSecond = 31;       // 시간요금 추가 기준 시간
        int addBoth = 40;                 // 복합할증 비율
        int addNight = 20;                // 심야할증 비율
        int addOutCity = 20;              // 시외할증 비율

        switch(curLocation){
            case "BUSAN":
                defaultCost = 3300;
                defaultCostDistance = 2000;
                runningCost = 100;
                runningCostDistance = 133;
                timeCost = 100;
                timeCostSecond = 34;
                addBoth = 40;
                addNight = 20;
                addOutCity = 30;
                break;
            case "DAEGU":
                defaultCost = 3300;
                defaultCostDistance = 2000;
                runningCost = 100;
                runningCostDistance = 134;
                timeCost = 100;
                timeCostSecond = 32;
                addBoth = 40;
                addNight = 20;
                addOutCity = 20;
                break;
            case "DAEJEON":
                isSeoul = false;
                defaultCost = 3300;
                defaultCostDistance = 2000;
                runningCost = 100;
                runningCostDistance = 133;
                timeCost = 100;
                timeCostSecond = 34;
                addBoth = 40;
                addNight = 20;
                addOutCity = 30;
                break;
            case "ETC":
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final View view = inflater.inflate(R.layout.dialog_welcome_custom, null);
                final EditText defaultCostInput = view.findViewById(R.id.dialog_input_default);
                final EditText defaultCostDistanceInput = view.findViewById(R.id.dialog_input_default_distance);
                final EditText runningCostInput = view.findViewById(R.id.dialog_input_running);
                final EditText runningCostDistanceInput = view.findViewById(R.id.dialog_input_running_distance);
                final EditText timeCostInput = view.findViewById(R.id.dialog_input_time);
                final EditText timeCostSecondInput = view.findViewById(R.id.dialog_input_time_second);
                final EditText bothInput = view.findViewById(R.id.dialog_input_both);
                final EditText nightInput = view.findViewById(R.id.dialog_input_night);
                final EditText outcityInput = view.findViewById(R.id.dialog_input_outcity);

                builder.setView(view);
                builder.setPositiveButton(getString(R.string.setting_dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(defaultCostInput.getText().toString().equals("") ||
                                defaultCostDistanceInput.getText().toString().equals("") ||
                                runningCostInput.getText().toString().equals("") ||
                                runningCostDistanceInput.getText().toString().equals("") ||
                                timeCostInput.getText().toString().equals("") ||
                                timeCostSecondInput.getText().toString().equals("") ||
                                bothInput.getText().toString().equals("") ||
                                nightInput.getText().toString().equals("") ||
                                outcityInput.getText().toString().equals("")){
                            Toast.makeText(MainActivity.this, getString(R.string.setting_toast_input_wrong), Toast.LENGTH_SHORT).show();
                        }else{
                            updateCostData(Integer.parseInt(defaultCostInput.getText().toString()),
                                    Integer.parseInt(defaultCostDistanceInput.getText().toString()),
                                    Integer.parseInt(runningCostInput.getText().toString()),
                                    Integer.parseInt(runningCostDistanceInput.getText().toString()),
                                    Integer.parseInt(timeCostInput.getText().toString()),
                                    Integer.parseInt(timeCostSecondInput.getText().toString()),
                                    Integer.parseInt(bothInput.getText().toString()),
                                    Integer.parseInt(nightInput.getText().toString()),
                                    Integer.parseInt(outcityInput.getText().toString()),
                                    false);
                         }
                    }
                });
                builder.show();
                break;
            case "GWANGJU":
                defaultCost = 3300;
                defaultCostDistance = 2000;
                runningCost = 100;
                runningCostDistance = 134;
                timeCost = 100;
                timeCostSecond = 32;
                addBoth = 40;
                addNight = 20;
                addOutCity = 35;
                break;
            case "INCHEON":
                defaultCost = 3800;
                defaultCostDistance = 2000;
                runningCost = 100;
                runningCostDistance = 135;
                timeCost = 100;
                timeCostSecond = 33;
                addBoth = 50;
                addNight = 20;
                addOutCity = 30;
                break;
            case "GYEONGGI":
                defaultCost = 3800;
                defaultCostDistance = 2000;
                runningCost = 100;
                runningCostDistance = 132;
                timeCost = 100;
                timeCostSecond = 31;
                addBoth = 40;
                addNight = 20;
                addOutCity = 20;
                break;
            case "SEOUL":
                isSeoul = true;
                defaultCost = 3800;
                defaultCostDistance = 2000;
                runningCost = 100;
                runningCostDistance = 132;
                timeCost = 100;
                timeCostSecond = 31;
                addBoth = 40;
                addNight = 20;
                addOutCity = 20;
                break;
            case "ULSAN":
                defaultCost = 3300;
                defaultCostDistance = 2000;
                runningCost = 100;
                runningCostDistance = 125;
                timeCost = 100;
                timeCostSecond = 30;
                addBoth = 50;
                addNight = 20;
                addOutCity = 30;
                break;
        }

        updateCostData(defaultCost, defaultCostDistance, runningCost, runningCostDistance, timeCost, timeCostSecond, addBoth, addNight, addOutCity, isSeoul);
    }

    public void updateCostData(int defaultCost, int defaultCostDistance, int runningCost, int runningCostDistance, int timeCost, int timeCostSecond, int addBoth, int addNight, int addOutCity, boolean isSeoul){
        ed.putInt("defaultCost", defaultCost);
        ed.putInt("defaultCostDistance", defaultCostDistance);
        ed.putInt("runningCost", runningCost);
        ed.putInt("runningCostDistance", runningCostDistance);
        ed.putInt("timeCost", timeCost);
        ed.putInt("timeCostSecond", timeCostSecond);
        ed.putInt("addBoth", addBoth);
        ed.putInt("addNight", addNight);
        ed.putInt("addOutCity", addOutCity);
        ed.putBoolean("isSeoul", isSeoul);
        ed.apply();
    }
}
