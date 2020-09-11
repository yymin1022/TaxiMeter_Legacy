package com.yong.taximeter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.Locale;
import java.util.Objects;

public class MeterService extends Service  implements LocationListener {
    int defaultCost = 3800;          // 기본요금
    int runningCost = 100;          // 주행요금
    int timeCost = 100;             // 시간요금 (시속 15km 이하)

    int defaultCostDistance = 2000;  // 기본요금 주행 거리
    int runningCostDistance = 132;  // 주행   요금 추가 기준 거리
    int timeCostSecond = 31;       // 시간요금 추가 기준 시간

    double curDistance = 0;
    double curSpeed = 0;
    String curTime = "";

    int costMode = 0; // 0 : 기본요금, 1 : 거리요금, 2 : 시간요금, 3 : 서울시 동시병산
    int currentCost = 0;    // 계산된 최종 요금

    double distanceForAdding = 0;
    double timeForAdding = 0;

    int sumDistance = 0;          // 총 이동거리
    int sumTime = 0;              // 총 이동시간

    int addBoth = 40;
    int addNight = 20;                // 심야할증 비율
    int addOutCity = 20;              // 시외할증 비율

    boolean isNight = false;
    boolean isOutCity = false;
    boolean isSeoul = true;

    SharedPreferences prefs;
    SharedPreferences.Editor ed;
    private LocationManager locationManager;
    private Location mLastlocation = null;

