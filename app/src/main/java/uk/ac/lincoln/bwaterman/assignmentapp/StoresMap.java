package uk.ac.lincoln.bwaterman.assignmentapp;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class StoresMap extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    ArrayList<String> storeList = new ArrayList<>();
    ArrayList<LatLng> latLongList = new ArrayList<>();

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Double lat;
    private Double longi;
    private boolean isConnected = false;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stores_map);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                lat = mLastLocation.getLatitude();
                longi = mLastLocation.getLongitude();

                isConnected = true;

                //Initialise mapFragment
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);

                //Start json parsing to get nearby stores
                new AsyncTaskParseJson().execute();
            }
        } catch(SecurityException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, longi))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .title("You"));

        LatLng latLng = new LatLng(lat, longi);
        CameraPosition cameraPosition = new CameraPosition(latLng, 13, 0, 0);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult result) {}

    // added asynctask class methods below -  you can make this class as a separate class file
    class AsyncTaskParseJson extends AsyncTask<String, String, String> {

        // set the url of the web service to call
        String yourServiceUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + longi + "&radius=5000&types=electronics_store&name=game&key=AIzaSyD3Qm1ACJ-qpOdDEX19jwXj1pNmcTQ_s10";

        @Override
        // this method is used for......................
        protected void onPreExecute() {}

        @Override
        // this method is used for...................
        protected String doInBackground(String... arg0)  {

            try {
                // create new instance of the httpConnect class
                HttpConnect jParser = new HttpConnect();

                // get json string from service url
                String json = jParser.getJSONFromUrl(yourServiceUrl);

                // parse returned json string into json array
                JSONObject jsonObject = new JSONObject(json);

                JSONArray jsonArray = jsonObject.optJSONArray("results");

                // loop through json array and add each tweet to item in arrayList
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonSubObject = jsonArray.getJSONObject(i);

                    if (jsonSubObject != null) {
                        //add each tweet to ArrayList as an item
                        String nameToAdd;
                        String latToAdd;
                        String longiToAdd;

                        try {
                            nameToAdd = jsonSubObject.getString("name");
                            storeList.add(nameToAdd);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        try {
                            latToAdd = jsonSubObject.getJSONObject("geometry").getJSONObject("location").getString("lat");
                            longiToAdd = jsonSubObject.getJSONObject("geometry").getJSONObject("location").getString("lng");
                            latLongList.add(new LatLng(Double.parseDouble(latToAdd), Double.parseDouble(longiToAdd)));
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {//(JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        // below method will run when service HTTP request is complete, will then bind tweet text in arrayList to ListView
        protected void onPostExecute(String strFromDoInBg) {
            if(map != null) {
                for(int i = 0; i < storeList.size(); i++) {
                    map.addMarker(new MarkerOptions()
                            .position(latLongList.get(i))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .title(storeList.get(i)));
                }
            }

        }
    }
}
