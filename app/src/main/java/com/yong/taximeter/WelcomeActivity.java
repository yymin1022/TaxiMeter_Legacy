package com.yong.taximeter;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
    int defaultCost = 3800;          // 기본요금
    int runningCost = 100;          // 주행요금
    int timeCost = 100;             // 시간요금 (시속 15km 이하)
    int defaultCostDistance = 2000;  // 기본요금 주행 거리
    int runningCostDistance = 132;  // 주행요금 추가 기준 거리
    int timeCostSecond = 31;       // 시간요금 추가 기준 시간
    int addNight = 20;                // 심야할증 비율
    int addOutCity = 20;              // 시외할증 비율

    String selectedCity = "";

    LinearLayout btnCostDone;
    LinearLayout btnLocationNext;
    LinearLayout btnWarningNext;
    LinearLayout costLayout;
    LinearLayout locationLayout;
    LinearLayout warningLayout;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btnCostDone = findViewById(R.id.btn_welcome_cost_done);
        btnLocationNext = findViewById(R.id.btn_welcome_location_next);
        btnWarningNext = findViewById(R.id.btn_welcome_warning_next);

        costLayout = findViewById(R.id.layout_welcome_cost);
        locationLayout = findViewById(R.id.layout_welcome_location);
        warningLayout = findViewById(R.id.layout_welcome_warning);

        costLayout.setVisibility(View.INVISIBLE);
        warningLayout.setVisibility(View.INVISIBLE);

        prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.btn_welcome_cost_done:
                        Toast.makeText(WelcomeActivity.this, "기본 설정이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                        ed = prefs.edit();
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

                        finish();
                        break;
                    case R.id.btn_welcome_location_next:
                        if(TedPermission.isGranted(WelcomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)){
                            costLayout.setVisibility(View.INVISIBLE);
                            locationLayout.setVisibility(View.INVISIBLE);
                            warningLayout.setVisibility(View.VISIBLE);
                        }else{
                            TedPermission.with(WelcomeActivity.this)
                                    .setPermissionListener(new PermissionListener() {
                                        @Override
                                        public void onPermissionGranted() {
                                            Toast.makeText(getApplicationContext(), "위치정보 사용 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onPermissionDenied(List<String> deniedPermissions) {
                                        }
                                    })
                                    .setDeniedMessage("위치정보 사용 권한이 허용되지 않았습니다. 애플리케이션 사용 중 예상치 못한 문제가 발생할 수 있으며, [설절] > [앱 및 알림] > [Seoul Healing] > [권한]으로 이동하여 위치정보 사용 권한을 허용해주세요.")
                                    .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                                    .check();
                        }

                        ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                        break;
                    case R.id.btn_welcome_warning_next:
                        costLayout.setVisibility(View.VISIBLE);
                        locationLayout.setVisibility(View.INVISIBLE);
                        warningLayout.setVisibility(View.INVISIBLE);

                        AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
                        builder.setMessage("지역을 선택하거나 요금을 지정해주세요.");
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
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
        btnLocationNext.setOnClickListener(onClickListener);
        btnWarningNext.setOnClickListener(onClickListener);

        final TextView tvCost = findViewById(R.id.tv_welcome_cost);

        RadioGroup localSelect = findViewById(R.id.rgroup_welcome_local);
        localSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int id) {
                switch (id) {
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
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
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
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
                                    Toast.makeText(WelcomeActivity.this, "모든 칸을 입력하지 않았습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
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
                                }
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
                        selectedCity = "KYUNGGI";
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
                    default:
                        Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                }
                ed.putString("CURRENT_LOCATION", selectedCity);
                tvCost.setText(String.format(Locale.getDefault(),"기본요금 %d원\n기본요금 주행거리 %dm\n주행요금 %d원\n주행요금 추가기준거리 %dm\n시간요금 %d원\n시간요즘 추가기준시간 %d초\n심야할증 비율 %d%%\n시외할증 비율 %d%%", defaultCost, defaultCostDistance, runningCost, runningCostDistance, timeCost, timeCostSecond, addNight, addOutCity));
            }
        });
        RadioButton defaultSelect = findViewById(R.id.rbtn_welcome_seoul);
        defaultSelect.setChecked(true);
    }
}
