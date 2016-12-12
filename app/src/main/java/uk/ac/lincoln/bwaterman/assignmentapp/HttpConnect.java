package uk.ac.lincoln.bwaterman.assignmentapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpConnect {
    // the below line is for making debugging easier
    final String TAG = "JsonParser.java";
    // where the returned json data from service will be stored when downloaded
    static String json = "";
    //store context
    Context context;
    //store whether data warning has already been displayed
    boolean hasWarned = false;

    // your android activity will call this method and pass in the url of the REST service
    public String getJSONFromUrl(String url, Context _context) {
        context = _context;

        //If not connected
        if(!isConnected()) {
            //Create toast for user alerting them
            createToast("No internet connection found!");
            //Exit it out of function, no point trying to connect with no connection
            return null;
        }

        try {
            // this code block represents/configures a connection to your REST service
            // it also represents an HTTP 'GET' request to get data from the REST service, not POST!
            URL u = new URL(url);
            HttpURLConnection restConnection = (HttpURLConnection) u.openConnection();
            restConnection.setRequestMethod("GET");
            restConnection.setRequestProperty("Content-length", "0");
            restConnection.setUseCaches(false);
            restConnection.setAllowUserInteraction(false);
            restConnection.setConnectTimeout(10000);
            restConnection.setReadTimeout(10000);
            restConnection.connect();
            int status = restConnection.getResponseCode();

            // switch statement to catch HTTP 200 and 201 errors
            switch (status) {
                case 200:
                case 201:
                    // live connection to your REST service is established here using getInputStream() method
                    BufferedReader br = new BufferedReader(new InputStreamReader(restConnection.getInputStream()));

                    // create a new string builder to store json data returned from the REST service
                    StringBuilder sb = new StringBuilder();
                    String line;

                    // loop through returned data line by line and append to stringbuilder 'sb' variable
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();

                    // remember, you are storing the json as a stringy
                    try {
                        json = sb.toString();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing data " + e.toString());
                    }
                    // return JSON String containing data to Tweet activity (or whatever your activity is called!)
                    return json;
            }
            // HTTP 200 and 201 error handling from switch statement
        } catch (MalformedURLException ex) {
            Log.e(TAG, "Malformed URL ");
        } catch (IOException ex) {
            Log.e(TAG, "IO Exception ");
        }

        return null;
    }

    //Function to check if there is an internet connection
    private boolean isConnected() {
        //Test for connection
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean connected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        //Check type of connection
        if(connected) {
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            if (isWiFi) {
                //Wifi connected, this is fine
            } else {
                if(!hasWarned) {
                    createToast("Be careful, watching streams on a non-WiFi connection will use a lot of data!");
                    //so we don't get a warning on every activity
                    hasWarned = true;
                }
            }
        }
        return connected;
    }

    //Have to create toast in UI thread
    void createToast(String message ) {
        final String toastText = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
            }
        });
    }
}