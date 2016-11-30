package uk.ac.lincoln.bwaterman.assignmentapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TwitchGames extends Activity {

    // array list to store tweet items from web service
    ArrayList<String> gameList = new ArrayList<>();
    ArrayList<String> channelViewersList = new ArrayList<>();
    ArrayList<String> imageUrlList = new ArrayList<>();
    ArrayList<String> textList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitch_games);

        //Start progress spinner while loading
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressGames);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        // start the  AsyncTask for calling the REST service using httpConnect class
        new AsyncTaskParseJson().execute();
    }

    // added asynctask class methods below -  you can make this class as a separate class file
    class AsyncTaskParseJson extends AsyncTask<String, String, String> {

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
        // this method is used for...................
        protected String doInBackground(String... arg0)  {

            try {
                // create new instance of the httpConnect class
                HttpConnect jParser = new HttpConnect();

                // get json string from service url
                String json = jParser.getJSONFromUrl(yourServiceUrl);

                //jsonTest = json.toString();

                // parse returned json string into json array
                JSONObject jsonObject = new JSONObject(json);

                JSONArray jsonArray = jsonObject.optJSONArray("top");

                // loop through json array and add each tweet to item in arrayList
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

                        //items.add(json_message.getJSONObject("game").getString("name"));
                        //items.add(json_message.getString("viewers"));
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
            //Gridview stuff
            GridView gridview = (GridView) findViewById(R.id.twitchList);
            gridview.setAdapter(new GridAdapter(TwitchGames.this, textList, imageUrlList, gameList, false));

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

