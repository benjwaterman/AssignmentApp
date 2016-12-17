package uk.ac.lincoln.bwaterman.assignmentapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TwitchGames extends Activity {

    // array list to store tweet items from web service
    ArrayList<String> gameList = new ArrayList<>();
    ArrayList<String> channelViewersList = new ArrayList<>();
    ArrayList<String> imageUrlList = new ArrayList<>();
    ArrayList<String> textList = new ArrayList<>();
    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitch_games);

        //Start progress spinner while loading
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressGames);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        //
        new ParseJsonGames().execute();
    }

    class ParseJsonGames extends AsyncTask<String, String, String> {

        // set the url of the web service to call
        String yourServiceUrl = "https://api.twitch.tv/kraken/games/top?limit=20";
        ProgressBar progressBar;

        @Override
        // this method is used for......................
        protected void onPreExecute() {
            //Get progress bar
            progressBar = (ProgressBar) findViewById(R.id.progressGames);
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

                // parse returned json string into json array
                JSONObject jsonObject = new JSONObject(json);

                JSONArray jsonArray = jsonObject.optJSONArray("top");

                // add data to relevant variables
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json_message = jsonArray.getJSONObject(i);

                    if (json_message != null) {
                        //add each tweet to ArrayList as an item
                        String nameToAdd;
                        String viewersToAdd;
                        String imageUrlToAdd;
                        try {
                            nameToAdd = json_message.getJSONObject("game").getString("name");// + " currently has " + json_message.getString("viewers") + " viewers.";
                            gameList.add(nameToAdd);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        try {
                            viewersToAdd = json_message.getString("viewers");
                            channelViewersList.add(viewersToAdd);
                            //Add the name of the game and how many viewers to textList
                            textList.add("<b>" + gameList.get(i) + "</b><br>" + viewersToAdd + " viewers");
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        try {
                            imageUrlToAdd = json_message.getJSONObject("game").getJSONObject("box").getString("medium");
                            imageUrlList.add(imageUrlToAdd);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
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

            //Gridview stuff
            GridView gridview = (GridView) findViewById(R.id.twitchList);
            gridview.setAdapter(new GridAdapter(TwitchGames.this, textList, imageUrlList, gameList, ImageType.GAME, false));

            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    //Toast.makeText(Twitch.this, "" + position,
                    //Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(TwitchGames.this, TwitchStreams.class);
                    //pass the name of the game to the new intent
                    intent.putExtra("gameName", gameList.get(position));
                    intent.putExtra("gameViewers", channelViewersList.get(position));
                    startActivity(intent);
                }
            });

            //Complete progress spinner after done loading
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}

