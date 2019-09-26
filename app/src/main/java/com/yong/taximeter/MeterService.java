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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class MeterService extends Service  implements LocationListener {
    NotificationCompat.Builder notificationBuilder;
    private LocationManager locationManager;
    private Location mLastlocation = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            stopSelf();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0, this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("RunningBackground", "택시미터기 실행중 알림", NotificationManager.IMPORTANCE_MIN);
            channel.setImportance(NotificationManager.IMPORTANCE_MIN);
            channel.setDescription("택시미터기가 백그라운드에서 실행중일 때 표시됩니다.");
            notificationManager.createNotificationChannel(channel);
        }
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "RunningBackground")
                .setSmallIcon(R.drawable.btn_main_taxi)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.btn_main_taxi))
                .setContentTitle("택시미터기")
                .setContentText("택시미터기가 백그라운드에서 동작중입니다.")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MIN)
                .setAutoCancel(false);
        startForeground(1379, notificationBuilder.build());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
        stopForeground(true);
    }

    @Override
    public void onLocationChanged(Location location) {
        double getSpeed = (Double.parseDouble(String.format(Locale.getDefault(), "%.3f", location.getSpeed()))) * 3600 / 1000;

        if(mLastlocation != null) {
            Intent intent = new Intent("CURRENT_SPEED");
            intent.putExtra("curSpeed",getSpeed);
            sendBroadcast(intent);
        }
        mLastlocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

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
