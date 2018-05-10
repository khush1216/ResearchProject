package edu.uic.kdurge2.cs478.proj1_temp;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.ConversionMethods;
import com.meapsoft.FFT;
import com.meapsoft.SpeedCalculator;

import com.peakChecker.PeakCheckerClass;


public class ServiceSensorPeaks extends Service implements SensorEventListener {

    //8 peaks, max mag, no of peaks, time between peaks, class label
    private static final int noOfFeatures = Variables_Globals.PEAKS_FFT_FEATURES + 4;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mStepCounter;
    private int mServiceTaskType;
    private Instances mDataInstance;
    private Attribute mClassAttribute;
    private Attribute mAttribute;
    private OnSensorChangedAsyncTask mAsyncTask;
    Classifier logisticClassifier;
    private Intent mServiceIntent;
    DecimalFormat f;
    private ArrayList<Float> speedCalc, oldValues;
    private boolean firstPass = false;

    public static boolean isStepAvailable;
    public static float noOfSteps = 0;
    private long lastUpdate = 0;


    //public static double speedFinal;
    SpeedCalculator speedCal;

    PeakCheckerClass peakChecker;
    Map<Double,Long> peakTimeMap;
    long curTime;
    double no_of_peaks;
    long averagepeakTimeDiff =0;



    Intent broadcastIntent;


    //testing

    private float[] filterOut;
    ConversionMethods conversionMethodsObj;


    private static final float ALPHA = 0.25f;


    //async
    private static ArrayBlockingQueue<Double> mInputBufferPeaks;

    @Override
    public void onCreate(){
        super.onCreate();
        mInputBufferPeaks = new ArrayBlockingQueue<Double>(Variables_Globals.ACCELEROMETER_BUFFER_SIZE);
        readModelFromInternal();
        //readModel();
        speedCal = new SpeedCalculator();
        oldValues = new ArrayList<Float>();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        PackageManager packageManager = getPackageManager();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_FASTEST);

