package edu.uic.kdurge2.cs478.proj1_temp;

import android.*;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.meapsoft.SpeedCalculator;

import org.json.JSONObject;
import org.w3c.dom.Text;
import java.util.Calendar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import database.events.History;

    public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener

{

    public static LatLng origin,destination;

    private Location lastLocation;
    Date currentTime;

    private Button start,stop;
    FetchURL fetchurl;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int WRITE_PERMISSION_REQ = 1118;
    private Intent mServiceIntent;
    private Intent mLocationServ;

    private TextView txtDist,txtSpeed,txtActivity,txtCalorie;
    private Handler uiLocHandler;
    private BroadcastReceiver bReceiver;
    private double distanceFromLocService;
    private String activityPredicted;


    private History histObj;
    private ArrayList<History> historyObjList;
    private String latestActivity;
    private double distance;
    private long time1, time2;
    private ArrayList<String> activityList;
    private ArrayList<String> storeActivityList;

    SupportMapFragment mapFragment;
    long timeAtOrigin, timeAtDestForSpeed;
    double speedNew;

    Thread threadGetLocUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        start = (Button) findViewById(R.id.startPred);
        stop = (Button) findViewById(R.id.stopBtn);
        txtDist = (TextView) findViewById(R.id.distance);
        txtSpeed = (TextView) findViewById(R.id.speed);
        txtActivity = (TextView) findViewById(R.id.activityUpdate);

        mServiceIntent = new Intent(this,ServiceSensor.class);
        mLocationServ = new Intent(this, MyLocationService.class);
        activityList = new ArrayList<String>();
        storeActivityList = new ArrayList<String>();
        //uiLocHandler = new Handler(Looper.getMainLooper());

        speedNew = 0;
        latestActivity = "";

        historyObjList = new ArrayList<History>();

        bReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("distance")) {
                    distanceFromLocService = intent.getDoubleExtra("distance", 0);
                    lastLocation = intent.getParcelableExtra("lastLocation");
                    timeAtDestForSpeed = System.currentTimeMillis();

                    SpeedCalculator speedC = new SpeedCalculator();
                    double dis = speedC.getDistanceFromLatLonInKm(origin.latitude,origin.longitude,lastLocation.getLatitude(),lastLocation.getLongitude());
                    speedNew = speedC.speedCal(dis,timeAtOrigin,timeAtDestForSpeed);

                    updateMapDist();
                }
                else if (intent.getAction().equals("activity")){
                    activityPredicted = intent.getStringExtra("activity");
                    if(activityList.size() == 5){
                        updateMapActivity();
                        activityList.clear();
                        activityList = new ArrayList<String>();
                    }
                    else{
                        activityList.add(activityPredicted);

                    }
                }

            }
        };


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    private void getSpeedBetweenTwoPoints(){


    }

    private void updateMapActivity(){
        String updatedAct = getMostPredictedAct(activityList);
        if(! latestActivity.equals(updatedAct)) {
            if(latestActivity.equals("")){
                txtActivity.setText(updatedAct);
                latestActivity = updatedAct;
            }
            else {
                time2 = System.currentTimeMillis();
                long duration = time2 - time1;
                currentTime = Calendar.getInstance().getTime();
                histObj = new History(latestActivity,time1, currentTime.toString(),duration, 10,10,10);
                historyObjList.add(histObj);
                time1 = System.currentTimeMillis();
                txtActivity.setText(updatedAct);
                latestActivity = updatedAct;
            }
        }
        else {
            txtActivity.setText(updatedAct);
        }
        txtSpeed.setText(Double.toString(speedNew) + "m/s");
    }
    private void updateMapDist(){
        txtDist.setText(Double.toString(distanceFromLocService) + "km");
    }


    private String getMostPredictedAct(ArrayList<String> actList){

        HashMap<String,Integer> activMap = new HashMap<String, Integer>();

        for(String x : actList){
            if(activMap.containsKey(x)){
                int val = activMap.get(x);
                val = val+1;
                activMap.put(x,val);
            }
            else {
                activMap.put(x,1);
            }
        }
        Map.Entry<String, Integer> max = null;
        for (Map.Entry<String, Integer> e : activMap.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max.getKey();

    }

    public void onStartPredClick(View view){

        timeAtOrigin = System.currentTimeMillis();
        time1 = System.currentTimeMillis();
        txtActivity.setText("");
        txtDist.setText("");
        txtSpeed.setText("");
        startService(mServiceIntent);
        threadGetLocUpdates = new Thread() {
            @Override
            public void run() {

                startService(mLocationServ);
            }
        };
        threadGetLocUpdates.start();

    }

    public void onStopPredClick(View view){

        History setDb = new History();
        setDb.enterHistoryToDataBase(historyObjList);

        LatLng userDest = null;
        if(lastLocation != null){
            Toast.makeText(this, " Your history will be saved!", Toast.LENGTH_LONG).show();
             userDest = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());}
        if(userDest != null) {
            Log.i("TAG","I'm in on Stop seeting userDest !!@@@!!!");
            mMap.addMarker(new MarkerOptions().position(userDest).title("Destination"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(userDest));
            String url = getMapsApiDirectionsUrl(origin, userDest);
            fetchurl = new FetchURL();
            fetchurl.execute(url);

        }
        else{
            Toast.makeText(this, "You haven't moved :/", Toast.LENGTH_LONG).show();
        }
        if(fetchurl != null){
            fetchurl.cancel(true);}
            else{
            Toast.makeText(this, " Not enough time to detect activity! Start again.", Toast.LENGTH_LONG).show();

        }

        latestActivity = "";
        stopService(mLocationServ);
        stopService(mServiceIntent);



    }

    private String  getMapsApiDirectionsUrl(LatLng origin,LatLng dest) {
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters + "&key=AIzaSyBImmFsOShIj1-0xVfy99bQRzSrTs5I90c";


        return url;

    }

    private class FetchURL extends AsyncTask<String,Void,String> {


        @Override
        protected String doInBackground(String... url) {
                String data = "";

                try{
                    data = downloadURL(url[0]);
                }
                catch (Exception e){
                    Log.i(Variables_Globals.TAG,e.toString());
                }
                return data;
            }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String,String>>> doInBackground(String... jsonData){
            JSONObject jsonObject;
            List<List<HashMap<String,String>>> routes = null;

            try{
                jsonObject = new JSONObject(jsonData[0]);
                DataParser parser = new DataParser();

                routes = parser.parse(jsonObject);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String,String>>> result){
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String,String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }


    }

    private String downloadURL(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null){
                sb.append(line);

            }
            data = sb.toString();

            br.close();
        }

        catch (Exception e){

        }
        finally {
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        if(origin == null) {
            mMap = googleMap;
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            //Initialize Google Play Services
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleApiClient();
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        if(destination != null){
            String url = getMapsApiDirectionsUrl(origin,destination);
            FetchURL fetchurl = new FetchURL();
            fetchurl.execute(url);
            fetchurl.cancel(true);
        }

        else if (destination == null){
            Toast.makeText(this, "Locating current location...", Toast.LENGTH_LONG).show();
        }
    }



    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case WRITE_PERMISSION_REQ: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("permission", "granted!!!");
                } else {
                    Toast.makeText(MapsActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
            }

            // other 'case' lines to check for other permissions this app might request.
            //You can add here other case statements according to your requirement.
        }
    }
    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        origin = latLng;
        //timeAtOrigin = System.currentTimeMillis();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }



    public void onMapSearch(View view) {
        EditText locationSearch = (EditText) findViewById(R.id.editText);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            destination = latLng;
            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapsActivity.this);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERMISSION_REQ);

        }

        IntentFilter intentFilter =    new IntentFilter();
        intentFilter.addAction("distance");
        intentFilter.addAction("activity");
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, intentFilter);

    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
