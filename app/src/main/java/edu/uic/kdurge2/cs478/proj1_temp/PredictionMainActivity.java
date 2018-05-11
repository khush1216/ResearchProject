package edu.uic.kdurge2.cs478.proj1_temp;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.meapsoft.SpeedCalculator;

import org.w3c.dom.Text;

import java.util.ArrayList;


//tests on different cases. First case is without considering peaks and time between peaks.
//the second case is still incomplete prediction. The accuracy obtained by the logistic regression classifier
//is about 86%.
public class PredictionMainActivity extends BaseActivity {

    TextView classLabel;
    TextView classLabelPeaks;
    private Button startBtn, stopBtn1;
    private Button startbtn2 ,stopBtn2;

    private boolean isService1Running = false;
    private boolean isService2Running = false;

    Intent mServiceIntent;
    Intent mPeakServiceIntent;
    private BroadcastReceiver bReceiver;
    Thread threadForSecondService;
    Thread threadForFirstService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_main);

        classLabel = (TextView) findViewById(R.id.activityID);
        classLabelPeaks = (TextView) findViewById(R.id.activityID2);

        /*startBtn = (Button) findViewById(R.id.startbtn);
        startbtn2 = (Button) findViewById(R.id.startbtn2);

        stopBtn1 = (Button) findViewById(R.id.stopBtn);
        stopBtn2 = (Button) findViewById(R.id.stopbtn2);*/

        mPeakServiceIntent = new Intent(this,ServiceSensorPeaks.class);
        mServiceIntent = new Intent(this,ServiceSensor.class);

        mServiceIntent.putExtra("callingActivityName","StillActivity");
//        String yourFilePath = this.getFilesDir() + "/";
//        Log.i("File path","%%%%%%%%%%%%%%%%"+ yourFilePath);

        bReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("activity")) {
                    String activityPredicted = intent.getStringExtra("activity");

                    classLabel.setText(activityPredicted);
                }

                if (intent.getAction().equals("activityPeaks")) {
                    String activityPredicted = intent.getStringExtra("activityPeaks");

                    classLabelPeaks.setText(activityPredicted);
                }
                }

        };

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
                                startActivity(new Intent(PredictionMainActivity.this, SignInActivity.class));
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
                                startActivity(new Intent(PredictionMainActivity.this, SignInActivity.class));
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


    @Override
    protected void onResume() {
        super.onResume();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

            IntentFilter intentFilter =    new IntentFilter();
            intentFilter.addAction("activity");
            intentFilter.addAction("activityPeaks");

            LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, intentFilter);


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

        if(isService2Running){
            Toast.makeText(PredictionMainActivity.this, "SERVICE WITH PEAKS IS RUNNING. STOP THAT FIRST", Toast.LENGTH_SHORT).show();

        }
        else {
            isService1Running = true;

            threadForFirstService = new Thread() {

                public void run() {
                    startService(mServiceIntent);
                }
            };
            threadForFirstService.start();

            Toast.makeText(this, "SERVICE 1 RUNNING!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onStart2Clicked(View view) {

        if (isService1Running) {
            Toast.makeText(PredictionMainActivity.this, "SERVICE 1 IS RUNNING ON SAME THREAD, STOP THAT FIRST!", Toast.LENGTH_SHORT).show();

        } else {

            isService2Running = true;
            threadForSecondService = new Thread() {
                @Override
                public void run() {

                    startService(mPeakServiceIntent);
                }
            };
            threadForSecondService.start();

            Toast.makeText(this, "SERVICE 2 RUNNING!", Toast.LENGTH_SHORT).show();

        }
    }

    public void onStop2Clicked(View view){

        isService2Running = false;
        threadForSecondService.interrupt();

        Toast.makeText(this, "SERVICE 2 STOPPED!", Toast.LENGTH_SHORT).show();

        stopService(mPeakServiceIntent);

    }

    public void onstopbtnClick(View view){

        isService1Running = false;
        threadForFirstService.interrupt();
        Toast.makeText(this, "SERVICE 1 STOPPED!", Toast.LENGTH_SHORT).show();
        stopService(mServiceIntent);

    }


    public void onDestroy() {
        // Stop the service and the notification.
        stopService(mServiceIntent);
        finish();
        super.onDestroy();
    }

}