        //add step detector
        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mSensorManager.registerListener(this,mStepCounter,SensorManager.SENSOR_DELAY_NORMAL);
        boolean step_exists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);


        peakChecker = new PeakCheckerClass();
        peakTimeMap = new ConcurrentHashMap<>();
        conversionMethodsObj = new ConversionMethods();


        ArrayList<Attribute> allAttributes = new ArrayList<Attribute>();

        ArrayList<String> classLabels = new ArrayList<String>(1);
        classLabels.add("SIT");
        classLabels.add("RUN");
        classLabels.add("WALK");

        mClassAttribute = new Attribute(Variables_Globals.CLASS_LABEL_KEY,classLabels);



        for(int i=0;i<Variables_Globals.PEAKS_FFT_FEATURES; i++){
            allAttributes.add(new Attribute(Variables_Globals.FFT_VARIABLE + Integer.toString(i)));
        }

        allAttributes.add(new Attribute(Variables_Globals.MAX_MAGNITUDE));
        //change var name
        allAttributes.add(new Attribute(Variables_Globals.NUMBER_OF_PEAKS));
        allAttributes.add(new Attribute(Variables_Globals.TIME_BETWEEN_PEAKS));
        allAttributes.add(mClassAttribute);

        mDataInstance = new Instances(Variables_Globals.FEATURE_SET_NAME,allAttributes,Variables_Globals.FEATURE_SET_CAPACITY);
        //fetaure set name = accelerometer features
        //feature set capacity = 10000;

        mDataInstance.setClassIndex(mDataInstance.numAttributes() - 1);

        mAsyncTask = new OnSensorChangedAsyncTask();
        mAsyncTask.execute();

        return START_NOT_STICKY;

    }

    protected float[] lowPass(float[] input, float output[]){

        if(output == null)return input;

        for(int i=0;i<input.length;i++){
            //more weight on output than input
            output[i] = output[i] + ALPHA *(input[i] - output[i]);
        }

        return output;

    }

    private class OnSensorChangedAsyncTask extends AsyncTask<Void,String,Void> {

        @Override
        protected Void doInBackground(Void... arg0) {

            boolean flag = true;

            Instance inst = new DenseInstance(noOfFeatures);
            inst.setDataset(mDataInstance);
            int blockSize = 0;
            int blockSize256 = 0;
            FFT fft = new FFT(8);

            double[] accBlock256 = new double[Variables_Globals.ACCELEROMETER_FEATURES_256];

            // double[] accBlock = new double[Variables_Globals.ACCELEROMETER_FEATURES];
            double[] realValue = new double[Variables_Globals.PEAKS_FFT_FEATURES];
            double[] imgValue = new double[Variables_Globals.PEAKS_FFT_FEATURES];

            double max = Double.MIN_VALUE;

            while(flag == true){
                try{
                    if(isCancelled() == true){

                        return null;
                    }
                    accBlock256[blockSize256++] = mInputBufferPeaks.take().doubleValue();
                    if(blockSize256 == Variables_Globals.ACCELEROMETER_FEATURES_256) {

                        blockSize256 = 0;
                        max = .0;

                        for(double val : accBlock256){
                            if(max<val){
                                max = val;
                            }
                        }

                        ArrayList<Double> accBlock256List = new ArrayList<Double>();
                        for(int i=0;i<accBlock256.length;i++){
                            accBlock256List.add(accBlock256[i]);
                        }

//                        double[] accBlock256Ref = new double[256];
//                        accBlock256Ref = conversionMethodsObj.convertPrimitiveToRef(accBlock256);
//
//
//                        ArrayList<Double> accBlock256List = new ArrayList<Double>(Arrays.asList(accBlock256));

                        ArrayList<Double> peaksFFT = new ArrayList<Double>();
                       // ArrayList<Float> accBlock256FloatList = new ArrayList<Float>();
                        //accBlock256FloatList = conversionMethodsObj.convertDoubleToFloat(accBlock256List);

                        peaksFFT = peakChecker.getPeakDouble(accBlock256List);



                        //ArrayList<Double> peaksFFTDouble = new ArrayList<Double>();
                        //peaksFFTDouble = conversionMethodsObj.convertFloatToDoubleList(peaksFFT);

                        if(peaksFFT.size() > 8){
                            no_of_peaks = peaksFFT.size();
                            //Log.i("MAP",peakTimeMap.toString());
                            averagepeakTimeDiff = peakChecker.getPeakTimeConcurrent(peakTimeMap,peaksFFT);
                            List<Double> top8peaks = new ArrayList<Double>();
                            top8peaks = peaksFFT.subList(0, 8);

                            for(int i=0;i<top8peaks.size();i++){
                                realValue[i] = top8peaks.get(i);
                            }

                            //realValue = conversionMethodsObj.convertFloatsToDoubles(re);
                            fft.fft(realValue,imgValue);

                            for(int i=0;i<realValue.length;i++){
                                double magnitude = Math.sqrt(realValue[i] * realValue[i] + imgValue[i]*imgValue[i]);
                                inst.setValue(i,magnitude);
                                imgValue[i] = .0;
                            }

                            inst.setValue(Variables_Globals.PEAKS_FFT_FEATURES,max);
                            inst.setValue(Variables_Globals.PEAKS_FFT_FEATURES + 1,no_of_peaks);
                            inst.setValue(Variables_Globals.PEAKS_FFT_FEATURES + 2,averagepeakTimeDiff);

                            mDataInstance.add(inst);

                            double classPredicted = logisticClassifier.classifyInstance(inst);
                            Log.i(Variables_Globals.TAG,"ACTIVITY RECOGNIZED!!!"+mDataInstance.classAttribute().value((int) classPredicted));
                            //send this to classifier
                            publishProgress(mDataInstance.classAttribute().value((int) classPredicted));
                        }

                    }
                    else {
                        Log.i(Variables_Globals.TAG, " block size not enough!!!!!!!!!!!!!!!!!!!");
                    }

                }//end of try
                catch(Exception e){
                    Log.i(Variables_Globals.TAG," exception caught!!!!!!!!!!!!!!!!!!");
                    e.printStackTrace();

                }
            }
            return null;
        }

        protected void onProgressUpdate(String... progress){


            String class_label = progress[0];
            broadcastIntent = new Intent("activityPeaks");
            broadcastIntent.putExtra("activityPeaks",class_label);
            LocalBroadcastManager.getInstance(ServiceSensorPeaks.this).sendBroadcast(broadcastIntent);


        }

        @Override
        protected void onCancelled(){
            super.onCancelled();

        }


    }

    @Override
    public void onSensorChanged(SensorEvent event){


        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            curTime = System.currentTimeMillis();

            if((curTime - lastUpdate) >= 10) {
                float a = event.values[0];
                float b = event.values[1];
                float c = event.values[2];

                long interval = curTime - lastUpdate;

                lastUpdate = curTime;
                filterOut = lowPass(event.values.clone(), filterOut);

                double magnitude = Math.sqrt(filterOut[0] * filterOut[0] + filterOut[1] * filterOut[1] + filterOut[2] * filterOut[2]);

                try {
                    mInputBufferPeaks.add(new Double(magnitude));
                    peakTimeMap.put(new Double(magnitude), curTime);
                } catch (IllegalStateException e) {

                    ArrayBlockingQueue<Double> newBuffer = new ArrayBlockingQueue<Double>(mInputBufferPeaks.size() * 2);
                    mInputBufferPeaks.drainTo(newBuffer);
                    mInputBufferPeaks = newBuffer;
                    mInputBufferPeaks.add(new Double(magnitude));
                }
            }
        }

    }

    public void readModelFromInternal() {

        String fpath = this.getFilesDir() + "/" + "LogisticRegressionClassifier.model";


        try {

            logisticClassifier = (Classifier) weka.core.SerializationHelper.read(fpath);

        }
        catch (Exception e){
            Toast.makeText(this, "File Not available to classify!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }

    }

    public void readModel() {

        try {
            String path  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + "LogisticRegressionClassifier.model";
            logisticClassifier = (Classifier) weka.core.SerializationHelper.read(path);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public ServiceSensorPeaks() {
    }

    public void onDestroy(){
        Log.v("SERVICE","Service killed");

        super.onDestroy();
        mAsyncTask.cancel(true);
        mSensorManager.unregisterListener(this);


    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

