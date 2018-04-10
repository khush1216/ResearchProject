package database.events;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

/**
 * Created by Khushbu on 4/9/2018.
 */

public class ProfileClass {

    public ProfileClass(){

    }

    public ProfileClass(String n, String dob, String email_id){

    }

    public void writeNewUser(DatabaseReference mDatabase, HashMap<String,String> userDetailsMap, String userDetails[]){


        //mDatabase.child("users").child(userDetails[0]).child("email").setValue(userDetailsMap.get("email"));
        mDatabase.child(userDetails[0]).child("address").setValue(userDetailsMap.get("address"));
        mDatabase.child(userDetails[0]).child("gender").setValue(userDetailsMap.get("gender"));
        mDatabase.child(userDetails[0]).child("dateofbirth").setValue(userDetailsMap.get("dob"));
        //mDatabase.child("users").child(userDetails[0]).child("username").setValue(userDetailsMap.get("username"));

    }




}
