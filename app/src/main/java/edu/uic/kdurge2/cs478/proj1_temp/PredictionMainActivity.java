package edu.uic.kdurge2.cs478.proj1_temp;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class PredictionMainActivity extends AppCompatActivity {

    public static TextView classLabel;
    private Button startBtn;
    Intent mServiceIntent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_main);

        classLabel = (TextView) findViewById(R.id.activityID);
        startBtn = (Button) findViewById(R.id.startbtn);
        mServiceIntent = new Intent(this,ServiceSensor.class);
//        String yourFilePath = this.getFilesDir() + "/";
//        Log.i("File path","%%%%%%%%%%%%%%%%"+ yourFilePath);


    }

    @Override
    protected void onResume() {
        super.onResume();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("permission", "granted!!!");
                } else {
                    Toast.makeText(PredictionMainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onStartClick(View view){

        startService(mServiceIntent);
    }


    public void onstopbtnClick(View view){

        Toast.makeText(this, "SERVICE STOPPED!", Toast.LENGTH_SHORT).show();
        stopService(mServiceIntent);
        Log.i("steps are :", "@@@@@@@@@@@@@@@@@@@@@"+ Float.toString(ServiceSensor.noOfSteps));

    }


    public void onDestroy() {
        // Stop the service and the notification.
        stopService(mServiceIntent);
        finish();
        super.onDestroy();
    }

}
