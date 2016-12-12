package uk.ac.lincoln.bwaterman.assignmentapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;

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
        }
        if(channelFollowers != null && !Objects.equals(channelFollowers, "")) {
            //Subtitle/viewers
            TextView textView = (TextView) findViewById(R.id.channelSubtitleText);
            textView.setText(channelFollowers + " followers");
        }

        //call async task to parse json
        new ParseJsonChannel().execute();
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
            textView = (TextView)findViewById(R.id.channelSubtitleText);
            textView.setText(channelFollowers + " followers");

            if(logoUrl != null && !Objects.equals(logoUrl, "")) {
                ImageView imageView = (ImageView) findViewById(R.id.channelLogo);
                imageLoader.displayImage(logoUrl, imageView);
            }

            if(bannerUrl != null && !Objects.equals(bannerUrl, "")) {
                ImageView imageView = (ImageView) findViewById(R.id.channelBanner);
                imageLoader.displayImage(bannerUrl, imageView);
            }

            //Complete progress spinner after done loading
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
