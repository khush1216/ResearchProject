package edu.uic.kdurge2.cs478.proj1_temp;

import android.app.ListActivity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import database.events.History;


//This activity maintains the history.
//retreives data from firebase and displays

public class HistoryActivity extends BaseActivity {

    ListView listView;
    DatabaseReference userDataRefObj;
    ArrayList<DataSnapshot> dataObject;

    private String activityDateTextV;
    private ArrayList<String> activityDateList;

    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);
        activityDateList = new ArrayList<String>();

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

                break;

            case R.id.help:
                break;
        }
        return super.onOptionsItemSelected(item);
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
           activityDateList.add(dataSnapshotArrayList.get(position).child("datetime").getValue().toString());

           //Log.i("ACTIVITY DATE LIST",activityDateList.toString());

           activity.setText("ACTIVITY TYPE:" + dataSnapshotArrayList.get(position).child("activity").getValue());
           distance.setText("DISTANCE :" + dataSnapshotArrayList.get(position).child("distance").getValue() + " kms");
           speed.setText("SPEED:" + dataSnapshotArrayList.get(position).child("speed").getValue() + " m/s");
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
