package com.yong.taximeter;

import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    int defaultCost = 3800;          // 기본요금
    int runningCost = 100;          // 주행요금
    int timeCost = 100;             // 시간요금 (시속 15km 이하)
    int defaultCostDistance = 2000;  // 기본요금 주행 거리
    int runningCostDistance = 132;  // 주행요금 추가 기준 거리
    int timeCostSecond = 31;       // 시간요금 추가 기준 시간
    int addNight = 20;                // 심야할증 비율
    int addOutCity = 20;              // 시외할증 비율
    String selectedCity = "";

    SharedPreferences prefs;
    SharedPreferences.Editor ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(getString(R.string.setting_title));
        }
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        ed = prefs.edit();
        
        final TextView tvCost = findViewById(R.id.tv_setting_cost);

        RadioButton defaultSelect;
        switch(prefs.getString("CURRENT_LOCATION", "SEOUL")){
            case "BUSAN":
                defaultSelect = findViewById(R.id.rbtn_setting_busan);
                defaultSelect.setChecked(true);
                break;
            case "DAEGU":
                defaultSelect = findViewById(R.id.rbtn_setting_daegu);
                defaultSelect.setChecked(true);
                break;
            case "DAEJEON":
                defaultSelect = findViewById(R.id.rbtn_setting_daejeon);
                defaultSelect.setChecked(true);
                break;
            case "ETC":
                defaultSelect = findViewById(R.id.rbtn_setting_etc);
                defaultSelect.setChecked(true);
                break;
            case "GWANGJU":
                defaultSelect = findViewById(R.id.rbtn_setting_gwangju);
                defaultSelect.setChecked(true);
                break;
            case "INCHEON":
                defaultSelect = findViewById(R.id.rbtn_setting_incheon);
                defaultSelect.setChecked(true);
                break;
            case "GYEONGGI":
                defaultSelect = findViewById(R.id.rbtn_setting_kyunggi);
                defaultSelect.setChecked(true);
                break;
            case "SEOUL":
                defaultSelect = findViewById(R.id.rbtn_setting_seoul);
                defaultSelect.setChecked(true);
                break;
            case "ULSAN":
                defaultSelect = findViewById(R.id.rbtn_setting_ulsan);
                defaultSelect.setChecked(true);
                break;
        }

        RadioGroup localSelect = findViewById(R.id.rgroup_setting_local);
        localSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int id) {
                switch(id){
                    case R.id.rbtn_setting_busan:
                        defaultCost = 3300;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 133;
                        timeCost = 100;
                        timeCostSecond = 34;
                        addNight = 20;
                        addOutCity = 20;
                        selectedCity = "BUSAN";
                        break;
                    case R.id.rbtn_setting_daegu:
                        defaultCost = 3800;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 134;
                        timeCost = 100;
                        timeCostSecond = 32;
                        addNight = 40;
                        addOutCity = 40;
                        selectedCity = "DAEGU";
                        break;
                    case R.id.rbtn_setting_daejeon:
                        defaultCost = 3300;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 133;
                        timeCost = 100;
                        timeCostSecond = 34;
                        addNight = 20;
                        addOutCity = 30;
                        selectedCity = "DAEJEON";
                        break;
                    case R.id.rbtn_setting_etc:
                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        final View view = inflater.inflate(R.layout.dialog_welcome_custom, null);
                        final EditText defaultCostInput = view.findViewById(R.id.dialog_input_default);
                        final EditText defaultCostDistanceInput = view.findViewById(R.id.dialog_input_default_distance);
                        final EditText runningCostInput = view.findViewById(R.id.dialog_input_running);
                        final EditText runningCostDistanceInput = view.findViewById(R.id.dialog_input_running_distance);
                        final EditText timeCostInput = view.findViewById(R.id.dialog_input_time);
                        final EditText timeCostSecondInput = view.findViewById(R.id.dialog_input_time_second);
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
                                        nightInput.getText().toString().equals("") ||
                                        outcityInput.getText().toString().equals("")){
                                    Toast.makeText(SettingsActivity.this, getString(R.string.setting_toast_input_wrong), Toast.LENGTH_SHORT).show();
                                }else{
                                    defaultCost = Integer.valueOf(defaultCostInput.getText().toString());
                                    defaultCostDistance = Integer.valueOf(defaultCostDistanceInput.getText().toString());
                                    runningCost = Integer.valueOf(runningCostInput.getText().toString());
                                    runningCostDistance = Integer.valueOf(runningCostDistanceInput.getText().toString());
                                    timeCost = Integer.valueOf(timeCostInput.getText().toString());
                                    timeCostSecond = Integer.valueOf(timeCostSecondInput.getText().toString());
                                    addNight = Integer.valueOf(nightInput.getText().toString());
                                    addOutCity = Integer.valueOf(outcityInput.getText().toString());
                                    selectedCity = "ETC";

                                    ed.putString("CURRENT_LOCATION", selectedCity);
                                    tvCost.setText(String.format(Locale.getDefault(), getString(R.string.setting_tv_fee_info), defaultCost, defaultCostDistance, runningCost, runningCostDistance, timeCost, timeCostSecond, addNight, addOutCity));
                                }
                            }
                        });
                        builder.setNegativeButton(getString(R.string.setting_dialog_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                RadioButton seoulRadio = findViewById(R.id.rbtn_setting_seoul);
                                seoulRadio.setChecked(true);
                            }
                        });
                        builder.show();
                        break;
                    case R.id.rbtn_setting_gwangju:
                        defaultCost = 3300;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 134;
                        timeCost = 100;
                        timeCostSecond = 32;
                        addNight = 20;
                        addOutCity = 35;
                        selectedCity = "GWANGJU";
                        break;
                    case R.id.rbtn_setting_incheon:
                        defaultCost = 3800;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 135;
                        timeCost = 100;
                        timeCostSecond = 33;
                        addNight = 20;
                        addOutCity = 30;
                        selectedCity = "INCHEON";
                        break;
                    case R.id.rbtn_setting_kyunggi:
                        defaultCost = 3800;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 132;
                        timeCost = 100;
                        timeCostSecond = 31;
                        addNight = 20;
                        addOutCity = 20;
                        selectedCity = "GYEONGGI";
                        break;
                    case R.id.rbtn_setting_seoul:
                        defaultCost = 3800;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 132;
                        timeCost = 100;
                        timeCostSecond = 31;
                        addNight = 20;
                        addOutCity = 20;
                        selectedCity = "SEOUL";
                        break;
                    case R.id.rbtn_setting_ulsan:
                        defaultCost = 3300;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 125;
                        timeCost = 100;
                        timeCostSecond = 30;
                        addNight = 20;
                        addOutCity = 30;
                        selectedCity = "ULSAN";
                        break;
                }
                ed.putString("CURRENT_LOCATION", selectedCity);
                tvCost.setText(String.format(Locale.getDefault(), getString(R.string.setting_tv_fee_info), defaultCost, defaultCostDistance, runningCost, runningCostDistance, timeCost, timeCostSecond, addNight, addOutCity));
            }
        });

        LinearLayout btnSave = findViewById(R.id.btn_setting_done);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ed.putInt("defaultCost", defaultCost);
                ed.putInt("defaultCostDistance", defaultCostDistance);
                ed.putInt("runningCost", runningCost);
                ed.putInt("runningCostDistance", runningCostDistance);
                ed.putInt("timeCost", timeCost);
                ed.putInt("timeCostSecond", timeCostSecond);
                ed.putInt("addNight", addNight);
                ed.putInt("addOutCity", addOutCity);
                ed.putBoolean("isFirst", false);
                ed.apply();
                Toast.makeText(getApplicationContext(), getString(R.string.setting_toast_saved), Toast.LENGTH_SHORT).show();

                finish();
            }
        });
    }
}
