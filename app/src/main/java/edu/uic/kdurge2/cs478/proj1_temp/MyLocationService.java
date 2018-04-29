package edu.uic.kdurge2.cs478.proj1_temp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;

public class MyLocationService extends Service
{
    private static final String TAG = "LOCSERVICE";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10;
    private static final float LOCATION_DISTANCE = 0;

    private Location lastLatLong = null;
    private Intent broadcastIntent;

    private double prevDist,newDist;




    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        //delete from here
        public double distanceHaversine(double startLat, double startLong,
                                      double endLat, double endLong) {

            int EARTH_RADIUS = 6371;
            double dLat  = Math.toRadians((endLat - startLat));
            double dLong = Math.toRadians((endLong - startLong));

            startLat = Math.toRadians(startLat);
            endLat   = Math.toRadians(endLat);

            double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            double res= EARTH_RADIUS * c;
            BigDecimal big = new BigDecimal(res);
            double roundRes = big.setScale(4,BigDecimal.ROUND_UP).doubleValue();
            return roundRes;
        }

        public double haversin(double val) {
            return Math.pow(Math.sin(val / 2), 2);
        }
        //delte till here

        public double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2)
        {
            final int R = 6371;
            // Radius of the earth in km
            double dLat = deg2rad(lat2 - lat1);
            // deg2rad below
            double dLon = deg2rad(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = R * c;
            // Distance in km
            d = (double)Math.round(d * 10000d) / 10000d;
            return d;
        }
        private double deg2rad(double deg)
        {
            return deg * (Math.PI / 180);
        }

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

            newDist = distanceHaversine(MapsActivity.origin.latitude, MapsActivity.origin.longitude, mLastLocation.getLatitude(), mLastLocation.getLongitude());

            lastLatLong = location;

            broadcastIntent = new Intent("distance");
            broadcastIntent.putExtra("distance",newDist);
            broadcastIntent.putExtra("lastLocation",lastLatLong);
            LocalBroadcastManager.getInstance(MyLocationService.this).sendBroadcast(broadcastIntent);

        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }


}