package com.yong.taximeter;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.Locale;

public class MeterService extends Service  implements LocationListener {
    private LocationManager locationManager;
    private Location mLastlocation = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            stopSelf();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0, this);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
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
