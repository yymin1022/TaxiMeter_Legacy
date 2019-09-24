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

    SharedPreferences prefs;
    SharedPreferences.Editor ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("설정");
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        
        final TextView tvCost = findViewById(R.id.tv_setting_cost);

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
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                defaultCost = Integer.valueOf(defaultCostInput.getText().toString());
                                defaultCostDistance = Integer.valueOf(defaultCostDistanceInput.getText().toString());
                                runningCost = Integer.valueOf(runningCostInput.getText().toString());
                                runningCostDistance = Integer.valueOf(runningCostDistanceInput.getText().toString());
                                timeCost = Integer.valueOf(timeCostInput.getText().toString());
                                timeCostSecond = Integer.valueOf(timeCostSecondInput.getText().toString());
                                addNight = Integer.valueOf(nightInput.getText().toString());
                                addOutCity = Integer.valueOf(outcityInput.getText().toString());
                                tvCost.setText(String.format(Locale.getDefault(),"기본요금 %d원\n기본요금 주행거리 %dm\n주행요금 %d원\n주행요금 추가기준거리 %dm\n시간요금 %d원\n시간요즘 추가기준시간 %d초\n심야할증 비율 %d%%\n시외할증 비율 %d%%", defaultCost, defaultCostDistance, runningCost, runningCostDistance, timeCost, timeCostSecond, addNight, addOutCity));
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
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                }
                tvCost.setText(String.format(Locale.getDefault(),"기본요금 %d원\n기본요금 주행거리 %dm\n주행요금 %d원\n주행요금 추가기준거리 %dm\n시간요금 %d원\n시간요즘 추가기준시간 %d초\n심야할증 비율 %d%%\n시외할증 비율 %d%%", defaultCost, defaultCostDistance, runningCost, runningCostDistance, timeCost, timeCostSecond, addNight, addOutCity));
            }
        });

        RadioButton defaultSelect = findViewById(R.id.rbtn_setting_seoul);
        defaultSelect.setChecked(true);
    }

    public void saveData(View V){
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
        Toast.makeText(getApplicationContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show();
    }
}
