package edu.uic.kdurge2.cs478.proj1_temp;
import android.app.Service;
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
import java.util.concurrent.ArrayBlockingQueue;

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
import android.util.Log;
import android.widget.Toast;

import com.meapsoft.FFT;



public class ServiceSensor extends Service implements SensorEventListener {

    private static final int noOfFeatures = Variables_Globals.ACCELEROMETER_FEATURES + 2;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mStepCounter;
    private int mServiceTaskType;
    private Instances mDataInstance;
    private Attribute mClassAttribute;
    private Attribute mAttribute;
    private OnSensorChangedAsyncTask mAsyncTask;
    Classifier j48Classifier;

    public static boolean isStepAvailable;
    public static float noOfSteps = 0;
    private long lastUpdate = 0;


    //testing

    private float[] filterOut;


    private static final float ALPHA = 0.25f;


    //async
    private static ArrayBlockingQueue<Double> mInputBuffer;

    @Override
    public void onCreate(){
        super.onCreate();
        mInputBuffer = new ArrayBlockingQueue<Double>(Variables_Globals.ACCELEROMETER_BUFFER_SIZE);
        readModel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //PackageManager packageManager = getPackageManager();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        //add step detector
       // mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
       // mSensorManager.registerListener(this,mStepCounter,SensorManager.SENSOR_DELAY_NORMAL);
      //  boolean step_exists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);

       // Log.i("IS STEP AVAILABLE??","####################" + Boolean.toString(step_exists));

        ArrayList<Attribute> allAttributes = new ArrayList<Attribute>();
//        //DecimalFormat df = new DecimalFormat("00");
//        ArrayList<String> labelItems = new ArrayList<String>(1);
//        labelItems.add(Variables_Globals.CLASS_PREDICT_LABEL);
//
//        //mClassAttribute = new Attribute(Variables_Globals.CLASS_LABEL_KEY,labelItems);
//
        ArrayList<String> classLabels = new ArrayList<String>(1);
        classLabels.add("RUN");
        classLabels.add("WALK");
        classLabels.add("SIT");

        mClassAttribute = new Attribute(Variables_Globals.CLASS_LABEL_KEY,classLabels);



        for(int i=1;i<=Variables_Globals.ACCELEROMETER_FEATURES; i++){
            allAttributes.add(new Attribute(Variables_Globals.FFT_VARIABLE + Integer.toString(i)));
        }
        allAttributes.add(new Attribute(Variables_Globals.MAX_MAGNITUDE));
        allAttributes.add(mClassAttribute);

        mDataInstance = new Instances(Variables_Globals.FEATURE_SET_NAME,allAttributes,Variables_Globals.FEATURE_SET_CAPACITY);
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
            FFT fft = new FFT(Variables_Globals.ACCELEROMETER_FEATURES);
            double[] accBlock = new double[Variables_Globals.ACCELEROMETER_FEATURES];
            double[] realValue = accBlock;
            double[] imgValue = new double[Variables_Globals.ACCELEROMETER_FEATURES];

            double max = Double.MIN_VALUE;

//            /********************************testing static values*****************/
//            Instance instStatic = new DenseInstance(noOfFeatures);
//            instStatic.setDataset(mDataInstance);
//            for(int j=0;j<StaticDataSets.attributesRun.length;j++){
//                instStatic.setValue(j,StaticDataSets.attributesRun[j]);
//            }
//
//            instStatic.setValue(Variables_Globals.ACCELEROMETER_FEATURES,StaticDataSets.maxMagStaticRun);
//            //instStatic.setValue(mClassAttribute,Variables_Globals.CLASS_PREDICT_LABEL);
//            mDataInstance.add(instStatic);
//            try {
//                double pred = j48Classifier.classifyInstance(mDataInstance.instance(0));
//                Log.i(Variables_Globals.TAG, "STATIC PREDICTION !!!!#####" + mDataInstance.classAttribute().value((int) pred));
//            }
//            catch (Exception e){
//                Log.i(Variables_Globals.TAG, "CAUGHT AN EXCEPTION ON STATIC VALUES!!");
//                e.printStackTrace();
//            }
            /*****************************************end of testing static values************/

            while(flag == true){
                try{
                    if(isCancelled() == true){
                        Log.i(Variables_Globals.TAG," in on cancelled!!!!!!!!!!!!!!!!!!!");

                        return null;
                    }
                    accBlock[blockSize++] = mInputBuffer.take().doubleValue();
                    if(blockSize == Variables_Globals.ACCELEROMETER_FEATURES){
                        // Log.i(Variables_Globals.TAG, " block size enough!!!!!!!!!!!!!!!!!!!");

                        blockSize =0;
                        max = .0;

                        for(double val : accBlock){
                            if(max<val){
                                max = val;
                            }
                        }

                        fft.fft(realValue,imgValue);
                        for(int i=0;i<realValue.length;i++){
                            double magnitude = Math.sqrt(realValue[i] * realValue[i] + imgValue[i]*imgValue[i]);
                            inst.setValue(i,magnitude);
                            imgValue[i] = .0;
                        }

                        inst.setValue(Variables_Globals.ACCELEROMETER_FEATURES,max);
                        //inst.setValue(mClassAttribute,Variables_Globals.CLASS_PREDICT_LABEL);
                        mDataInstance.add(inst);
                        double classPredicted = j48Classifier.classifyInstance(inst);
                        Log.i(Variables_Globals.TAG,"ACTIVITY RECOGNIZED!!!"+mDataInstance.classAttribute().value((int) classPredicted));

                        publishProgress(mDataInstance.classAttribute().value((int) classPredicted));
                        //pass instance to classifier
                    }
                    else {
                        Log.i(Variables_Globals.TAG, " block size not enough!!!!!!!!!!!!!!!!!!!");
                    }

                }

                catch(Exception e){
                    Log.i(Variables_Globals.TAG," exception caught!!!!!!!!!!!!!!!!!!");
                    e.printStackTrace();

                }
            }
            return null;
        }

