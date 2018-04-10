package edu.uic.kdurge2.cs478.proj1_temp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import database.events.ProfileClass;

import org.w3c.dom.Text;

import java.util.HashMap;

public class UserProfile extends BaseActivity {

    private String userDetails[];
    private boolean dataExists=false;

    private HashMap<String,String> userProfileDetails;
    
    EditText  address, dob;
    String uID;
    TextView userHeading, email_id,username;
    RadioButton genderF, genderM;

    String addressStr, userHeadingStr, email_idStr, genderStr, dateofbirth,lastname;

    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        dob = (EditText) findViewById(R.id.dobID);
        userHeading = (TextView) findViewById(R.id.mainUserName);
        username = (TextView) findViewById(R.id.userNameID);
        address = (EditText) findViewById(R.id.addressID);
        email_id = (TextView) findViewById(R.id.emailID);
        genderF = (RadioButton) findViewById(R.id.female);
        genderM = (RadioButton) findViewById(R.id.male);

        Intent intent = getIntent();
        userDetails = intent.getStringArrayExtra("userdetails");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        uID = userDetails[0];

        email_id.setText(userDetails[2]);
        userHeading.setText(userDetails[1]);
        username.setText(userDetails[1]);
        mDatabase.child(userDetails[0]).child("email").setValue(userDetails[2]);
        mDatabase.child(userDetails[0]).child("username").setValue(userDetails[1]);

        updateProfile();

        if(dataExists){

        }

    }

    public void saveData(View view){
        ProfileClass profileData = new ProfileClass();
        userProfileDetails = new HashMap<String, String>();

        addressStr = address.getText().toString();
        dateofbirth = dob.getText().toString();
        if(genderF.isChecked()){
            genderStr = "female";
        }
        else {
            genderStr = "male";
        }

        //userProfileDetails.put("username",userHeadingStr);
        userProfileDetails.put("lastname",lastname);
        userProfileDetails.put("gender",genderStr);
        //userProfileDetails.put("email",email_idStr);
        userProfileDetails.put("dob",dateofbirth);
        userProfileDetails.put("address",addressStr);

        profileData.writeNewUser(mDatabase, userProfileDetails, userDetails);
        Toast.makeText(this, "Your Profile has been saved! Start your excercise.", Toast.LENGTH_SHORT).show();


    }

    public void startExcerice(View view){
        //Intent selectAppState = new Intent(this, SelectAppState.class);
        //startActivity(selectAppState);
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
                        dataExists = true;
                    } else {
                        Log.i("","");
                    }
                    if (data.child("dateofbirth").exists()) {
                        dateofbirth = data.child("dateofbirth").getValue().toString();
                        dob.setText(dateofbirth);
                    } else {
                        Log.i("TAG", "Should not come here!!!");
                    }
                    if (data.child("email").exists()) {
                        email_idStr = data.child("email").getValue().toString();
                    } else {
                        Log.i("TAG", "Should not come here!!!");
                    }

                    if (data.child("gender").exists()) {
                        genderStr = data.child("gender").getValue().toString();
                        if(genderStr == "female"){
                            genderF.setChecked(true);
                        }
                        else if (genderStr == "male"){
                            genderM.setChecked(true);
                        }

                    } else {

                    }

                    if (data.child("lastname").exists()) {
                        lastname = data.child("lastname").getValue().toString();

                    } else {

                    }
                    if (data.child("username").exists()) {
                        userHeadingStr = data.child("username").getValue().toString();
                        username.setText(userDetails[1]);
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
