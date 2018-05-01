package com.meapsoft;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by Khushbu on 4/28/2018.
 */

public class OsrmRequests {

    public void sendRequestToOSRM(double lat1, double lon1, double lat2, double lon2) {

        String url = "http://router.project-osrm.org/route/v1/driving/" + Double.toString(lon1) + "," + Double.toString(lat1) + ";" + Double.toString(lon2) + "," + Double.toString(lat2) + "?exclude=motorway";
        sendRequest(url);

    }

    public void sendRequest(String url) {

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject myResponse = new JSONObject(response.toString());
            String x = myResponse.getString("routes");

            Log.i("",myResponse.getString("routes") );

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
