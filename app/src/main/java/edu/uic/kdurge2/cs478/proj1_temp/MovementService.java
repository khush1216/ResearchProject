package edu.uic.kdurge2.cs478.proj1_temp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.peakChecker.PeakCheckerClass;

import com.meapsoft.FFT;


/**
 * Created by Khushbu on 2/1/2018.
 */

public class MovementService extends Service implements SensorEventListener {


    private SensorManager mSensorManager;

    private PeakCheckerClass peakChecker;
    Thread t;
    //store initial 64 magnitudes
    private ArrayList<Float> feature64List;
    private Sensor mAccelerometer, mGyroscope, mStepDetector;
    //store max magnitude after applying low pass filter

    private long timePeak1, timePeak2;
    private ArrayList<Float> onlyFileredMag;
    private HashMap<Float,Long> peakTimeMap;

    Object maxMagnitude;
    String activity;
    //store output of x,y,z readings after applying lowpassfilter
    protected float[] filterOut;
    private int sampleNumber = 0;
    private long secondUpdate = 0;

    //not used
    Handler mHandler;

    String userName;
    File file, magFile;
    FileOutputStream fos, fosMag;
    String content, magContent;
    //store time in milliseconds about the last update
    private long lastUpdate = 0;

    //low pass constant. more weightage to new values
    static final float ALPHA = 0.25f;

    int no_of_peaks = 0;
    long curTime;
    long averagepeakTimeDiff;
    double[] x;  //real part of FFT input
    double[] im; //imaginary part of FFT input(initially passed as an array of zeroes)

    ArrayList<Double> finalFFTFeature64;


    @Override
    public void onCreate() {
        super.onCreate();
        feature64List = new ArrayList<Float>();
        onlyFileredMag = new ArrayList<Float>();
        peakTimeMap = new HashMap<Float,Long>();

        peakChecker = new PeakCheckerClass();
        content = "";
        magContent = "";

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //checking the sensors
        PackageManager packageManager = getPackageManager();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
        //gyroscope
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this,mGyroscope,SensorManager.SENSOR_DELAY_NORMAL);
        //step - detector
        mStepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mSensorManager.registerListener(this,mStepDetector,SensorManager.SENSOR_DELAY_NORMAL);
        Bundle extras = intent.getExtras();
        activity = extras.getString("class_label");
        userName = extras.getString("user_name");

        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + userName + "_training_dataSet.csv");
        magFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + userName + "_MagnitudeDataForPeaks.csv");
        try{
            fos = new FileOutputStream(file,true);
            //fosMag = new FileOutputStream(magFile,true);
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    public void generate64FeatureList(Float x, Float y, Float z){

        float magnitude = (float)Math.sqrt(x*x + y*y + z*z);
        feature64List.add(magnitude);

        onlyFileredMag.add(magnitude);
        peakTimeMap.put(magnitude,curTime);
    }

    public void getMaxFeature(){
        maxMagnitude = Collections.max(feature64List);
    }

    //array of doubles to string
    private void convertFFTtoString(){

        for(Double individualFeature : finalFFTFeature64){
            content = content + individualFeature.toString() + ",";

        }
    }

    //arraylist of floats to string
    private void convertFeaturesToString(){

        for(Float individualMag : feature64List){
            content = content + individualMag.toString() + ",";

        }
    }

    private void convertPeakToString(ArrayList<Float> peakList){
        for(Float peakMagnitude : peakList){
            magContent = magContent + peakMagnitude.toString() + ",";
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    //convert float to double
    public static double[] convertFloatsToDoubles(float[] input)
    {
        if (input == null)
        {
            return null; // Or throw an exception - your choice
        }
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++)
        {
            output[i] = input[i];
        }
        return output;
    }

    //***********apply a low pass Filter*********************//
    protected float[] lowPass(float[] input, float output[]){

        if(output == null)return input;

        for(int i=0;i<input.length;i++){
            //more weight on output than on input
            output[i] = output[i] + ALPHA *(input[i] - output[i]);
        }

        return output;

    }
    //*********end of low pass filter*****************

    //**************** code for generating FFT features************************//
    public void getFFT_Features(List<Float> top8Peaks){


        FFT fft = new FFT(8);

        x = new double[8];
        im = new double[8];
        float[] re = new float[top8Peaks.size()];
        int i = 0;

        for (Float f : top8Peaks) {
            re[i++] = (f != null ? f : Float.NaN);
        }

        x = convertFloatsToDoubles(re);
        fft.fft(x,im);
        Log.i("","");
    }

    //calculate maginitude from FFT values.
    public void getFinalFFTMagnitude(){

        finalFFTFeature64 = new ArrayList<Double>();

       for(int k=0;k<x.length;k++){
           double mag = Math.sqrt(x[k] * x[k] + im[k] * im[k]);
           finalFFTFeature64.add(mag);

       }
    }
    //**************** above code for generating FFT features************************//




    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor mySensor = event.sensor;

        if(mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            //pass through low pass filter
            filterOut = lowPass(event.values.clone(), filterOut);
            //end of calling low pass filter

            if(feature64List.isEmpty() || feature64List.size() == 256){
                if(feature64List.isEmpty()){
                    feature64List = new ArrayList<Float>();
                    //onlyFileredMag = new ArrayList<Float>();

                }

                else if(feature64List.size() == 256){

                  ArrayList<Float> peaksFFT = new ArrayList<Float>();
//
                    peaksFFT = peakChecker.getPeak(feature64List);


                    if(peaksFFT.size() > 8) {

                        no_of_peaks = peaksFFT.size();
                        averagepeakTimeDiff = peakChecker.getPeakTime(peakTimeMap,peaksFFT);
                        List<Float> top8peaks = new ArrayList<Float>();
                        top8peaks = peaksFFT.subList(0, 8);
                        getMaxFeature();
                        getFFT_Features(top8peaks);
                        getFinalFFTMagnitude();
                        convertFFTtoString();

                        if (isExternalStorageWritable()) {
                            try {
                                content = content + maxMagnitude.toString() + "," + Integer.toString(no_of_peaks) + "," + Long.toString(averagepeakTimeDiff)+ "," + activity + "," + userName + "\n";
                                fos.write(content.getBytes());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
//
                    }

                    feature64List = new ArrayList<Float>();
                    maxMagnitude = 0;
                    content = "";
                    //***********FFT functionality to be confirmed*************//


                }

            }
//******************pass the filtered output to Window buffer of size 64*************//

             curTime = System.currentTimeMillis();

                if((curTime - lastUpdate) >= 10) {


                lastUpdate = curTime;
                float x = filterOut[0];
                float y = filterOut[1];
                float z = filterOut[2];

                MainActivity.x_ui = x;
                MainActivity.y_ui = y;
                MainActivity.z_ui = z;
//***********************need to work on (avoid setting static text views)**********************
                MainActivity.uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.updateUI();

                    }
                });
//*********************need to work on above (avoid setting static text views)*********************


                generate64FeatureList(x, y, z);

            }
        }
    }

    @Override
    public void onDestroy(){
        Log.v("SERVICE","Service killed");

        super.onDestroy();
        mSensorManager.unregisterListener(this);

        try{
            //fosMag.close();
            fos.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
