package uk.ac.lincoln.bwaterman.assignmentapp;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.util.Objects;

import static android.R.drawable.presence_invisible;
import static android.R.drawable.presence_online;

public class TwitchChannel extends AppCompatActivity {

    String channelName;
    String channelFollowers;
    boolean isConnected;

    //Variables from json
    boolean isMature;
    boolean liveStatus;
    String game;
    String logoUrl;
    String bannerUrl;
    String channelUrl;
    String views;

    //Database
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitch_channel);

        TabHost tabHost = (TabHost)findViewById(R.id.channelTabs);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.infoTab);
        tabSpec.setIndicator("INFO");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.videosTab);
        tabSpec.setIndicator("VIDEOS");
        tabHost.addTab(tabSpec);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            channelName = extras.getString("channelName");
            channelFollowers = extras.getString("channelFollowers");
        }
        if(channelName != null && !Objects.equals(channelName, "")) {
            //Page title
            TextView textView = (TextView) findViewById(R.id.channelTitleText);
            textView.setText(channelName);
            //Channel name
            textView = (TextView) findViewById(R.id.channelName);
            textView.setText(channelName);
            textView = (TextView) findViewById(R.id.channelName2);
            textView.setText("Name: " + channelName);
        }
        if(channelFollowers != null && !Objects.equals(channelFollowers, "")) {
            //Subtitle/viewers
            TextView textView = (TextView) findViewById(R.id.channelSubtitleText);
            textView.setText(channelFollowers + " followers");
            textView = (TextView) findViewById(R.id.channelFollowers2);
            textView.setText("Followers: " + channelFollowers);
        }

        databaseHelper = new DatabaseHelper(this);

        //Check if streamer is in favourites
        Cursor cursor = databaseHelper.getNameMatches(channelName);
        //Get switch
        Switch favouritesSwitch = (Switch) findViewById(R.id.favouriteSwitch);
        //If results are returned then streamer is in favourites
        if(cursor.getCount() > 0)
        {
            favouritesSwitch.setChecked(true);
        }
        //Not in favourites
        else
        {
            favouritesSwitch.setChecked(false);
        }

        //Set listener if switch is checked or unchecked
        favouritesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Is switched on, add to favourites
                if(isChecked) {
                    //Uses async task as it can be a bit resource heavy on main thread
                    new AddFavourite().execute();
                }
                //Is switched off, remove from favourites
                else {
                    new RemoveFavourite().execute();
                }
            }
        });

        //call async task to parse json
        new ParseJsonChannel().execute();
    }

    //Add to favourites asynchronously
    class AddFavourite extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                boolean isInserted = databaseHelper.insertData(channelName, channelUrl, "0");
                if(isInserted)
                    Toast.makeText(TwitchChannel.this,"Favourite added!",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(TwitchChannel.this,"An error occurred: favourite not added",Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    //Remove from favourites asynchronously
    class RemoveFavourite extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                int result = databaseHelper.deleteData(channelName);
                if(result > 0)
                    Toast.makeText(TwitchChannel.this,"Favourite removed!",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(TwitchChannel.this,"An error occurred: favourite not deleted",Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class ParseJsonChannel extends AsyncTask<String, String, String> {

        // API url for channel
        String yourServiceUrl = "https://api.twitch.tv/kraken/channels/" + channelName;
        ProgressBar progressBar;

        //Setup imageloader
        ImageLoader imageLoader = ImageLoader.getInstance();

        @Override
        protected void onPreExecute() {
            //Get progress bar
            progressBar = (ProgressBar) findViewById(R.id.progressChannel);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... arg0)  {

            try {
                // create new instance of the httpConnect class
                HttpConnect jParser = new HttpConnect();

                // get json string from url
                String json = jParser.getJSONFromUrl(yourServiceUrl, getApplicationContext());

                //If no data was returned or string is null, set connection to false and return out of function
                if(json == null || json.length() == 0) {
                    isConnected = false;
                    return null;
                }
                isConnected = true;

                // parse returned json into json object
                JSONObject jsonObject = new JSONObject(json);

                //If json returned something
                if (jsonObject.length() != 0) {
                    //Get whether channel is mature or not, parsing string to boolean
                    isMature = Boolean.parseBoolean(jsonObject.getString("mature"));
                    game = jsonObject.getString("game");
                    logoUrl = jsonObject.getString("logo");
                    bannerUrl = jsonObject.getString("profile_banner");
                    channelUrl = jsonObject.getString("url");
                    views = jsonObject.getString("views");
                    channelFollowers = jsonObject.getString("followers");
                }
                else {
                    isConnected = false;
                }

                //If connected check whether channel is currently live
                if(isConnected) {
                    //Get whether channel is currently live
                    String liveCheckUrl = "https://api.twitch.tv/kraken/streams/" + channelName;
                    json = jParser.getJSONFromUrl(liveCheckUrl, getApplicationContext());
                    jsonObject = new JSONObject(json);

                    if(Objects.equals(jsonObject.getString("stream"), "null")) {
                        //Channel is offline
                        liveStatus = false;
                    }
                    else {
                        //Channel is online
                        liveStatus = true;
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {
            //If not connected, set not connected text to visible and return out of function
            if(!isConnected) {
                TextView text = (TextView)findViewById(R.id.noConnectionText);
                text.setVisibility(View.VISIBLE);
                return;
            }

            //Assign live status
            TextView textView = (TextView)findViewById(R.id.statusText);
            if(liveStatus) {
                textView.setText("LIVE");
                ImageView imageView = (ImageView)findViewById(R.id.statusImage);
                imageView.setImageResource(presence_online);
            }
            else {
                textView.setText("OFFLINE");
                ImageView imageView = (ImageView)findViewById(R.id.statusImage);
                imageView.setImageResource(presence_invisible);
            }

            //Assign followers
            textView = (TextView)findViewById(R.id.channelSubtitleText);
            textView.setText(channelFollowers + " followers");
            textView = (TextView) findViewById(R.id.channelFollowers2);
            textView.setText("Followers: " + channelFollowers);

            //Assign logo
            if(logoUrl != null && !Objects.equals(logoUrl, "")) {
                ImageView imageView = (ImageView) findViewById(R.id.channelLogo);
                imageLoader.displayImage(logoUrl, imageView);
            }

            //Assign banner
            if(bannerUrl != null && !Objects.equals(bannerUrl, "")) {
                ImageView imageView = (ImageView) findViewById(R.id.channelBanner);
                imageLoader.displayImage(bannerUrl, imageView);
            }

            //Set if channel is mature
            textView = (TextView) findViewById(R.id.matureText);
            if(isMature) {
                textView.setText("Mature: True");
            }
            else {
                textView.setText("Mature: False");
            }

            //Assign game
            if(game != null && !Objects.equals(game, "")) {
                textView = (TextView) findViewById(R.id.gameText);
                textView.setText("Last game played: " + game);
            }

            //Assign views
            if(views != null && !Objects.equals(views, "")) {
                textView = (TextView) findViewById(R.id.viewsText);
                textView.setText("Total views: " + views);
            }

            //GET VIDEO IMAGE URLS AND ADD IMAGEVIEW TO LINEAR LAYOUT UNDER HORIZ SCROLL VIEW

            //Complete progress spinner after done loading
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
