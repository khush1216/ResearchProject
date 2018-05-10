package edu.uic.kdurge2.cs478.proj1_temp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import database.events.History;
import database.events.ProfileClass;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class UserProfile extends BaseActivity {

    private String userDetails[];
    private boolean dataExists=false;

    DatePickerDialog.OnDateSetListener date;
    Calendar myCalendar;

    public static HashMap<String,String> userProfileDetails;

    EditText  address, dob, age, weight;
    String uID;
    TextView email_id,name_nav,email_nav,userHeading,username;
    RadioButton genderF, genderM;

    String addressStr, userHeadingStr, email_idStr, genderStr, dateofbirth,lastname,ageStr,weightStr;

    private DatabaseReference mDatabase;
    public static DatabaseReference mUserDatabaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        dob = (EditText) findViewById(R.id.dobID);
        weight = (EditText) findViewById(R.id.weightID);
        age = (EditText) findViewById(R.id.ageID);
        userHeading = (TextView) findViewById(R.id.mainUserName);
        username = (TextView) findViewById(R.id.userNameID);

        address = (EditText) findViewById(R.id.addressID);
        email_id = (TextView) findViewById(R.id.emailID);
        genderF = (RadioButton) findViewById(R.id.female);
        genderM = (RadioButton) findViewById(R.id.male);

        Log.i("FILES DIRECTORY!!",this.getFilesDir().toString());

        myCalendar = Calendar.getInstance();
        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(UserProfile.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        Intent intent = getIntent();
        userDetails = intent.getStringArrayExtra("userdetails");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        uID = userDetails[0];

        email_id.setText(userDetails[2]);
        userHeading.setText(userDetails[1]);

        username.setText(userDetails[1]);
        mDatabase.child(userDetails[0]).child("email").setValue(userDetails[2]);
        mDatabase.child(userDetails[0]).child("username").setValue(userDetails[1]);


        mUserDatabaseRef = mDatabase.child(userDetails[0]);
        userProfileDetails = new HashMap<String, String>();

        updateProfile();

        FloatingActionButton fab = findViewById(R.id.fab);

        //for editing the content
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                address.setEnabled(true);
                dob.setEnabled(true);
                age.setEnabled(true);
                weight.setEnabled(true);
                Snackbar.make(view, "You can now edit !", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dob.setText(sdf.format(myCalendar.getTime()));
    }

    public void onRadioButtonClicked(View view){
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()){
            case R.id.female:
                genderStr = "female";
                break;

            case R.id.male:
                genderStr = "male";
                break;

        }
    }
    public void saveData(View view){
        if(TextUtils.isEmpty(age.getText()) || TextUtils.isEmpty(dob.getText()) || TextUtils.isEmpty(weight.getText())){

            Toast.makeText(this, "Complete Mandatory fields!", Toast.LENGTH_SHORT).show();
        }

        else {
            address.setEnabled(false);
            dob.setEnabled(false);
            age.setEnabled(false);
            weight.setEnabled(false);

            ProfileClass profileData = new ProfileClass();

            addressStr = address.getText().toString();
            dateofbirth = dob.getText().toString();
            ageStr = age.getText().toString();
            weightStr = weight.getText().toString();
            if (genderF.isChecked()) {
                genderStr = "female";
            } else {
                genderStr = "male";
            }

            userProfileDetails.put("username", userHeadingStr);
            userProfileDetails.put("lastname", lastname);
            userProfileDetails.put("gender", genderStr);
            userProfileDetails.put("weight",weightStr);
            userProfileDetails.put("dob", dateofbirth);
            userProfileDetails.put("age", ageStr);
            userProfileDetails.put("address", addressStr);

            profileData.writeNewUser(mDatabase, userProfileDetails, userDetails);
            Toast.makeText(this, "Your Profile has been saved! Start your excercise.", Toast.LENGTH_SHORT).show();
        }
    }

    public void viewHistory(View view){
        Intent histInt = new Intent(this, HistoryActivity.class);
        startActivity(histInt);
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
                                startActivity(new Intent(UserProfile.this, SignInActivity.class));
                                finish();
                            }
                        });
                return true;
            case R.id.deleteAcc:

                DatabaseReference deleteRef = mDatabase.child(userDetails[0]);
                deleteRef.getRef().removeValue();
                Toast.makeText(this, "Deleting your data...", Toast.LENGTH_SHORT).show();
                AuthUI.getInstance()
                        .delete(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                startActivity(new Intent(UserProfile.this, SignInActivity.class));
                                finish();
                            }
                        });
                Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();

                return true;
            case R.id.help:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void updateProfile(){

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.getKey().equals(uID)) {

                    if(data.child("address").exists()) {
                        addressStr = data.child("address").getValue().toString();
                        address.setText(addressStr);
                        address.setEnabled(false);
                        dataExists = true;
                        userProfileDetails.put("address",addressStr);

                    } else {
                        Log.i("","");
                    }
                    if (data.child("dateofbirth").exists()) {
                        dateofbirth = data.child("dateofbirth").getValue().toString();
                        dob.setText(dateofbirth);
                        dob.setEnabled(false);

                        userProfileDetails.put("dob",dateofbirth);
                    } else {
                    }
                    if (data.child("email").exists()) {
                        email_idStr = data.child("email").getValue().toString();

                    } else {
                    }

                        if (data.child("age").exists()) {
                            ageStr = data.child("age").getValue().toString();
                            age.setText(ageStr);
                            age.setEnabled(false);

                            userProfileDetails.put("age",ageStr);

                        } else {
                        }

                        if (data.child("weight").exists()) {
                            weightStr = data.child("weight").getValue().toString();
                            weight.setText(weightStr);
                            weight.setEnabled(false);

                            userProfileDetails.put("weight",weightStr);
                        } else {
                        }
                    if (data.child("gender").exists()) {
                        genderStr = data.child("gender").getValue().toString();
                        if(genderStr.equals("female")){
                            genderF.setChecked(true);
                        }
                        else if (genderStr.equals("male")){
                            genderM.setChecked(true);
                        }
                        userProfileDetails.put("gender",genderStr);

                    } else {

                    }

                    if (data.child("lastname").exists()) {
                        lastname = data.child("lastname").getValue().toString();
                        userProfileDetails.put("lastname",lastname);

                    } else {

                    }
                    if (data.child("username").exists()) {
                        userHeadingStr = data.child("username").getValue().toString();
                        username.setText(userDetails[1]);

                        userProfileDetails.put("username",userHeadingStr);
                    } else {

                    }
                }
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.i("ERROR!!","Could not retreive your data for some unknown error!");
                Log.i("ERROR",firebaseError.getMessage());
            }
        });
        }


    @Override
    public void onBackPressed()
    {
        //sign out on back pressed
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        startActivity(new Intent(UserProfile.this, SignInActivity.class));
                        finish();
                    }
                });
        super.onBackPressed();
    }
}
