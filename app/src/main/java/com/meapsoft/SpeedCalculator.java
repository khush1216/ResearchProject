package com.meapsoft;

import android.location.Location;
import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Khushbu on 4/11/2018.
 */

public class SpeedCalculator {

    public static double v0,vx,vy,vz;
    public double velocities;

//    public double calculateVelocity(float x, float y, float z, ArrayList<Float> previousValues, long interval, boolean firstPass){
//            double acceleration;
//            if(firstPass){
//                v0 = 0;
//                acceleration = x+y+z;
//            }
//            else {
//                acceleration = x + y + z - previousValues.get(0) - previousValues.get(1) - previousValues.get(2);
//            }
//            double velocity = v0 + (acceleration*(interval/(double)1000));
//            velocities= Math.abs(velocity);
//            v0= velocity;
//            return Math.round(velocities*100.0)/100.0;
//
//    }

    public double calculateVelocity(float x, float y, float z, long interval, boolean firstPass){
        if(firstPass){
            v0 = 0;
            vx = 0;
            vy =0;
            vz =0;
        }

        double x1 = Math.round(x*100.0)/100.0;
        double y1 = Math.round(y*100.0)/100.0;
        double z1 = Math.round(z*100.0)/100.0;

        double vxn = vx + (x1* (interval/(double)1000));
        double vyn = vy + (y1 * (interval/(double)1000));
        double vzn = vz + (z1 * (interval/(double)1000));

        velocities= Math.sqrt(vxn * vxn + vyn * vyn + vzn * vzn);
        vx = vxn;
        vy = vyn;
        vz = vzn;

//        Log.i("INTERVAL!!!", Long.toString(interval));
        return velocities;

    }

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

    public double speedCal(double distance, long startTime, long endtime){

        long timeDiffinSeconds = (endtime - startTime)/1000;

        double speed = Math.round((distance*1000/timeDiffinSeconds)*100.0)/100.0 ;
        return speed;

    }

}
