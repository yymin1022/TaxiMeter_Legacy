package com.yong.taximeter;

import android.annotation.SuppressLint;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.fsn.cauly.CaulyAdInfo;
import com.fsn.cauly.CaulyAdInfoBuilder;
import com.fsn.cauly.CaulyAdView;
import com.fsn.cauly.CaulyAdViewListener;
import com.fsn.cauly.CaulyInterstitialAd;
import com.fsn.cauly.CaulyInterstitialAdListener;

import java.util.Locale;

public class MeterActivity extends AppCompatActivity implements CaulyAdViewListener, CaulyInterstitialAdListener {
    boolean showInterstitial = false;
    boolean isInterstitialAdLoaded = false;

    int curCost = 0;
    double curDistance = 0.0;
    String curAnim = "HORSE";
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
                    case 3:
                        tvType.setText(getString(R.string.meter_tv_cost_mode_both));
                        break;
                }

                runAnim((long)curSpeed);
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

    CaulyInterstitialAd loadedInterstitialAd;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;

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

        tvDistance.setMaxLines(1);
        tvDistance.setHorizontallyScrolling(true);
        tvDistance.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvDistance.setSelected(true);
        tvInfo.setMaxLines(1);
        tvInfo.setHorizontallyScrolling(true);
        tvInfo.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvInfo.setSelected(true);
        tvSpeed.setMaxLines(1);
        tvSpeed.setHorizontallyScrolling(true);
        tvSpeed.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvSpeed.setSelected(true);
        tvTime.setMaxLines(1);
        tvTime.setHorizontallyScrolling(true);
        tvTime.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvTime.setSelected(true);
        tvType.setMaxLines(1);
        tvType.setHorizontallyScrolling(true);
        tvType.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvType.setSelected(true);

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

        curAnim = prefs.getString("CURRENT_THEME", "HORSE");
        if(curAnim != null && curAnim.equals("HORSE")){
            ivHorse.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_horse_1));
        }else{
            ivHorse.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_circle_1));
        }

        if(!prefs.getBoolean("NeverSeeCaution", false)){
            AlertDialog.Builder cautionDialog = new AlertDialog.Builder(MeterActivity.this);
            cautionDialog.setTitle(getString(R.string.meter_dialog_caution_title));
            cautionDialog.setMessage(getString(R.string.meter_dialog_caution_content));
            cautionDialog.setCancelable(false);
            cautionDialog.setPositiveButton(getString(R.string.meter_dialog_caution_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            cautionDialog.setNeutralButton(getString(R.string.meter_dialog_caution_never), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ed = prefs.edit();
                    ed.putBoolean("NeverSeeCaution", true);
                    ed.apply();
                }
            });
            cautionDialog.create().show();
        }

        if(prefs.getBoolean("isServiceRunning", false)){
            tvInfo.setText(getString(R.string.meter_tv_info_running));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        IntentFilter gpsStatusFilter = new IntentFilter();
        gpsStatusFilter.addAction("GPS_STATUS");

        try{
            registerReceiver(gpsStatusReceiver, gpsStatusFilter);
        }catch(Exception e){
            Log.e("ERROR", e.toString());
        }

        IntentFilter speedFilter = new IntentFilter();
        speedFilter.addAction("CURRENT_SPEED");

        try{
            registerReceiver(speedReceiver, speedFilter);
        }catch(Exception e){
            Log.e("ERROR", e.toString());
        }

        Intent intent = new Intent("requestData");
        sendBroadcast(intent);

        if(!prefs.getBoolean("ad_removed", false)){
            CaulyAdInfo bannerAdInfo = new CaulyAdInfoBuilder(CAULY_KEY).
                    effect("FadeIn").
                    bannerHeight("Fixed_50").
                    enableDefaultBannerAd(true).
                    reloadInterval(20).
                    build();

            CaulyAdView javaAdView = new CaulyAdView(this);
            javaAdView.setAdInfo(bannerAdInfo);
            javaAdView.setAdViewListener(MeterActivity.this);

            RelativeLayout rootView = findViewById(R.id.meter_cauly);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            rootView.addView(javaAdView, params);

            CaulyAdInfo InterstitialAdInfo = new CaulyAdInfoBuilder(CAULY_KEY)
                    .build();

            CaulyInterstitialAd interstitial = new CaulyInterstitialAd();
            interstitial.setAdInfo(InterstitialAdInfo);
            interstitial.setInterstialAdListener(this);
            interstitial.disableBackKey();

            interstitial.requestInterstitialAd(this);

            showInterstitial = true;
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

        if(prefs.getBoolean("isServiceRunning", false)){
            Toast.makeText(getApplicationContext(), getString(R.string.meter_toast_onpause_warning), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void startCount(View v){
        if(!prefs.getBoolean("isServiceRunning", false)){
            tvInfo.setText(getString(R.string.meter_tv_info_running));

            startService(new Intent(this, MeterService.class));
        }
    }

    public void stopCount(View v){
        if(prefs.getBoolean("isServiceRunning", false)){
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
        }else{
            Toast.makeText(getApplicationContext(), getString(R.string.meter_toast_start_first), Toast.LENGTH_SHORT).show();
        }

        if(!prefs.getBoolean("ad_removed", false)) {
            if((int)(Math.random() * 10) > 4 && loadedInterstitialAd != null){
                if(showInterstitial && isInterstitialAdLoaded){
                    loadedInterstitialAd.show();
                }else{
                    loadedInterstitialAd.cancel();
                }
            }
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void runAnim(long speed){
        AnimationDrawable animationDrawable = new AnimationDrawable();

        if(curAnim.equals("HORSE")){
            if(speed > 90){
                for(int i = 0; i < 7; i++){
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_1), 47);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 47);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 48);
                }
            }else if(speed > 60){
                for(int i = 0; i < 5; i++){
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_1), 66);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 67);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 67);
                }
            }else if(speed > 40){
                for(int i = 0; i < 4; i++){
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_1), 83);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 83);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 84);
                }
            }else if(speed > 20){
                for(int i = 0; i < 3; i++){
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_1), 111);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 111);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 111);
                }
            }else if(speed > 0) {
                for (int i = 0; i < 2; i++) {
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_1), 166);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 166);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 167);
                }
            }else{
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 1000);
            }
        }else{
            if(speed > 80){
                for(int i = 0; i < 10; i++){
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_1), 12);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_2), 12);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_3), 12);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_4), 12);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_5), 13);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_6), 13);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_7), 13);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_8), 13);
                }
            }else if(speed > 50){
                for(int i = 0; i < 7; i++){
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_1), 20);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_2), 20);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_3), 21);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_4), 21);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_5), 21);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_6), 21);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_7), 21);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_8), 21);
                }
            }else if(speed > 20){
                for(int i = 0; i < 3; i++){
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_1), 41);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_2), 41);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_3), 41);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_4), 42);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_5), 42);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_6), 42);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_7), 42);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_8), 42);
                }
            }else if(speed > 0){
                for(int i = 0; i < 1; i++){
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_1), 125);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_2), 125);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_3), 125);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_4), 125);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_5), 125);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_6), 125);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_7), 125);
                    animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_8), 125);
                }
            }else{
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_circle_1), 125);
            }
        }

        animationDrawable.start();
        ivHorse.setImageDrawable(animationDrawable);
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
    public void onFailedToReceiveAd(CaulyAdView caulyAdView, int errorCode, String errorMsg) {
        Log.e("CAULY ERROR", errorMsg + " : " + errorCode);
    }

    @Override
    public void onShowLandingScreen(CaulyAdView caulyAdView) {

    }

    @Override
    public void onCloseLandingScreen(CaulyAdView caulyAdView) {

    }

    @Override
    public void onReceiveInterstitialAd(CaulyInterstitialAd ad, boolean isChargeableAd) {
        Log.d("CAULY", "OK");

        isInterstitialAdLoaded = true;
        loadedInterstitialAd = ad;
    }

    @Override
    public void onFailedToReceiveInterstitialAd(CaulyInterstitialAd ad, int errorCode, String errorMsg) {
        Log.e("CAULY ERROR", errorMsg + " : " + errorCode);
    }

    @Override
    public void onClosedInterstitialAd(CaulyInterstitialAd ad) {

    }

    @Override
    public void onLeaveInterstitialAd(CaulyInterstitialAd caulyInterstitialAd) {

    }
}