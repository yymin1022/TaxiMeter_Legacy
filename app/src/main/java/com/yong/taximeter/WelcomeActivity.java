package com.yong.taximeter;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity {
    int defaultCost = 3800;          // 기본요금
    int runningCost = 100;          // 주행요금
    int timeCost = 100;             // 시간요금 (시속 15km 이하)
    int defaultCostDistance = 2000;  // 기본요금 주행 거리
    int runningCostDistance = 132;  // 주행요금 추가 기준 거리
    int timeCostSecond = 31;       // 시간요금 추가 기준 시간
    int addNight = 20;                // 심야할증 비율
    int addOutCity = 20;              // 시외할증 비율

    SharedPreferences prefs;
    SharedPreferences.Editor ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        ed = prefs.edit();

        Button locationPermissionButton = findViewById(R.id.btn_welcome_location);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                locationPermissionButton.setEnabled(true);
            } else {
                locationPermissionButton.setEnabled(true);
            }
        }

        RadioGroup localSelect = findViewById(R.id.rgroup_welcome_local);
        localSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int id) {
                switch(id){
                    case R.id.rbtn_welcome_busan:
                        Toast.makeText(getApplicationContext(), "부산", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.rbtn_welcome_daegu:
                        break;
                    case R.id.rbtn_welcome_daejeon:
                        break;
                    case R.id.rbtn_welcome_etc:
                        break;
                    case R.id.rbtn_welcome_gwangju:
                        break;
                    case R.id.rbtn_welcome_incheon:
                        break;
                    case R.id.rbtn_welcome_kyunggi:
                        break;
                    case R.id.rbtn_welcome_seoul:
                        break;
                    case R.id.rbtn_welcome_ulsan:
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getLocationPermission(View V){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
    }

    public void exitWelcome(View V){
        ed.putBoolean("isFirst", false);
        ed.apply();
        finish();
    }
}
