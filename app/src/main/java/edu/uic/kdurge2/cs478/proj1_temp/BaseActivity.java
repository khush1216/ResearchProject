package edu.uic.kdurge2.cs478.proj1_temp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;


//this class is extended by all activities to implement the side menu.
public class BaseActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    TextView emailIDNav,profileNameNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void setContentView(int layoutResID)
    {
        DrawerLayout fullView = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityContainer = (FrameLayout) fullView.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(fullView);

        mDrawerLayout = findViewById(R.id.activity_container);


        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);
        emailIDNav = (TextView) headerView.findViewById(R.id.emailIDnav);
        profileNameNav = (TextView) headerView.findViewById(R.id.nameNav);

        emailIDNav.setText(SignInActivity.staticUserDetailsBase[2]);
        profileNameNav.setText(SignInActivity.staticUserDetailsBase[1]);

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);

                        int id = menuItem.getItemId();

                        if (id == R.id.dataCollect) {

                            if(SignInActivity.staticUserDetailsBase[2].equals("khushidurge@gmail.com")) {

                                Intent i = new Intent(BaseActivity.this, MainActivity.class);
                                startActivity(i);
                            }
                            else{
                                showDialogPop();
                                Toast.makeText(BaseActivity.this, "Not Authorized!", Toast.LENGTH_SHORT).show();

                            }

                        }

                        if (id == R.id.mapactivity) {
                            Intent i = new Intent(BaseActivity.this,MapsActivity.class);
                            startActivity(i);
                        }

                        if(id == R.id.treadmillAct){
                            if(SignInActivity.staticUserDetailsBase[2].equals("khushidurge@gmail.com")) {

                                Intent i = new Intent(BaseActivity.this, PredictionMainActivity.class);
                                startActivity(i);
                            }
                            else{
                                showDialogPop();
                                Toast.makeText(BaseActivity.this, "Not Authorized!", Toast.LENGTH_SHORT).show();

                            }
                        }

                        if(id == R.id.signout2){
                            AuthUI.getInstance()
                                    .signOut(BaseActivity.this)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // user is now signed out
                                            startActivity(new Intent(BaseActivity.this, SignInActivity.class));
                                            finish();
                                        }
                                    });
                        }

                        if(id == R.id.historyView){
                            Intent i = new Intent(BaseActivity.this, HistoryActivity.class);
                            startActivity(i);
                        }

                        if(id == R.id.profile){
                            Intent i = new Intent(BaseActivity.this, UserProfile.class);
                            i.putExtra("userdetails",SignInActivity.staticUserDetailsBase);
                            startActivity(i);
                        }

                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();


                        return true;
                    }
                });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        setTitle("ACTIVITY RECOGNITION");
        toolbar.setTitleTextColor(0xFFFFFFFF);

    }

    public void showDialogPop(){
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_box, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
