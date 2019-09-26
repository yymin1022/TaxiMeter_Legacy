package com.yong.taximeter;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.channels.InterruptedByTimeoutException;
import java.util.Locale;

public class MeterActivity extends AppCompatActivity{
    int defaultCost = 3800;          // 기본요금
    int runningCost = 100;          // 주행요금
    int timeCost = 100;             // 시간요금 (시속 15km 이하)
    int defaultCostDistance = 2000;  // 기본요금 주행 거리
    int runningCostDistance = 132;  // 주행요금 추가 기준 거리
    int timeCostSecond = 31;       // 시간요금 추가 기준 시간
    int currentCost = 0;    // 계산된 최종 요금

    double distanceForAdding = 0;
    double timeForAdding = 0;

    double sumDistance = 0;          // 총 이동거리
    int sumTime = 0;              // 총 이동시간

    int addNight = 20;                // 심야할증 비율
    int addOutCity = 20;              // 시외할증 비율

    boolean isNight = false;
    boolean isOutCity = false;

    BroadcastReceiver gpsStatusReceiver = null;
    BroadcastReceiver speedReceiver = null;
    SharedPreferences prefs;

    private ImageView ivHorse;
    private TextView tvCost, tvDistance, tvInfo, tvSpeed, tvTime, tvType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter);

        prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        defaultCost = prefs.getInt("defaultCost", 3800);
        runningCost = prefs.getInt("runningCost", 100);
        timeCost = prefs.getInt("timeCost", 100);
        defaultCostDistance = prefs.getInt("defaultCostDistance", 2000);
        runningCostDistance = prefs.getInt("runningCostDistance", 132);
        timeCostSecond = prefs.getInt("timeCostSecond", 32);
        addNight = prefs.getInt("addNight", 20);
        addOutCity = prefs.getInt("addOutCity", 20);
        currentCost = defaultCost;

        ivHorse = findViewById(R.id.meter_image_horse);
        tvCost = findViewById(R.id.tvCost);
        tvDistance = findViewById(R.id.tvDistance);
        tvInfo = findViewById(R.id.tvInfo);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvTime = findViewById(R.id.tvTime);
        tvType = findViewById(R.id.tvType);
        ToggleButton isNightButton = findViewById(R.id.isNight);
        ToggleButton isOutCityButton = findViewById(R.id.isOuterCity);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/digital_num.ttf");
        tvCost.setTypeface(typeFace);
        tvDistance.setTypeface(typeFace);
        tvSpeed.setTypeface(typeFace);
        tvTime.setTypeface(typeFace);
        tvType.setTypeface(typeFace);

        tvCost.setText(String.format(Locale.getDefault(), "%d원", currentCost));
        tvDistance.setText(String.format(Locale.getDefault(), "%.1fkm", sumDistance));
        tvSpeed.setText("0.0km/s");
        tvTime.setText(String.format(Locale.getDefault(), "%d초", sumTime));
        tvType.setText("기본요금");

        isNightButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    Toast.makeText(getApplicationContext(), "최종 금액에 심야할증이 적용됩니다.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "심야할증 적용이 해제됩니다.", Toast.LENGTH_SHORT).show();
                }
                isNight = isChecked;
            }
        });
        isOutCityButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    Toast.makeText(getApplicationContext(), "최종 금액에 시외할증이 적용됩니다.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "시외할증 적용이 해제됩니다.", Toast.LENGTH_SHORT).show();
                }
                isOutCity = isChecked;
            }
        });

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            createGpsDisabledAlert();
        }

        // FOR DEBUGGING ONLY : CURRENT TIME TEXT VIEW IS DISABLED FOR RELEASE MODE
        TextView tvTimeTitle = findViewById(R.id.tvTimeTitle);
        tvTime.setVisibility(View.GONE);
        tvTimeTitle.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(getApplicationContext(), "운행 종료를 먼저 눌러주세요.", Toast.LENGTH_SHORT).show();
    }

    public void carculate(double curSpeed){
        double deltaDistance;
        
        deltaDistance = curSpeed;
        sumDistance += deltaDistance;
        sumTime += 1;

        // 이동거리가 기본요금 거리 이상인지 확인
        if(sumDistance > defaultCostDistance){
            // 속도에 따라 거리요금 / 시간요금 선택 적용
            if(curSpeed < 15){
                if(timeForAdding >= timeCostSecond){
                    currentCost += timeCost * Math.round(timeForAdding / timeCostSecond);
                    timeForAdding = 0;
                }else{
                    timeForAdding += 1;
                }
                tvType.setText("시간요금");
            }else{
                if(distanceForAdding >= runningCostDistance){
                    currentCost += runningCost * Math.round(distanceForAdding / runningCostDistance);
                    distanceForAdding = 0;
                }else{
                    distanceForAdding += deltaDistance;
                }
                tvType.setText("주행요금");
            }
        }else{
            tvType.setText("기본요금");
        }

        currentCost = currentCost / 100 * 100;
        tvCost.setText(String.format(Locale.getDefault(), "%d원", currentCost));
        tvDistance.setText(String.format(Locale.getDefault(), "%.1fkm", sumDistance/1000));
        tvSpeed.setText(String.format(Locale.getDefault(), "%.1fkm/s", curSpeed));
        tvTime.setText(String.format(Locale.getDefault(), "%d초", sumTime));

        runHorse(Math.round(curSpeed));
    }

    public void startCount(View v){
        tvInfo.setText("운행중입니다.");

        IntentFilter gpsStatusFiler = new IntentFilter();
        IntentFilter speedFilter = new IntentFilter();
        gpsStatusFiler.addAction("GPS_STATUS");
        speedFilter.addAction("CURRENT_SPEED");

        gpsStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean receivedStatus = intent.getBooleanExtra("curStatus", false);
                if(intent.getAction().equals("GPS_STATUS")){
                    if(receivedStatus){
                        tvInfo.setText("운행중입니다.");
                    }else{
                        tvInfo.setText("GPS를 일시적으로 사용할 수 없습니다.");
                    }
                }
            }
        };

        speedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double receivedSpeed = intent.getDoubleExtra("curSpeed",0);
                if(intent.getAction().equals("CURRENT_SPEED")){
                    carculate(receivedSpeed);
                }
            }
        };

        startService(new Intent(this, MeterService.class));
        registerReceiver(gpsStatusReceiver, gpsStatusFiler);
        registerReceiver(speedReceiver, speedFilter);
    }

    public void stopCount(View v) {
        ActivityManager manager = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        boolean isRunning = false;

        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if(MeterService.class.getName().equals(service.service.getClassName())){
                isRunning = true;
                stopService(new Intent(this, MeterService.class));
                if(speedReceiver != null){
                    unregisterReceiver(speedReceiver);
                }

                if (isOutCity) {
                    Log.i("OutCity", "TRUE");
                    currentCost = currentCost * (100 + addOutCity) / 100;
                }
                if (isNight) {
                    Log.i("Night", "TRUE");
                    currentCost = currentCost * (100 + addNight) / 100;
                }
                currentCost = (currentCost + 50) / 100 * 100;

                AlertDialog.Builder stopDialog = new AlertDialog.Builder(this);
                stopDialog.setTitle("운행이 종료되었습니다");
                stopDialog.setMessage("총 요금 : " + currentCost + /* "\n운행 시간 : " + sumTime + */ "\n이동 거리 : " + sumDistance);
                stopDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                stopDialog.show();
            }
        }
        if(!isRunning){
            Toast.makeText(getApplicationContext(), "운행을 아직 시작하지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void runHorse(long speed){
        AnimationDrawable animationDrawable = new AnimationDrawable();

        if(speed > 60){
            for(int i = 0; i < 7; i++){
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_1), 47);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 47);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 48);
                animationDrawable.setOneShot(true);
            }
        }else if(speed > 40){
            for(int i = 0; i < 5; i++){
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_1), 66);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 66);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 68);
                animationDrawable.setOneShot(true);
            }
        }else if(speed > 20){
            for(int i = 0; i < 3; i++){
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_1), 111);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 111);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 111);
                animationDrawable.setOneShot(true);
            }
        }else if(speed > 0){
            for(int i = 0; i < 2; i++){
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_1), 166);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 166);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 168);
                animationDrawable.setOneShot(true);
            }
        }else{
            animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 1000);
            animationDrawable.setOneShot(true);
        }

        ivHorse.setBackground(animationDrawable);
        animationDrawable.start();
    }

    private void createGpsDisabledAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS 기능이 비활성화되었습니다.")
                .setCancelable(false)
                .setPositiveButton("GPS 활성화하기",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(gpsOptionsIntent);
                            }
                        })
                .setNegativeButton("종료",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}