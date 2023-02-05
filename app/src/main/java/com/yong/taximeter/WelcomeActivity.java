package com.yong.taximeter;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;
import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {
    int defaultCost = 4800;          // 기본요금
    int runningCost = 100;          // 주행요금
    int timeCost = 100;             // 시간요금 (시속 15km 이하)
    int defaultCostDistance = 1600;  // 기본요금 주행 거리
    int runningCostDistance = 131;  // 주행요금 추가 기준 거리
    int timeCostSecond = 30;       // 시간요금 추가 기준 시간
    int addBoth = 40;                 // 복합할증 비율
    int addNight = 20;                // 심야할증 비율
    int addOutCity = 20;              // 시외할증 비율

    String selectedCity = "";

    LinearLayout btnCostDone;
    LinearLayout btnBackgroundLocationNext;
    LinearLayout btnBackgroundLocationSkip;
    LinearLayout btnLocationNext;
    LinearLayout btnWarningNext;
    LinearLayout costLayout;
    LinearLayout backgroundLocationLayout;
    LinearLayout locationLayout;
    LinearLayout warningLayout;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btnCostDone = findViewById(R.id.btn_welcome_cost_done);
        btnBackgroundLocationNext = findViewById(R.id.btn_welcome_background_location_next);
        btnBackgroundLocationSkip = findViewById(R.id.btn_welcome_background_location_skip);
        btnLocationNext = findViewById(R.id.btn_welcome_location_next);
        btnWarningNext = findViewById(R.id.btn_welcome_warning_next);

        costLayout = findViewById(R.id.layout_welcome_cost);
        backgroundLocationLayout = findViewById(R.id.layout_welcome_background_location);
        locationLayout = findViewById(R.id.layout_welcome_location);
        warningLayout = findViewById(R.id.layout_welcome_warning);

        costLayout.setVisibility(View.INVISIBLE);
        backgroundLocationLayout.setVisibility(View.INVISIBLE);
        warningLayout.setVisibility(View.INVISIBLE);

        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        ed = prefs.edit();

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.btn_welcome_cost_done:
                        Toast.makeText(WelcomeActivity.this, getString(R.string.welcome_toast_done), Toast.LENGTH_SHORT).show();

                        ed.putInt("defaultCost", defaultCost);
                        ed.putInt("defaultCostDistance", defaultCostDistance);
                        ed.putInt("runningCost", runningCost);
                        ed.putInt("runningCostDistance", runningCostDistance);
                        ed.putInt("timeCost", timeCost);
                        ed.putInt("timeCostSecond", timeCostSecond);
                        ed.putInt("addBoth", addBoth);
                        ed.putInt("addNight", addNight);
                        ed.putInt("addOutCity", addOutCity);
                        ed.putBoolean("isFirst", false);
                        ed.apply();

                        finish();
                        break;
                    case R.id.btn_welcome_background_location_next:
                        if(TedPermission.isGranted(WelcomeActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                            costLayout.setVisibility(View.INVISIBLE);
                            backgroundLocationLayout.setVisibility(View.INVISIBLE);
                            locationLayout.setVisibility(View.INVISIBLE);
                            warningLayout.setVisibility(View.VISIBLE);
                        }else{
                            TedPermission.with(WelcomeActivity.this)
                                .setPermissionListener(new PermissionListener() {
                                    @Override
                                    public void onPermissionGranted() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.welcome_toast_location_granted), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onPermissionDenied(List<String> deniedPermissions) {
                                    }
                                })
                                .setDeniedMessage(getString(R.string.welcome_toast_location_not_granted))
                                .setPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                .check();
                        }

                        ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 100);
                        break;
                    case R.id.btn_welcome_background_location_skip:
                        costLayout.setVisibility(View.INVISIBLE);
                        backgroundLocationLayout.setVisibility(View.INVISIBLE);
                        locationLayout.setVisibility(View.INVISIBLE);
                        warningLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.btn_welcome_location_next:
                        if(TedPermission.isGranted(WelcomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                                TedPermission.isGranted(WelcomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)){
                            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                                costLayout.setVisibility(View.INVISIBLE);
                                backgroundLocationLayout.setVisibility(View.VISIBLE);
                                locationLayout.setVisibility(View.INVISIBLE);
                                warningLayout.setVisibility(View.INVISIBLE);
                            }else{
                                costLayout.setVisibility(View.INVISIBLE);
                                backgroundLocationLayout.setVisibility(View.INVISIBLE);
                                locationLayout.setVisibility(View.INVISIBLE);
                                warningLayout.setVisibility(View.VISIBLE);
                            }
                        }else{
                            TedPermission.with(WelcomeActivity.this)
                                .setPermissionListener(new PermissionListener() {
                                    @Override
                                    public void onPermissionGranted() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.welcome_toast_location_granted), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onPermissionDenied(List<String> deniedPermissions) {
                                    }
                                })
                                .setDeniedMessage(getString(R.string.welcome_toast_location_not_granted))
                                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                                .check();
                        }

                        ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

                        break;
                    case R.id.btn_welcome_warning_next:
                        costLayout.setVisibility(View.VISIBLE);
                        backgroundLocationLayout.setVisibility(View.INVISIBLE);
                        locationLayout.setVisibility(View.INVISIBLE);
                        warningLayout.setVisibility(View.INVISIBLE);

                        AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
                        builder.setMessage(getString(R.string.welcome_dialog_location_select));
                        builder.setPositiveButton(getString(R.string.welcome_dialog_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                        break;
                }
            }
        };

        btnCostDone.setOnClickListener(onClickListener);
        btnBackgroundLocationNext.setOnClickListener(onClickListener);
        btnBackgroundLocationSkip.setOnClickListener(onClickListener);
        btnLocationNext.setOnClickListener(onClickListener);
        btnWarningNext.setOnClickListener(onClickListener);

        final TextView tvCost = findViewById(R.id.tv_welcome_cost);

        final RadioGroup localSelect = findViewById(R.id.rgroup_welcome_local);
        localSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int id) {
                switch(id){
                    case R.id.rbtn_welcome_busan:
                        defaultCost = 3800;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 133;
                        timeCost = 100;
                        timeCostSecond = 34;
                        addBoth = 40;
                        addNight = 20;
                        addOutCity = 30;
                        selectedCity = "BUSAN";
                        break;
                    case R.id.rbtn_welcome_daegu:
                        defaultCost = 4000;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 130;
                        timeCost = 100;
                        timeCostSecond = 31;
                        addBoth = 40;
                        addNight = 20;
                        addOutCity = 20;
                        selectedCity = "DAEGU";
                        break;
                    case R.id.rbtn_welcome_daejeon:
                        defaultCost = 3300;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 133;
                        timeCost = 100;
                        timeCostSecond = 34;
                        addBoth = 40;
                        addNight = 20;
                        addOutCity = 30;
                        selectedCity = "DAEJEON";
                        break;
                    case R.id.rbtn_welcome_etc:
                        AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
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
                        builder.setPositiveButton(getString(R.string.welcome_dialog_ok), new DialogInterface.OnClickListener() {
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
                                    Toast.makeText(WelcomeActivity.this, getString(R.string.welcome_toast_input_wrong), Toast.LENGTH_SHORT).show();
                                }else{
                                    defaultCost = Integer.parseInt(defaultCostInput.getText().toString());
                                    defaultCostDistance = Integer.parseInt(defaultCostDistanceInput.getText().toString());
                                    runningCost = Integer.parseInt(runningCostInput.getText().toString());
                                    runningCostDistance = Integer.parseInt(runningCostDistanceInput.getText().toString());
                                    timeCost = Integer.parseInt(timeCostInput.getText().toString());
                                    timeCostSecond = Integer.parseInt(timeCostSecondInput.getText().toString());
                                    addBoth = Integer.parseInt(bothInput.getText().toString());
                                    addNight = Integer.parseInt(nightInput.getText().toString());
                                    addOutCity = Integer.parseInt(outcityInput.getText().toString());
                                    selectedCity = "ETC";

                                    ed.putString("CURRENT_LOCATION", selectedCity);
                                    tvCost.setText(String.format(Locale.getDefault(), getString(R.string.welcome_tv_fee_info), defaultCost, defaultCostDistance, runningCost, runningCostDistance, timeCost, timeCostSecond, addBoth, addNight, addOutCity));
                                }
                            }
                        });
                        builder.setNegativeButton(getString(R.string.welcome_dialog_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                RadioButton seoulRadio = findViewById(R.id.rbtn_welcome_seoul);
                                seoulRadio.setChecked(true);
                            }
                        });
                        builder.show();
                        break;
                    case R.id.rbtn_welcome_gwangju:
                        defaultCost = 3300;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 134;
                        timeCost = 100;
                        timeCostSecond = 32;
                        addBoth = 40;
                        addNight = 20;
                        addOutCity = 35;
                        selectedCity = "GWANGJU";
                        break;
                    case R.id.rbtn_welcome_incheon:
                        defaultCost = 3800;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 135;
                        timeCost = 100;
                        timeCostSecond = 33;
                        addBoth = 50;
                        addNight = 20;
                        addOutCity = 30;
                        selectedCity = "INCHEON";
                        break;
                    case R.id.rbtn_welcome_kyunggi:
                        defaultCost = 3800;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 132;
                        timeCost = 100;
                        timeCostSecond = 31;
                        addBoth = 40;
                        addNight = 20;
                        addOutCity = 20;
                        selectedCity = "GYEONGGI";
                        break;
                    case R.id.rbtn_welcome_seoul:
                        defaultCost = 4800;
                        defaultCostDistance = 1600;
                        runningCost = 100;
                        runningCostDistance = 131;
                        timeCost = 100;
                        timeCostSecond = 30;
                        addBoth = 40;
                        addNight = 20;
                        addOutCity = 20;
                        selectedCity = "SEOUL";
                        break;
                    case R.id.rbtn_welcome_ulsan:
                        defaultCost = 4000;
                        defaultCostDistance = 2000;
                        runningCost = 100;
                        runningCostDistance = 125;
                        timeCost = 100;
                        timeCostSecond = 30;
                        addBoth = 50;
                        addNight = 20;
                        addOutCity = 30;
                        selectedCity = "ULSAN";
                        break;
                }
                ed.putString("CURRENT_LOCATION", selectedCity);
                tvCost.setText(String.format(Locale.getDefault(), getString(R.string.setting_tv_fee_info), defaultCost, defaultCostDistance, runningCost, runningCostDistance, timeCost, timeCostSecond, addBoth, addNight, addOutCity));
            }
        });

        RadioButton defaultSelect = findViewById(R.id.rbtn_welcome_seoul);
        defaultSelect.setChecked(true);
    }
}
