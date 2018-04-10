package edu.uic.kdurge2.cs478.proj1_temp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends BaseActivity {

    private static final int RC_SIGN_OUT = 1234;

    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

    RadioButton walk,run,sit,cycle;
    Button start,stop,delete,uploadCloud;
    private String activity = "";
    private String userName;

    //uiHandler
    public static Handler uiHandler;

    public static float x_ui,y_ui,z_ui;

    private enum State {
        IDLE, COLLECTING, TRAINING, CLASSIFYING
    };

    private State state;

    public static TextView x_axis, y_axis, z_axis, x_gyro, y_gyro, z_gyro;
    //username
    EditText name;
    //service intent
    Intent mServiceIntent;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(R.string.title_app);

        state = State.IDLE;
        mServiceIntent = new Intent(this, MovementService.class);

        //text views that show the x,y,z readings for accelerometer
        x_axis = (TextView) findViewById(R.id.x_axis);
        y_axis = (TextView) findViewById(R.id.y_axis);
        z_axis = (TextView) findViewById(R.id.z_axis);

        //not using gyroscope as yet
        x_gyro = (TextView) findViewById(R.id.x_gyro);
        y_gyro = (TextView) findViewById(R.id.y_gyro);
        z_gyro = (TextView) findViewById(R.id.z_gyro);

        //input user name
        name = (EditText) findViewById(R.id.name);

        //selecting activities
        walk = (RadioButton) findViewById(R.id.walk);
        run = (RadioButton) findViewById(R.id.run);
        sit = (RadioButton) findViewById(R.id.sit);
        cycle = (RadioButton) findViewById(R.id.cycle);

        //start/stop service, delete current data
        start = (Button) findViewById(R.id.startBtn);
        stop = (Button) findViewById(R.id.stopBtn) ;
        delete = (Button) findViewById(R.id.deleteData);
        uploadCloud = (Button) findViewById(R.id.uploadData);


        uiHandler = new Handler(Looper.getMainLooper());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.signOut:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                finish();
                            }
                        });
                return true;
            case R.id.deleteAcc:
                AuthUI.getInstance()
                        .delete(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                finish();
                                // ...
                            }
                        });
                return true;
            case R.id.help:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void uploadToCloud(View view){

        Log.i("TAG1","Inside upload method!");

        File extStore = Environment.getExternalStorageDirectory();
        File dataFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + userName + "_training_dataSet.csv");

        if(dataFile.exists()){

            Log.i("TAG1","FILE FOUND!!!!##########");

            Uri file = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + userName + "_training_dataSet.csv"));
           // StorageReference mUpload = mStorageRef.child(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/training_dataSet.csv");
            StorageReference mUpload = mStorageRef.child(file.getLastPathSegment());


            mUpload.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(MainActivity.this, "UPLOAD SUCCESSFUL! ;)", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this, "UPLOAD IN PROGRESS...", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "UNABLE TO UPLOAD! TRY LATER :(", Toast.LENGTH_SHORT).show();
                }
            });
        }

        else{
            Toast.makeText(MainActivity.this, "NO SUCH FILE!", Toast.LENGTH_SHORT).show();
        }

    }


    public static void updateUI(){

        //update text view. is it a good idea to keep static text views?? what are the other options apart from using bound service
        //need to analyse
        x_axis.setText(Float.toString(x_ui));
        y_axis.setText(Float.toString(y_ui));
        z_axis.setText(Float.toString(z_ui));

    }

    //update the activity selected
    public void onRadioButtonClicked(View view){

        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()){
            case R.id.walk:
                if(checked){
                    activity = "WALK";
                }
                break;
            case R.id.run:
                if(checked)
                    activity = "RUN";
                break;
            case R.id.sit:
                if(checked)
                    activity = "SIT";
                break;
            case R.id.cycle:
                if(checked)
                    activity = "CYCLE";
                break;
            default:
                activity = "NOT_SELECTED";
                break;

        }


    }

    //method for deleting currently saved data
    public void deleteData(View view){

        String userName2 = name.getText().toString();
        if(userName2.isEmpty()){
            Toast.makeText(this, "Enter USERNAME", Toast.LENGTH_SHORT).show();

        }
        else {
            //Log.i("FILE !!!!","IM HERE!!!!!");
            String selectedFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + userName2 + "_training_dataSet.csv";
            //Log.i("FILE !!!!","PATH ISSS!!!!!" + selectedFilePath);
            File file = new File(selectedFilePath);
            boolean deleted = file.delete();

            if (deleted) {
                Toast.makeText(this, "DATA DELETED! START AGAIN!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "NO DATA AVAILABLE TO DELETE. PRESS START", Toast.LENGTH_SHORT).show();
            }

        }

    }

    //start data collection
    public void onStartClick(View view) {

            if(activity.equals("")){
                Toast.makeText(this, "PLEASE SELECT AN ACTIVITY TRAINING!", Toast.LENGTH_SHORT).show();
            }
            else {

                start.setEnabled(false);
                delete.setEnabled(false);
                uploadCloud.setEnabled(false);


               // Log.i("TAG1", "start clicked!############");

                state = State.COLLECTING;
                userName = name.getText().toString();
                Bundle bundle = new Bundle();
                bundle.putString("user_name", userName);
                bundle.putString("class_label", activity);
                mServiceIntent.putExtras(bundle);
               // Log.i("TAG1", "starting service!############");

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        Log.i("TAG1", "starting service on a new thread!!@@");

                        startService(mServiceIntent);
                    }
                };
                t.start();
            }


    }

    //stop data collection
    public void onStopClick(View view) {

        delete.setEnabled(true);
        uploadCloud.setEnabled(true);

        Toast.makeText(this, "DATA COLLECTED!", Toast.LENGTH_SHORT).show();


        start.setEnabled(true);

        state = State.IDLE;
        stopService(mServiceIntent);

    }

    public void onDestroy() {
        // Stop the service and the notification.
        // Need to check whether the mSensorService is null or not.
        stopService(mServiceIntent);
        finish();
        super.onDestroy();
    }


    @Override // android recommended class to handle permissions
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("permission", "granted!!!");
                } else {

                    // functionality that depends on this permission.uujm
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //write to external storage
    protected void onResume() {
        super.onResume();
        // mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        //mSensorManager.registerListener(this,mGyroscope,SensorManager.SENSOR_DELAY_NORMAL);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        }


    }


    protected void onPause() {
        super.onPause();
        Log.i("","in on pause");
        // mSensorManager.unregisterListener(this);
//        try{
//            fos.close();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//        if (writer != null) {
//            try {
//                writer.close();
//            } catch (Exception e) {
//            }
//        }

    }
}