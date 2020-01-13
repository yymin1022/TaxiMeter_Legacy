package com.yong.taximeter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

public class MeterService extends Service  implements LocationListener {
    NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;
    private LocationManager locationManager;
    private Location mLastlocation = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0, this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("RunningBackground", getString(R.string.meter_noti_channel_name), NotificationManager.IMPORTANCE_MIN);
            channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.meter_noti_channel_description));
            if(notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "RunningBackground")
                .setSmallIcon(R.drawable.btn_main_taxi)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.btn_main_taxi))
                .setContentTitle(getString(R.string.meter_noti_title))
                .setContentText(getString(R.string.meter_noti_text))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setAutoCancel(false);
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationBuilder != null && notificationManager != null){
            startForeground(1379, notificationBuilder.build());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        try{
            locationManager.removeUpdates(this);
        }catch(NullPointerException e){
            Log.e("ERROR", e.toString());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double getSpeed = (Double.parseDouble(String.format(Locale.getDefault(), "%.3f", location.getSpeed())));  // m/s
        if(mLastlocation != null) {
            Intent intent = new Intent("CURRENT_SPEED");
            intent.putExtra("curSpeed",getSpeed);
            sendBroadcast(intent);
            Log.d("CURRENT_SPEED", String.valueOf(getSpeed));
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

}
