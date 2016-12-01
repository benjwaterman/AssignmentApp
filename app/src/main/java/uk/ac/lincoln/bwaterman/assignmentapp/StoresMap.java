package uk.ac.lincoln.bwaterman.assignmentapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

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
    boolean isConnected;
    boolean hasLocation;
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
            //Has got a location
            if (mLastLocation != null) {
                hasLocation = true;

                lat = mLastLocation.getLatitude();
                longi = mLastLocation.getLongitude();

                //Initialise mapFragment
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);

                //Start json parsing to get nearby stores
                new AsyncTaskParseJson().execute();
            }
            //Has not got a location
            else {
                hasLocation = false;
                onNoConnection();
            }
        } catch(SecurityException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (hasLocation) {
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, longi))
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("twitch_logo_icon_white", 30, 30)))
                    .title("You"));

            LatLng latLng = new LatLng(lat, longi);
            CameraPosition cameraPosition = new CameraPosition(latLng, 13, 0, 0);
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        hasLocation = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        hasLocation = false;
    }

    // added asynctask class methods below -  you can make this class as a separate class file
    class AsyncTaskParseJson extends AsyncTask<String, String, String> {
        // set the url of the web service to call
        String yourServiceUrl;

        @Override
        // this method is used for......................
        protected void onPreExecute() {
            if(!hasLocation) {
                return;
            }
            yourServiceUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + longi + "&radius=5000&types=electronics_store&name=game&key=AIzaSyD3Qm1ACJ-qpOdDEX19jwXj1pNmcTQ_s10";
        }

        @Override
        // this method is used for...................
        protected String doInBackground(String... arg0)  {
            try {
                //If there was no location, there is no json to parse
                if(!hasLocation) {
                    return null;
                }

                HttpConnect jParser = new HttpConnect();

                String json = jParser.getJSONFromUrl(yourServiceUrl, getApplicationContext());

                //Json returned nothing, exit out function
                if(json == null || json.length() == 0) {
                    isConnected = false;
                    return null;
                }
                JSONObject jsonObject = new JSONObject(json);

                JSONArray jsonArray = jsonObject.optJSONArray("results");

                //
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
        // Add markers to locations of stores
        protected void onPostExecute(String strFromDoInBg) {
            if(!isConnected || !hasLocation) {
                onNoConnection();
            }

            if(map != null) {
                for(int i = 0; i < storeList.size(); i++) {
                    map.addMarker(new MarkerOptions()
                            .position(latLongList.get(i))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                            .title(storeList.get(i)));
                }
            }

        }
    }

    //Resize the icon before its used as a map marker
    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth()/width, imageBitmap.getHeight()/height, false);
        return resizedBitmap;
    }

    void onNoConnection() {
        TextView text = (TextView)findViewById(R.id.mapErrorText);
        //If have connection but no location
        if(!hasLocation) {
            text.setText("No location could be found");
        }
        text.setVisibility(View.VISIBLE);
        text = (TextView) findViewById(R.id.helpText);
        //Set help text invisible if error text is showing
        text.setVisibility(View.INVISIBLE);
    }

}
