package edu.uic.kdurge2.cs478.proj1_temp;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.events.History;


//This activity maintains the history.
//retreives data from firebase and displays

public class HistoryActivity extends BaseActivity {

    ListView listView;
    DatabaseReference userDataRefObj;
    ArrayList<DataSnapshot> dataObject;

    private String activityDateTextV;
    private Map<Integer,String> activityDateList;

    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);
        activityDateList = new HashMap<Integer,String>();

        listView = (ListView) findViewById(R.id.listviewHist);
        getHistoryFromDB(getApplicationContext());
        registerForContextMenu(listView);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflateLayout = getMenuInflater();
        inflateLayout.inflate(R.menu.context_menu_history, menu);
    }

    public boolean onContextItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.deleteEntry:
                final AdapterView.AdapterContextMenuInfo infoM = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                activityDateTextV = activityDateList.get(infoM.position);

                DatabaseReference tempRef = UserProfile.mUserDatabaseRef.child("history");
                tempRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data: dataSnapshot.getChildren()) {

                            if(data.child("datetime").getValue().equals(activityDateTextV)){
                                data.getRef().removeValue();
                                dataObject.remove(infoM.position);
                                activityDateList.clear();
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    public void onCancelled(DatabaseError firebaseError) {
                        Log.i("ERROR!!", "Could not retreive your data for some unknown error!");
                        Log.i("ERROR", firebaseError.getMessage());
                    }
                });


                break;
            case R.id.viewData:
                Toast.makeText(this, "No Access! Request access from owner!", Toast.LENGTH_SHORT).show();

                break;

            case R.id.help:
                Toast.makeText(this, "View Documentation @ http://www.khushbu-durge.info", Toast.LENGTH_LONG).show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.history_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.signOutHist:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                startActivity(new Intent(HistoryActivity.this, SignInActivity.class));
                                finish();
                            }
                        });
                return true;
            case R.id.deleteHist:

                DatabaseReference deleteRef = UserProfile.mUserDatabaseRef.child("history");
                deleteRef.getRef().removeValue();
                activityDateList.clear();
                dataObject.clear();
                adapter.notifyDataSetChanged();

                Toast.makeText(this, "Your history is now deleted...", Toast.LENGTH_SHORT).show();

                return true;
            case R.id.help:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class MyAdapter extends ArrayAdapter {

       private Context context;
       private ArrayList<DataSnapshot> dataSnapshotArrayList;

       public MyAdapter(Context context, ArrayList<DataSnapshot> dataSnapshotArrayList) {

           super(context, R.layout.row_layout, dataSnapshotArrayList);

           this.context = context;
           this.dataSnapshotArrayList = dataSnapshotArrayList;
       }

       @Override
       public int getCount() {
           return dataSnapshotArrayList.size();
       }

       @Override
       public DataSnapshot getItem(int position) {
           return dataSnapshotArrayList.get(position);
       }

       @Override
       public long getItemId(int position) {
           return 0;
       }

       @Override
       public View getView(int position, View convertView, ViewGroup parent) {

           LayoutInflater inflater = (LayoutInflater) context
                   .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

           View rowView = inflater.inflate(R.layout.row_layout, parent, false);
           TextView activityDate = (TextView) rowView.findViewById(R.id.activityDate);
           TextView activity = (TextView) rowView.findViewById(R.id.activityDB);
           TextView distance = (TextView) rowView.findViewById(R.id.distanceDB);
           TextView speed = (TextView) rowView.findViewById(R.id.speedDB);
           TextView calorie = (TextView) rowView.findViewById(R.id.calorieDB);

           activityDate.setText("ACTIVITY DATE:" + dataSnapshotArrayList.get(position).child("datetime").getValue());
           activityDateList.put(position,dataSnapshotArrayList.get(position).child("datetime").getValue().toString());

           //Log.i("ACTIVITY DATE LIST",activityDateList.toString());

           activity.setText("ACTIVITY TYPE:" + dataSnapshotArrayList.get(position).child("activity").getValue());
           distance.setText("DISTANCE :" + dataSnapshotArrayList.get(position).child("distance").getValue() + " kms");
           speed.setText("SPEED:" + dataSnapshotArrayList.get(position).child("averageSpeed").getValue() + " m/s");
           calorie.setText("CALORIES LOST:" + dataSnapshotArrayList.get(position).child("calorieslost").getValue() + "cal");

           return rowView;
       }
   }


    private void getHistoryFromDB(Context contxt){

        final Context myContxt = contxt;
        userDataRefObj = UserProfile.mUserDatabaseRef.child("history");
        userDataRefObj.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataObject = new ArrayList<DataSnapshot>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    dataObject.add(data);
                }

                adapter = new MyAdapter(myContxt, dataObject);
                listView.setAdapter(adapter);

            }

            public void onCancelled(DatabaseError firebaseError) {
                Log.i("ERROR!!", "Could not retreive your data for some unknown error!");
                Log.i("ERROR", firebaseError.getMessage());
            }
        });

    }

}