    BroadcastReceiver addCostEnable = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() != null && intent.getAction().equals("ADD_ENABLE")){
                switch(Objects.requireNonNull(intent.getStringExtra("addType"))){
                    case "NIGHT":
                        isNight = intent.getBooleanExtra("enable", false);
                        Log.d("ADDING", "NIGHT status " + isNight);
                        break;
                    case "OUTCITY":
                        isOutCity = intent.getBooleanExtra("enable", false);
                        Log.d("ADDING", "NIGHT status " + isOutCity);
                        break;
                }
            }
        }
    };

    BroadcastReceiver requestData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() != null && intent.getAction().equals("requestData")){
                sendData();
            }
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0, this);

        prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        defaultCost = prefs.getInt("defaultCost", 3800);
        runningCost = prefs.getInt("runningCost", 100);
        timeCost = prefs.getInt("timeCost", 100);

        defaultCostDistance = prefs.getInt("defaultCostDistance", 2000);
        runningCostDistance = prefs.getInt("runningCostDistance", 132);
        timeCostSecond = prefs.getInt("timeCostSecond", 32);

        addBoth = prefs.getInt("addBoth", 40);
        addNight = prefs.getInt("addNight", 20);
        addOutCity = prefs.getInt("addOutCity", 20);

        isSeoul = prefs.getBoolean("isSeoul", true);

        currentCost = defaultCost;

        IntentFilter addEnableFilter = new IntentFilter();
        addEnableFilter.addAction("ADD_ENABLE");
        registerReceiver(addCostEnable, addEnableFilter);

        IntentFilter requestDataFilter = new IntentFilter();
        addEnableFilter.addAction("requestData");
        registerReceiver(requestData, requestDataFilter);

        startForeground(1379, getServiceNotification(getString(R.string.meter_noti_text_default)));

        ed = prefs.edit();
        ed.putBoolean("isServiceRunning", true);
        ed.apply();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1379);

        try{
            locationManager.removeUpdates(this);
        }catch(NullPointerException e){
            Log.e("ERROR", e.toString());
        }
        try{
            unregisterReceiver(addCostEnable);
        }catch(Exception e){
            Log.e("ERROR", e.toString());
        }
        try{
            unregisterReceiver(requestData);
        }catch(Exception e){
            Log.e("ERROR", e.toString());
        }

        ed.putBoolean("isServiceRunning", false);
        ed.apply();
    }

    @Override
    public void onLocationChanged(Location location) {
        double getSpeed = Double.parseDouble(String.format(Locale.getDefault(), "%.1f", location.getSpeed()));  // m/s

        if(mLastlocation != null) {
            carculate(getSpeed);

            curDistance = (double)sumDistance / 1000;
            curSpeed = getSpeed * 3.6;
            curTime = getTimeFormat(sumTime);

            sendData();

            updateServiceNotification(String.format(Locale.getDefault(), getString(R.string.meter_noti_text_running), currentCost, curSpeed, curTime));
        }
        mLastlocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Intent intent = new Intent("GPS_STATUS");
        switch(status){
            case LocationProvider.AVAILABLE:
                intent.putExtra("curStatus", true);
                break;
            case LocationProvider.OUT_OF_SERVICE:
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                intent.putExtra("curStatus",false);
                break;
        }
        sendBroadcast(intent);
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, this);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void sendData(){
        Intent intent = new Intent("CURRENT_SPEED");
        intent.putExtra("curCost", currentCost);
        intent.putExtra("curCostMode", costMode);
        intent.putExtra("curDistance",curDistance);
        intent.putExtra("curSpeed", curSpeed);
        intent.putExtra("curTime", curTime);

        sendBroadcast(intent);
    }

    public void carculate(double curSpeed){
        double deltaDistance;
        int costForAdd = 0;

        deltaDistance = curSpeed;
        sumDistance += deltaDistance;
        sumTime += 1;

        // 이동거리가 기본요금 거리 이상인지 확인
        if(sumDistance > defaultCostDistance){
            // 속도에 따라 거리요금 / 시간요금 선택 적용
            // 서울은 저속 주행시 동시병산
            if(isSeoul){
                // 거리요금
                costMode = 1;
                if(distanceForAdding >= runningCostDistance){
                    costForAdd = runningCost * (int)Math.round(distanceForAdding / runningCostDistance);
                    distanceForAdding = 0;
                }else{
                    distanceForAdding += deltaDistance;
                }
                if(curSpeed < (15.0 / 3.6)){
                    // 시간요금 동시병산
                    costMode = 3;
                    if(timeForAdding >= timeCostSecond){
                        costForAdd += timeCost * (int)Math.round(timeForAdding / timeCostSecond);
                        timeForAdding = 0;
                    }else{
                        timeForAdding += 1;
                    }
                }
            }else{
                if(curSpeed < (15.0 / 3.6)){
                    // 시간요금 동시병산
                    costMode = 2;
                    if(timeForAdding >= timeCostSecond){
                        costForAdd = timeCost * (int)Math.round(timeForAdding / timeCostSecond);
                        timeForAdding = 0;
                    }else{
                        timeForAdding += 1;
                    }
                }else{
                    // 거리요금
                    costMode = 1;
                    if(distanceForAdding >= runningCostDistance){
                        costForAdd = runningCost * (int)Math.round(distanceForAdding / runningCostDistance);
                        distanceForAdding = 0;
                    }else{
                        distanceForAdding += deltaDistance;
                    }
                }
            }

            if(isNight && isOutCity){
                //복합할증
                costForAdd *= (double)(addBoth + 100) / 100;
            }else if(isNight){
                //야간할증
                costForAdd *= (double)(addNight + 100) / 100;
            }else if(isOutCity){
                //시외할증
                costForAdd *= (double)(addOutCity + 100) / 100;
            }

            currentCost += costForAdd;
        }else{
            // 기본요금
            costMode = 0;
        }
    }

    private String getTimeFormat(int timeSecond){
        int hour, minute, second;
        minute = timeSecond / 60;
        hour = minute / 60;
        second = timeSecond % 60;
        minute = minute % 60;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
    }

    private Notification getServiceNotification(String notiText){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("RunningBackground", getString(R.string.meter_noti_channel_name), NotificationManager.IMPORTANCE_MIN);
            channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.meter_noti_channel_description));
            if(notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this,0, new Intent(this, MeterActivity.class),0);

        return new NotificationCompat.Builder(getApplicationContext(), "RunningBackground")
                .setAutoCancel(false)
                .setContentIntent(contentIntent)
                .setContentText(notiText)
                .setContentTitle(getString(R.string.meter_noti_title))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.btn_main_taxi))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.btn_main_taxi)
                .build();
    }

    private void updateServiceNotification(String notiText){
        Notification notification = getServiceNotification(notiText);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1379, notification);
    }
}
