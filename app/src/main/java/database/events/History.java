package database.events;

import java.util.ArrayList;

import edu.uic.kdurge2.cs478.proj1_temp.UserProfile;

/**
 * Created by Khushbu on 4/13/2018.
 */


//Class stores history into database and retrieves history from database
public class History {

    private String activity,currDateTime;
    private long startTimeStamp,endTimeStamp,duration;
    private double speed, distance, calories;
//    public static long endTime;

    public History() {

    }

    public History (String act, long start, String currDate, long dur, double speed, double dist, double calories){
        this.activity = act;
        this.startTimeStamp = start;
        this.speed = speed;
        this.distance = dist;
        this.calories = calories;
        this.currDateTime = currDate;
        this.duration = dur;
    }

    public void enterHistoryToDataBase(ArrayList<History> historyList){

        for(History histObj : historyList) {


            UserProfile.mUserDatabaseRef.child("history").child(Long.toString(histObj.startTimeStamp)).child("activity").setValue(histObj.activity);
            UserProfile.mUserDatabaseRef.child("history").child(Long.toString(histObj.startTimeStamp)).child("duration").setValue(Long.toString(histObj.duration));
            UserProfile.mUserDatabaseRef.child("history").child(Long.toString(histObj.startTimeStamp)).child("datetime").setValue(histObj.currDateTime);
            UserProfile.mUserDatabaseRef.child("history").child(Long.toString(histObj.startTimeStamp)).child("averageSpeed").setValue(histObj.speed);
            UserProfile.mUserDatabaseRef.child("history").child(Long.toString(histObj.startTimeStamp)).child("distance").setValue(histObj.distance);
            UserProfile.mUserDatabaseRef.child("history").child(Long.toString(histObj.startTimeStamp)).child("calorieslost").setValue(histObj.calories);
        }
        }

//    public ArrayList<DataSnapshot> getHistoryFromDB(){
//
//
//        final ArrayList<DataSnapshot> dataObject = new ArrayList<DataSnapshot>();
//        DatabaseReference userDataRefObj = UserProfile.mUserDatabaseRef.child("history");
//        userDataRefObj.keepSynced(true);
//        userDataRefObj.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//
//                    for (DataSnapshot data : dataSnapshot.getChildren()) {
//                        dataObject.add(data);
//                    }
//
//
//                }
//
//                public void onCancelled(DatabaseError firebaseError) {
//                    Log.i("ERROR!!", "Could not retreive your data for some unknown error!");
//                    Log.i("ERROR", firebaseError.getMessage());
//                }
//            });
//
//    return  dataObject;
//    }

}
