package com.yong.taximeter;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fsn.cauly.CaulyAdInfo;
import com.fsn.cauly.CaulyAdInfoBuilder;
import com.fsn.cauly.CaulyAdView;
import com.fsn.cauly.CaulyAdViewListener;

import java.util.Locale;

public class MeterActivity extends AppCompatActivity implements CaulyAdViewListener {
    int defaultCost = 3800;          // 기본요금
    int runningCost = 100;          // 주행요금
    int timeCost = 100;             // 시간요금 (시속 15km 이하)
    int defaultCostDistance = 2000;  // 기본요금 주행 거리
    int runningCostDistance = 132;  // 주행   요금 추가 기준 거리
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

    String CAULY_KEY = BuildConfig.CAULY_KEY;

    BroadcastReceiver gpsStatusReceiver = null;
    BroadcastReceiver speedReceiver = null;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    SharedPreferences prefs;

    private ImageView ivHorse;
    private TextView tvCost, tvDistance, tvInfo, tvSpeed, tvTime, tvType;

    @Override
    protected void onCreate(Bundle savedInstanceState){
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

        tvCost.setText(String.format(Locale.getDefault(), getString(R.string.meter_tv_current_cost_format), currentCost));
        tvDistance.setText(String.format(Locale.getDefault(), getString(R.string.meter_tv_moving_distance_format), sumDistance));
        tvSpeed.setText(getString(R.string.meter_tv_current_speed));
        tvTime.setText(String.format(Locale.getDefault(), getString(R.string.meter_tv_moving_time_format), sumTime));
        tvType.setText(getString(R.string.meter_tv_cost_mode_default));

        isNightButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked){
                if(isChecked){
                    Toast.makeText(getApplicationContext(), getString(R.string.meter_toast_night_extra_enabled), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.meter_toast_night_extra_disabled), Toast.LENGTH_SHORT).show();
                }
                isNight = isChecked;
            }
        });
        isOutCityButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked){
                if(isChecked){
                    Toast.makeText(getApplicationContext(), getString(R.string.meter_toast_outcity_extra_enabled), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.meter_toast_outcity_extra_disabled), Toast.LENGTH_SHORT).show();
                }
                isOutCity = isChecked;
            }
        });

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            createGpsDisabledAlert();
        }

        // FOR DEBUGGING ONLY : CURRENT TIME TEXT VIEW IS DISABLED FOR RELEASE
        TextView tvTimeTitle = findViewById(R.id.tvTimeTitle);
        tvTime.setVisibility(View.GONE);
        tvTimeTitle.setVisibility(View.GONE);

        if(!prefs.getBoolean("ad_removed", false)){
            Log.d("CAULY", CAULY_KEY);
            CaulyAdInfo adInfo = new CaulyAdInfoBuilder(CAULY_KEY).
                    effect("FadeIn").
                    bannerHeight("Fixed_50").
                    enableDefaultBannerAd(true).
                    reloadInterval(20).
                    build();

            CaulyAdView javaAdView = new CaulyAdView(this);
            javaAdView.setAdInfo(adInfo);
            javaAdView.setAdViewListener(MeterActivity.this);

            RelativeLayout rootView = findViewById(R.id.meter_cauly);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            rootView.addView(javaAdView, params);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        if(powerManager != null){
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "METER:WAKELOCK");
            wakeLock.acquire();
        }
    }

    @Override
    public void onBackPressed(){
        ActivityManager manager = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        boolean isRunning = false;

        if(manager != null){
            for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
                if (MeterService.class.getName().equals(service.service.getClassName())) {
                    isRunning = true;
                }
            }
            if(isRunning){
                Toast.makeText(getApplicationContext(), getString(R.string.meter_toast_stop_first), Toast.LENGTH_SHORT).show();
            }else{
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ActivityManager manager = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if(manager != null){
            for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            {
                if(MeterService.class.getName().equals(service.service.getClassName())){
                    Toast.makeText(getApplicationContext(), getString(R.string.meter_toast_onpause_warning), Toast.LENGTH_SHORT).show();
                }
            }
        }

        wakeLock.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager manager = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if(manager != null){
            for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            {
                if(MeterService.class.getName().equals(service.service.getClassName())){
                    stopService(new Intent(this, MeterService.class));

                    try{
                        unregisterReceiver(gpsStatusReceiver);
                    }catch(Exception e){
                        Log.e("ERROR", e.toString());
                    }
                    try{
                        unregisterReceiver(speedReceiver);
                    }catch(Exception e){
                        Log.e("ERROR", e.toString());
                    }
                }
            }
        }
    }

    public void carculate(double curSpeed){
        double deltaDistance;
        
        deltaDistance = curSpeed;
        sumDistance += deltaDistance;
        sumTime += 1;

        // 이동거리가 기본요금 거리 이상인지 확인
        if(sumDistance > defaultCostDistance){
            // 속도에 따라 거리요금 / 시간요금 선택 적용
            if(curSpeed < (15.0 / 3.6)){
                if(timeForAdding >= timeCostSecond){
                    currentCost += timeCost * Math.round(timeForAdding / timeCostSecond);
                    timeForAdding = 0;
                }else{
                    timeForAdding += 1;
                }
                tvType.setText(getString(R.string.meter_tv_cost_mode_time));
            }else{
                if(distanceForAdding >= runningCostDistance){
                    currentCost += runningCost * Math.round(distanceForAdding / runningCostDistance);
                    distanceForAdding = 0;
                }else{
                    distanceForAdding += deltaDistance;
                }
                tvType.setText(getString(R.string.meter_tv_cost_mode_distance));
            }
        }else{
            tvType.setText(getString(R.string.meter_tv_cost_mode_default));
        }

        currentCost = currentCost / 100 * 100;
        tvCost.setText(String.format(Locale.getDefault(), getString(R.string.meter_tv_current_cost_format), currentCost));
        tvDistance.setText(String.format(Locale.getDefault(), getString(R.string.meter_tv_moving_distance_format), sumDistance / 1000));
        tvSpeed.setText(String.format(Locale.getDefault(), getString(R.string.meter_tv_current_speed_format), curSpeed * 3.6));
        tvTime.setText(String.format(Locale.getDefault(), getString(R.string.meter_tv_moving_time_format), sumTime));

        runHorse(Math.round(curSpeed));
    }

    public void startCount(View v){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        tvInfo.setText(getString(R.string.meter_tv_info_running));

        IntentFilter gpsStatusFiler = new IntentFilter();
        IntentFilter speedFilter = new IntentFilter();
        gpsStatusFiler.addAction("GPS_STATUS");
        speedFilter.addAction("CURRENT_SPEED");
        gpsStatusReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                boolean receivedStatus = intent.getBooleanExtra("curStatus", false);
                if(intent.getAction() != null && intent.getAction().equals("GPS_STATUS")){
                    if(receivedStatus){
                        tvInfo.setText(getString(R.string.meter_tv_info_running));
                    }else{
                        tvInfo.setText(getString(R.string.meter_tv_info_gps_unavailable));
                    }
                }
            }
        };

        speedReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                double receivedSpeed = intent.getDoubleExtra("curSpeed",0.0);
                if(intent.getAction() != null && intent.getAction().equals("CURRENT_SPEED")){
                    carculate(receivedSpeed);
                }
            }
        };

        try{
            registerReceiver(gpsStatusReceiver, gpsStatusFiler);
        }catch(Exception e){
            Log.e("ERROR", e.toString());
        }
        try{
            registerReceiver(speedReceiver, speedFilter);
        }catch(Exception e){
            Log.e("ERROR", e.toString());
        }

        startService(new Intent(this, MeterService.class));
    }

    public void stopCount(View v){
        ActivityManager manager = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        boolean isRunning = false;

        if(manager != null){
            for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            {
                if(MeterService.class.getName().equals(service.service.getClassName())){
                    isRunning = true;
                    stopService(new Intent(this, MeterService.class));

                    try{
                        unregisterReceiver(gpsStatusReceiver);
                    }catch(Exception e){
                        Log.e("ERROR", e.toString());
                    }
                    try{
                        unregisterReceiver(speedReceiver);
                    }catch(Exception e){
                        Log.e("ERROR", e.toString());
                    }

                    if(isOutCity){
                        currentCost = currentCost * (100 + addOutCity) / 100;
                    }
                    if(isNight){
                        currentCost = currentCost * (100 + addNight) / 100;
                    }
                    currentCost = (currentCost + 50) / 100 * 100;

                    AlertDialog.Builder stopDialog = new AlertDialog.Builder(this);
                    stopDialog.setTitle(getString(R.string.meter_dialog_finish_title));
                    stopDialog.setMessage(String.format(Locale.getDefault(), getString(R.string.meter_dialog_finish_message), currentCost, sumDistance));
//                    stopDialog.setMessage(String.format(Locale.getDefault(), getString(R.string.meter_dialog_finish_message_debug), currentCost,  sumTime, sumDistance));
                    stopDialog.setPositiveButton(getString(R.string.meter_dialog_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i){
                            finish();
                        }
                    });
                    stopDialog.show();
                }
            }
            if(!isRunning){
                Toast.makeText(getApplicationContext(), getString(R.string.meter_toast_start_first), Toast.LENGTH_SHORT).show();
            }
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

    private void createGpsDisabledAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.meter_dialog_gps_unavailable))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.meter_dialog_gps_unavailable_open_setting),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id){
                                Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(gpsOptionsIntent);
                            }
                        })
                .setNegativeButton(getString(R.string.meter_dialog_gps_unavailable_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id){
                                finish();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onReceiveAd(CaulyAdView caulyAdView, boolean b) {
        Log.d("CAULY", "OK");
    }

    @Override
    public void onFailedToReceiveAd(CaulyAdView caulyAdView, int i, String s) {
        Log.d("CAULY ERROR", s + " : " + i);
    }

    @Override
    public void onShowLandingScreen(CaulyAdView caulyAdView) {

    }

    @Override
    public void onCloseLandingScreen(CaulyAdView caulyAdView) {

    }
}