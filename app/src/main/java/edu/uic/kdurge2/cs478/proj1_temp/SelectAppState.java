package edu.uic.kdurge2.cs478.proj1_temp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SelectAppState extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_app_state);

    }


        //Button btn = (Button) findViewById(R.id.predict);
        //btn.setEnabled(false);


    public void startCollectActivity(View view){
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }

    public void startPredictionActivity(View view){
        Intent i = new Intent(this,PredictionMainActivity.class);
        startActivity(i);

   }
    public void viewOnMap(View view){
        Intent i = new Intent(this,MapsActivity.class);
        startActivity(i);

    }

}
