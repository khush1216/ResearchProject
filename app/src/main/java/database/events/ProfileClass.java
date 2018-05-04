package database.events;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

/**
 * Created by Khushbu on 4/9/2018.
 */

//get user profile details from database
public class ProfileClass {

    public ProfileClass(){

    }

    public ProfileClass(String n, String dob, String email_id){

    }

    public void writeNewUser(DatabaseReference mDatabase, HashMap<String,String> userDetailsMap, String userDetails[]){


        //mDatabase.child("users").child(userDetails[0]).child("email").setValue(userDetailsMap.get("email"));
        mDatabase.child(userDetails[0]).child("address").setValue(userDetailsMap.get("address"));
        mDatabase.child(userDetails[0]).child("gender").setValue(userDetailsMap.get("gender"));
        mDatabase.child(userDetails[0]).child("age").setValue(userDetailsMap.get("age"));
        mDatabase.child(userDetails[0]).child("weight").setValue(userDetailsMap.get("weight"));
        mDatabase.child(userDetails[0]).child("dateofbirth").setValue(userDetailsMap.get("dob"));
        mDatabase.child(userDetails[0]).child("username").setValue(userDetailsMap.get("username"));

    }




}
