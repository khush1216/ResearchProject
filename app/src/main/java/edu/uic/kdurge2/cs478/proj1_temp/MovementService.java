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

import com.meapsoft.FFT;


/**
 * Created by Khushbu on 2/1/2018.
 */

public class MovementService extends Service implements SensorEventListener {


    private SensorManager mSensorManager;
    //store initial 64 magnitudes
    private ArrayList<Float> feature64List;
    private Sensor mAccelerometer, mGyroscope, mStepDetector;
    //store max magnitude after applying low pass filter
    Object maxMagnitude;
    String activity;
    //store output of x,y,z readings after applying lowpassfilter
    protected float[] filterOut;
    private int sampleNumber = 0;
    private long secondUpdate = 0;

    //not used
    Handler mHandler;

    String userName;
    File file;
    FileOutputStream fos;
    String content;
    //store time in milliseconds about the last update
    private long lastUpdate = 0;

    //low pass constant. more weightage to new values
    static final float ALPHA = 0.25f;

    double[] x;  //real part of FFT input
    double[] im; //imaginary part of FFT input(initially passed as an array of zeroes)

    ArrayList<Double> finalFFTFeature64;


    @Override
    public void onCreate() {
        super.onCreate();
        feature64List = new ArrayList<Float>();
        content = "";

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //checking the sensors
        PackageManager packageManager = getPackageManager();
        //check if sensors exist
        boolean gyroExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        boolean step_exists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
        //gyroscope
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //step - detector
        mStepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        Bundle extras = intent.getExtras();
        activity = extras.getString("class_label");
        userName = extras.getString("user_name");

        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + userName + "_training_dataSet.csv");
        try{
            fos = new FileOutputStream(file,true);
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    public void generate64FeatureList(Float x, Float y, Float z){

        float magnitude = (float)Math.sqrt(x*x + y*y + z*z);
        feature64List.add(magnitude);
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

    public boolean isExternalStorageWritable() {
       // Log.i("","I'm inside external storage!!@@@@@@@@@@");
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
    public void getFFT_Features(){


        FFT fft = new FFT(64);

        x = new double[64];
        im = new double[64];
        float[] re = new float[feature64List.size()];
        int i = 0;

        for (Float f : feature64List) {
            re[i++] = (f != null ? f : Float.NaN);
        }

        x = convertFloatsToDoubles(re);
        fft.fft(x,im);
    }

    //calculate maginitude from FFT values.
    public void getFinalFFTMagnitude(){

        finalFFTFeature64 = new ArrayList<Double>();

       for(int k=0;k<x.length;k++){
           double mag = Math.sqrt(x[k] * x[k] + im[k] * im[k]);
           finalFFTFeature64.add(mag);

       }

       Log.i("finalFeatureFFT","" + finalFFTFeature64);
    }
    //**************** above code for generating FFT features************************//




    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor mySensor = event.sensor;


        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];


        }
        if(mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            //pass through low pass filter
            filterOut = lowPass(event.values.clone(), filterOut);
            //end of calling low pass filter

            if(feature64List.isEmpty() || feature64List.size() == 64){
                if(feature64List.isEmpty()){
                    feature64List = new ArrayList<Float>();
                }

                else if(feature64List.size() == 64){

                    getMaxFeature();
                    //************* *FFT functionality to be confirmed****************//
                    getFFT_Features();
                    getFinalFFTMagnitude();
                    convertFFTtoString();
                    //***********FFT functionality to be confirmed*************//

                    //initial magnitude to string
                    //convertFeaturesToString();

                    if (isExternalStorageWritable()) {
                        try {
                            //Log.i("TAG1", "##################now writing to the file");
                            content = content + maxMagnitude.toString() + "," + activity + "," + userName + "\n";
                            Log.i("Content!$$$$$$$$$","" + content);
                            //String content = activity + "," + name.getText() + "," + Float.toString(x) + "," + Float.toString(y) + "," + Float.toString(z) +"\n";
                            fos.write(content.getBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    feature64List = new ArrayList<Float>();
                    maxMagnitude = 0;
                    content = "";
                }

            }
//******************pass the filtered output to Window buffer of size 64*************//

            long curTime = System.currentTimeMillis();

                if((curTime - lastUpdate) >= 20) {


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

        if(mySensor.getType() == Sensor.TYPE_STEP_COUNTER) {
           // Log.i("","####STEP COUNTER DETECTED!!");
        }


    }

    @Override
    public void onDestroy(){
        Log.v("SERVICE","Service killed");

        super.onDestroy();
        mSensorManager.unregisterListener(this);

        try{
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
