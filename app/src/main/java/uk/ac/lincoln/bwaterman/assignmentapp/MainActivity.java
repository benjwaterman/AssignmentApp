package uk.ac.lincoln.bwaterman.assignmentapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    DatabaseHelper databaseHelper;
    ArrayList<String> favouritesNamesList = new ArrayList<>();
    ArrayList<String> favouritesViewsList = new ArrayList<>();
    ArrayList<String> favouritesLogoList = new ArrayList<>();
    FileHelper fileHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        databaseHelper = new DatabaseHelper(this);
        fileHelper = new FileHelper(getApplicationContext());

        GridView gridView = (GridView) findViewById(R.id.favouritesGridView);
        //Register for context menu
        registerForContextMenu(gridView);
    }

    protected void onStart() {
        updateImageDataSize();
        updateFavouritesList();
        super.onStart();
    }

    public void startTwitch(View view) {
        Intent intent = new Intent(this, TwitchGames.class);
        startActivity(intent);
    }

    public void findStores(View view) {
        Intent intent = new Intent(this, StoresMap.class);
        startActivity(intent);
    }

    //Clear images function
    public void clearImageData(View view) {

        //Delete files
        try {
            boolean success1, success2;
            success1 = fileHelper.deleteFile(new File(getFilesDir(), "streamers"));
            success2 = fileHelper.deleteFile(new File(getFilesDir(), "games"));

            if(success1 && success2) {
                Toast.makeText(getApplicationContext(), "Image data deleted!", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        updateImageDataSize();
    }

    //Updates text with image data size
    void updateImageDataSize() {
        //Update space used text
        TextView spaceUsedTV = (TextView) findViewById(R.id.spaceUsedText);
        //Get size of folders
        Float size = fileHelper.getImageDataSize(new File(getFilesDir(), "streamers")) + fileHelper.getImageDataSize(new File(getFilesDir(), "games"));
        //Convert to MB and format to string
        String spaceUsed = String.format(Locale.ENGLISH, "%.2f", size / 1000000);
        spaceUsedTV.setText(spaceUsed + " MB used");
    }

    void showFavourites() {
        GridView gridView = (GridView) findViewById(R.id.favouritesGridView);

        //If there are no favourites to display
        if(favouritesNamesList.size() == 0) {
            ArrayList<String> message  = new ArrayList<>();
            message.add("No favourites to display");
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, message);
            gridView.setAdapter(arrayAdapter);
        }
        else {
            gridView.setAdapter(new GridAdapter(MainActivity.this, favouritesNamesList, favouritesLogoList, favouritesNamesList, false, true));
        }

    }

    void updateFavouritesList() {
        final Cursor res = databaseHelper.getAllData();
        favouritesNamesList.clear();
        favouritesViewsList.clear();
        favouritesLogoList.clear();
        //If no favourites have been added, do nothing
        if(res.getCount() == 0) {
            // show message
            showFavourites();
            favouritesNamesList.clear();
            favouritesViewsList.clear();
            favouritesLogoList.clear();
            return;
        }

        while (res.moveToNext()) {
            //Get favourites names from database and add them to the list
            favouritesNamesList.add(res.getString(1));
            //Get times viewed
            favouritesViewsList.add(res.getString(3));

            favouritesLogoList.add("https://pbs.twimg.com/profile_images/616076655547682816/6gMRtQyY.jpg");
        }

        showFavourites();

        //Open the link to the stream on click
        GridView gridView = (GridView) findViewById(R.id.favouritesGridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                res.moveToPosition(position);
                String url = res.getString(2);
                //Update times viewed
                databaseHelper.updateTimesViewed(res);
                Uri streamPage;
                try {
                    streamPage = Uri.parse(url);
                    //Intent intent = new Intent(Intent.ACTION_VIEW, streamPage);/
                    Intent intent = new Intent(MainActivity.this, TwitchChannel.class);
                    intent.putExtra("channelName", favouritesNamesList.get(position));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                res.close();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //If correct list view AND there is at least 1 favourite added, then create context menu
        if (v.getId() == R.id.favouritesGridView && favouritesNamesList.size() > 0) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(favouritesNamesList.get(info.position));
            MenuInflater inflater = getMenuInflater();

            //Always going to be in our favourites
            inflater.inflate(R.menu.menu_favourites_context, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Cursor res = databaseHelper.getAllData();
        switch (item.getItemId()) {
            //Open stream
            case R.id.view:
                //Move cursor to position to this streamer
                res.moveToPosition(info.position);
                //Get url from 3rd column in database
                String url = res.getString(2);
                //Add to times viewed
                databaseHelper.updateTimesViewed(res);
                Uri streamPage;
                try {
                    streamPage = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, streamPage);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                res.close();
                return true;

            case R.id.remove:
                try {
                    int result = databaseHelper.deleteData(favouritesNamesList.get(info.position));
                    if(result > 0) {
                        Toast.makeText(this, "Favourite removed!", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(this,"An error occurred: favourite not deleted",Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                updateFavouritesList();
                res.close();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }
}

