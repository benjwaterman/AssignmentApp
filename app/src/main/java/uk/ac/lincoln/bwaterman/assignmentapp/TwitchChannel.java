package uk.ac.lincoln.bwaterman.assignmentapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import static android.R.drawable.presence_invisible;
import static android.R.drawable.presence_online;

public class TwitchChannel extends AppCompatActivity {

    ImageLoader imageLoader;
    FileHelper fileHelper;

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
    ArrayList<String> videoUrlList = new ArrayList<>();
    ArrayList<String> videoThumbList = new ArrayList<>();
    ArrayList<String> videoLengthList = new ArrayList<>();

    //Database
    DatabaseHelper databaseHelper;
    boolean isFavourite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitch_channel);

        final TabHost tabHost = (TabHost)findViewById(R.id.channelTabs);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.infoTab);
        tabSpec.setIndicator("INFO");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.videosTab);
        tabSpec.setIndicator("VIDEOS");
        tabHost.addTab(tabSpec);

        //Set text colour of tabs to white
        for(int i=0;i<tabHost.getTabWidget().getChildCount();i++)
        {
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_dark));
        }

        //Set colour of currently selected tab
        tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        TextView tv = (TextView) tabHost.getCurrentTabView().findViewById(android.R.id.title);
        tv.setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_dark));

        //Add listener to change colour of tab when a different tab is selected
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {

                //Unselected tab
                for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
                    tabHost.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                    tv.setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_dark));
                }

                //Selected tab
                tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                TextView tv = (TextView) tabHost.getCurrentTabView().findViewById(android.R.id.title);
                tv.setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_dark));

            }
        });

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
            textView.setText(channelName);
        }
        if(channelFollowers != null && !Objects.equals(channelFollowers, "")) {
            //Subtitle/viewers
            TextView textView = (TextView) findViewById(R.id.channelSubtitleText);
            textView.setText(channelFollowers + " followers");
            textView = (TextView) findViewById(R.id.channelFollowers2);
            textView.setText(channelFollowers);
        }

        databaseHelper = new DatabaseHelper(this);
        //Setup imageloader
        if(imageLoader == null)
            imageLoader = ImageLoader.getInstance();
        fileHelper = new FileHelper(getApplicationContext());

        //Check if streamer is in favourites
        Cursor cursor = databaseHelper.getNameMatches(channelName);
        //Get switch
        Switch favouritesSwitch = (Switch) findViewById(R.id.favouriteSwitch);
        //If results are returned then streamer is in favourites
        if(cursor.getCount() > 0)
        {
            favouritesSwitch.setChecked(true);
            isFavourite = true;
        }
        //Not in favourites
        else
        {
            favouritesSwitch.setChecked(false);
            isFavourite = false;
        }

        cursor.close();

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

    void displayFavouriteImages() {
        //Assign logo
        {
            ImageView imageView = (ImageView) findViewById(R.id.channelLogo);
            //imageLoader.displayImage(logoUrl, imageView);
            fileHelper.displayImage(getApplicationContext(), ImageType.LOGO, logoUrl, channelName, imageView, null, isFavourite);
        }

        //Assign banner
        {
            ImageView imageView = (ImageView) findViewById(R.id.channelBanner);
            //imageLoader.displayImage(bannerUrl, imageView);
            fileHelper.displayImage(getApplicationContext(), ImageType.BANNER, bannerUrl, channelName, imageView, null, isFavourite);
        }

        //Complete progress spinner after done loading
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressChannel);
        progressBar.setVisibility(View.INVISIBLE);
    }

    void watchStream(View view) {
        Uri streamPage;
        try {
            streamPage = Uri.parse(channelUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW, streamPage);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Have to create toast in UI thread
    void createToast(String message ) {
        final String toastText = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Add to favourites asynchronously
    class AddFavourite extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                boolean isInserted = databaseHelper.insertData(channelName, channelUrl, "0", logoUrl);
                if(isInserted)
                    createToast("Favourite added!");
                else
                    createToast("An error occurred: favourite not added");

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
                if(result > 0) {
                    createToast("Favourite removed!");
                    fileHelper.deleteFavouriteImages(channelName);
                }
                else {
                    createToast("An error occurred: favourite not deleted");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    //Get json for channel
    class ParseJsonChannel extends AsyncTask<String, String, String> {

        // API url for channel
        String yourServiceUrl = "https://api.twitch.tv/kraken/channels/" + channelName;
        //Url for videos from channel
        String getVideosUrl = "https://api.twitch.tv/kraken/channels/" + channelName + "/videos?limit=10" + "&broadcasts=true";
        ProgressBar progressBar;

        @Override
        protected void onPreExecute() {
            //Get progress bar
            progressBar = (ProgressBar) findViewById(R.id.progressChannel);
            progressBar.setVisibility(View.VISIBLE);

            //Show images for favourites if they exist, this stops the delay of waiting until the json has been parsed
            if(isFavourite) {
                displayFavouriteImages();
            }
        }

        @Override
        protected String doInBackground(String... arg0)  {

            try {
                // create new instance of the httpConnect class
                HttpConnect jParser = new HttpConnect();

                // get json string from url
                String json = jParser.getJSONFromUrl(yourServiceUrl, getApplicationContext());
                // Get json for videos
                String jsonVideos = jParser.getJSONFromUrl(getVideosUrl, getApplicationContext());

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
                    //If json returns nothing, we're probably not connected to the internet
                    isConnected = false;
                }

                //If connected attempt to get json
                if(isConnected) {
                    //Check something has been returned
                    if(jsonVideos != null && jsonVideos.length() != 0) {
                        JSONObject jsonVideoObject = new JSONObject(jsonVideos);
                        //If there are videos to get
                        if (jsonVideoObject.length() != 0) {
                            JSONArray videoArray = jsonVideoObject.getJSONArray("videos");

                            //Loop through each video
                            for (int i = 0; i < videoArray.length(); i++) {
                                JSONObject json_message = videoArray.getJSONObject(i);
                                //Get video url and add it to list
                                videoUrlList.add(json_message.getString("url"));
                                videoThumbList.add(json_message.getString("preview"));
                                videoLengthList.add(json_message.getString("length"));
                            }
                        }
                    }

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
            textView.setText(channelFollowers);

            //Assign logo
            if(logoUrl != null && !Objects.equals(logoUrl, "")) {
                ImageView imageView = (ImageView) findViewById(R.id.channelLogo);
                fileHelper.displayImage(getApplicationContext(), ImageType.LOGO, logoUrl, channelName, imageView, null, isFavourite);
            }

            //Assign banner
            if(bannerUrl != null && !Objects.equals(bannerUrl, "") && !Objects.equals(bannerUrl, "null")) {
                ImageView imageView = (ImageView) findViewById(R.id.channelBanner);
                fileHelper.displayImage(getApplicationContext(), ImageType.BANNER, bannerUrl, channelName, imageView, null, isFavourite);
            }

            //Set if channel is mature
            textView = (TextView) findViewById(R.id.matureText);
            if(isMature) {
                textView.setText("True");
            }
            else {
                textView.setText("False");
            }

            //Assign game
            if(game != null && !Objects.equals(game, "")) {
                textView = (TextView) findViewById(R.id.gameText);
                textView.setText(game);
            }

            //Assign views
            if(views != null && !Objects.equals(views, "")) {
                textView = (TextView) findViewById(R.id.viewsText);
                textView.setText(views);
            }

            //
            LinearLayout layout = (LinearLayout)findViewById(R.id.videoThumbLayout);
            if(videoThumbList.size() != 0) {
                //Loop through list
                for(int i=0; i < videoThumbList.size(); i++)
                {
                    //Check theres something to display
                    if(videoThumbList.get(i) != null && !Objects.equals(videoThumbList.get(i), "")) {
                        View videoView;
                        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        videoView = inflater.inflate(R.layout.video_thumbnail, null);
                        textView = (TextView) videoView.findViewById(R.id.grid_text);
                        ImageView imageView = (ImageView) videoView.findViewById(R.id.grid_image);

                        int hours = Integer.parseInt(videoLengthList.get(i)) / 3600;
                        int minutes = (Integer.parseInt(videoLengthList.get(i)) % 3600) / 60;//Integer.parseInt(videoLengthList.get(i)) % 60;
                        int seconds = Integer.parseInt(videoLengthList.get(i)) % 60;

                        //Display length of videos in HH:MM::SS format
                        textView.setText(String.format("%d:%02d:%02d", hours, minutes, seconds));
                        imageLoader.displayImage(videoThumbList.get(i), imageView);

                        //On an on click listener to each picture so if its clicked it goes to that video
                        final String url = videoUrlList.get(i);
                        imageView.setOnClickListener(new ImageView.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri streamPage = Uri.parse(url);
                                Intent intent = new Intent(Intent.ACTION_VIEW, streamPage);
                                startActivity(intent);
                            }
                        });

                        layout.addView(videoView);
                    }
                }
            }

            //Complete progress spinner after done loading
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
