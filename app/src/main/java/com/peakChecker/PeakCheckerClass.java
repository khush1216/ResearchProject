package com.peakChecker;

import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Khushbu on 4/10/2018.
 */

public class PeakCheckerClass {

    public ArrayList<Float> getPeak(ArrayList<Float> peakList){

        ArrayList<Float> result = new ArrayList<Float>();
        int count = 0;

        for(float x : peakList){
            if(count == 0){
                if(peakList.get(count + 1) < x){
                    result.add(x);
                    count++;
                    continue;
                }
            }

            if(count == peakList.size()-1){
                if(x>peakList.get(count-1)){
                    result.add(x);

                }
                break;
            }
            if(x>peakList.get(count + 1) && x > peakList.get(count - 1)){
                result.add(x);
                count ++;
                continue;
            }

            count++;

        }

        //Log.i("TAG","$$$$$$$$$$$$^^^^^^^^^^^^^^^^^^"+ result.toString());
        return result;
    }

    private Long getDifference(ArrayList<Long> timeList){
        ArrayList<Long> timeDiffList = new ArrayList<>();
        Long returnval = 0L;

        for(int i=0;i<timeList.size()-1;i++){
            int j = i+1;
            if(j != timeDiffList.size()-1) {
                timeDiffList.add(timeList.get(j) - timeList.get(i));
            }
        }
        Long sum = 0L;
        if(!timeDiffList.isEmpty()) {
            for (Long time : timeDiffList) {
                sum += time;
            }
            returnval  = sum.longValue() / timeDiffList.size();
        }
        return returnval;

    }

    public Long getPeakTime(HashMap<Float,Long> peakTimeMap, ArrayList<Float> peakList){

        ArrayList<Float> magnitudeList = new ArrayList<>(peakTimeMap.keySet());

        ArrayList<Long> timeList = new ArrayList<Long>();
        for(Float x : peakList){
            timeList.add(peakTimeMap.get(x));
        }

        Long averageTimeDiff = getDifference(timeList);
        Log.i("DIFFERENCE", averageTimeDiff.toString());

        return averageTimeDiff;
    }

    public Long getPeakTimeConcurrent(Map<Double,Long> peakTimeMap, ArrayList<Double> peakList){

        ArrayList<Double> magnitudeList = new ArrayList<>(peakTimeMap.keySet());

        ArrayList<Long> timeList = new ArrayList<Long>();
        for(double x : peakList){
            timeList.add(peakTimeMap.get(x));
        }

        Long averageTimeDiff = getDifference(timeList);
        Log.i("DIFFERENCE", averageTimeDiff.toString());

        return averageTimeDiff;
    }

}

