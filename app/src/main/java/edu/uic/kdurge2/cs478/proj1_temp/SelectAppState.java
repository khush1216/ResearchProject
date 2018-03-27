package edu.uic.kdurge2.cs478.proj1_temp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelectAppState extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_app_state);

        //Button btn = (Button) findViewById(R.id.predict);
        //btn.setEnabled(false);
    }

    public void startCollectActivity(View view){
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }

    public void startPredictionActivity(View view){
        Intent i = new Intent(this,PredictionMainActivity.class);
        startActivity(i);

    }

}