        protected void onProgressUpdate(String... progress){

            Log.i(Variables_Globals.TAG," in on progress!!!!!!!!!!!!!!!!!!!");

            String class_label = progress[0];
            PredictionMainActivity.classLabel.setText(class_label);

        }

        @Override
        protected void onCancelled(){
            super.onCancelled();

        }


    }

    @Override
    public void onSensorChanged(SensorEvent event){


        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            long curTime = System.currentTimeMillis();

            if((curTime - lastUpdate) >= 20) {

                lastUpdate = curTime;
                filterOut = lowPass(event.values.clone(), filterOut);

                double magnitude = Math.sqrt(filterOut[0] * filterOut[0] + filterOut[1] * filterOut[1] + filterOut[2] * filterOut[2]);
                try {
                    mInputBuffer.add(new Double(magnitude));
                } catch (IllegalStateException e) {

                    ArrayBlockingQueue<Double> newBuffer = new ArrayBlockingQueue<Double>(mInputBuffer.size() * 2);
                    mInputBuffer.drainTo(newBuffer);
                    mInputBuffer = newBuffer;
                    mInputBuffer.add(new Double(magnitude));
                }
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){

            Log.i("NO OF STEPS!","??????????????????"+Float.toString(event.values[0]));
            noOfSteps = event.values[0];

        }

    }

    public void readModel() {

        try {
            String path  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + "RandomForestClassifier2nd4thApril.model";
            j48Classifier = (Classifier) weka.core.SerializationHelper.read(path);

//            ObjectInputStream ois = new ObjectInputStream(
//                    new FileInputStream("C:/Users/Khushbu/Desktop/uic/main/M_Project/J48WekaClassifier.model"));
//            Classifier cls = (Classifier) ois.readObject();
//            ois.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public ServiceSensor() {
    }

    public void onDestroy(){
        Log.v("SERVICE","Service killed");

        super.onDestroy();
        mSensorManager.unregisterListener(this);

    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

