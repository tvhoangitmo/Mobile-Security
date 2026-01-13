package com.mobisec.myapplication;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.content.Context;

public class LocationService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            // Thử lấy last known location trước
            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) {
                loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (loc != null) {
                broadcastLocation(loc);
            } else {
                // Nếu chưa có, đăng ký nhận một lần update
                lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        broadcastLocation(location);
                    }
                    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override public void onProviderEnabled(String provider) {}
                    @Override public void onProviderDisabled(String provider) {}
                }, null);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    private void broadcastLocation(Location loc) {
        Intent i = new Intent();
        i.setAction("com.mobisec.intent.action.LOCATION_ANNOUNCEMENT");
        i.putExtra("location", loc);
        sendBroadcast(i);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
