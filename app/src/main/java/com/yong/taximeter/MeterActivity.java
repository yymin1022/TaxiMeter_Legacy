package com.yong.taximeter;

import android.annotation.SuppressLint;
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

public class MeterActivity extends AppCompatActivity implements CaulyAdViewListener{
    int curCost = 0;
    double curDistance = 0.0;
    String curTime = "00:00:00";

    String CAULY_KEY = BuildConfig.CAULY_KEY;

    BroadcastReceiver speedReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            if(intent.getAction() != null && intent.getAction().equals("CURRENT_SPEED")){
                int curMode = intent.getIntExtra("curCostMode", 0);
                double curSpeed = intent.getDoubleExtra("curSpeed",0.0);

                curCost = intent.getIntExtra("curCost", 0);
                curDistance = intent.getDoubleExtra("curDistance", 0.0);
                curTime = intent.getStringExtra("curTime");

                Log.d("STATUS", String.format("%d %.1f %.2f %d %s", curCost, curSpeed, curDistance, curMode, curTime));

                tvCost.setText(String.format(Locale.getDefault(), getString(R.string.meter_tv_current_cost_format), curCost));
                tvDistance.setText(String.format(Locale.getDefault(), getString(R.string.meter_tv_moving_distance_format), curDistance));
                tvSpeed.setText(String.format(Locale.getDefault(), getString(R.string.meter_tv_current_speed_format), curSpeed));
                tvTime.setText(curTime);

                switch(curMode){
                    case 0:
                        //기본요금
                        tvType.setText(getString(R.string.meter_tv_cost_mode_default));
                        break;
                    case 1:
                        //거리요금
                        tvType.setText(getString(R.string.meter_tv_cost_mode_distance));
                        break;
                    case 2:
                        //시간요금
                        tvType.setText(getString(R.string.meter_tv_cost_mode_time));
                        break;
                }

                runHorse((long)curSpeed);
            }
        }
    };

    BroadcastReceiver gpsStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() != null && intent.getAction().equals("GPS_STATUS")){
                boolean isGPS = intent.getBooleanExtra("curStatus", true);
                if(isGPS){
                    tvInfo.setText(getString(R.string.meter_tv_info_running));
                }else{
                    tvInfo.setText(getString(R.string.meter_tv_info_gps_unavailable));
                }
            }
        }
    };

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

        isNightButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked){
                if(isChecked){
                    Toast.makeText(getApplicationContext(), getString(R.string.meter_toast_night_extra_enabled), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.meter_toast_night_extra_disabled), Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent("ADD_ENABLE");
                intent.putExtra("addType", "NIGHT");
                intent.putExtra("enable", isChecked);
                sendBroadcast(intent);
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
                Intent intent = new Intent("ADD_ENABLE");
                intent.putExtra("addType", "OUTCITY");
                intent.putExtra("enable", isChecked);
                sendBroadcast(intent);
            }
        });

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            createGpsDisabledAlert();
        }

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

        IntentFilter speedFilter = new IntentFilter();
        speedFilter.addAction("CURRENT_SPEED");

        try{
            registerReceiver(speedReceiver, speedFilter);
        }catch(Exception e){
            Log.e("ERROR", e.toString());
        }

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
    }

    public void startCount(View v){
        tvInfo.setText(getString(R.string.meter_tv_info_running));

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

                    AlertDialog.Builder stopDialog = new AlertDialog.Builder(this);
                    stopDialog.setTitle(getString(R.string.meter_dialog_finish_title));
                    stopDialog.setMessage(String.format(Locale.getDefault(), getString(R.string.meter_dialog_finish_message), curCost, curDistance, curTime));
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

    @SuppressLint("UseCompatLoadingForDrawables")
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
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 67);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 67);
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
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 167);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 167);
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