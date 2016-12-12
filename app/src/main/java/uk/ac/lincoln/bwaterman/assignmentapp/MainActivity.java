package uk.ac.lincoln.bwaterman.assignmentapp;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    DatabaseHelper databaseHelper;
    ArrayList<String> favouritesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        databaseHelper = new DatabaseHelper(this);

        ListView listView = (ListView) findViewById(R.id.favouritesListView);
        //Register for context menu
        registerForContextMenu(listView);
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
        Context context = getApplicationContext();
        //Check savedImages.txt exists, if not exit function
        File file = new File(context.getFilesDir(), "savedImages.txt");
        if (!file.exists()) {
            CharSequence text = "No image data to delete!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        //Delete files
        try {
            FileInputStream input = openFileInput("savedImages.txt");
            InputStreamReader streamReader = new InputStreamReader(input);
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            String readString = bufferedReader.readLine();
            //While there is text being read in, aka not end of file
            while (readString != null) {
                //Check file exists, if it does delete it
                file = new File(context.getFilesDir(), readString);
                if(file.exists()) {
                    context.deleteFile(readString);
                    readString = bufferedReader.readLine() ;
                }
                else {
                    readString = bufferedReader.readLine() ;
                }
            }

            //Close input streams
            input.close();
            streamReader.close();
            bufferedReader.close();

            //After all images have been deleted, delete savedImages.txt as it will be regenerated when the next images are loaded
            context.deleteFile("savedImages.txt");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        CharSequence text = "Image data deleted!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        updateImageDataSize();
    }

    void updateImageDataSize() {
        //Update space used text
        TextView spaceUsedTV = (TextView) findViewById(R.id.spaceUsedText);
        String spaceUsed = getImageDataSize();
        spaceUsedTV.setText(spaceUsed + " MB used");
    }

    String getImageDataSize() {
        Context context = getApplicationContext();
        float bytes = 0;
        //Check savedImages.txt exists, if not exit function returning 0 bytes used
        File file = new File(context.getFilesDir(), "savedImages.txt");
        if (!file.exists()) {
            return "0";
        }

        //Delete files
        try {
            FileInputStream input = openFileInput("savedImages.txt");
            InputStreamReader streamReader = new InputStreamReader(input);
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            String readString = bufferedReader.readLine();
            //While there is text being read in, aka not end of file
            while (readString != null) {
                //Check file exists, if it does delete it
                file = new File(context.getFilesDir(), readString);
                if (file.exists()) {
                    bytes += file.length();
                    readString = bufferedReader.readLine();
                } else {
                    readString = bufferedReader.readLine();
                }
            }

            //Close input streams
            input.close();
            streamReader.close();
            bufferedReader.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        //Convert to MB
        return String.format(Locale.ENGLISH, "%.2f", bytes / 1000000);
    }

    void showFavourites(ArrayList<String> Message) {
        ListView listView = (ListView) findViewById(R.id.favouritesListView);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, Message);
        listView.setAdapter(arrayAdapter);
    }

    void updateFavouritesList() {
        final Cursor res = databaseHelper.getAllData();
        ArrayList<String> arrayList = new ArrayList<>();
        favouritesList.clear();
        //If no favourites have been added, do nothing
        if(res.getCount() == 0) {
            // show message
            arrayList.add("No favourites to display");
            showFavourites(arrayList);
            favouritesList.clear();
            return;
        }

        while (res.moveToNext()) {
            //Get favourites names from database and ddd them to the list
            favouritesList.add(res.getString(1));

            arrayList.add(res.getString(1)+"\n" +
                    "Times Viewed: "+ res.getString(3)+"\n");
        }

        showFavourites(arrayList);

        //Open the link to the stream on click
        ListView listView = (ListView) findViewById(R.id.favouritesListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                    intent.putExtra("channelName", favouritesList.get(position));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //If correct list view AND there is at least 1 favourite added, then create context menu
        if (v.getId() == R.id.favouritesListView && favouritesList.size() > 0) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(favouritesList.get(info.position));
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
                return true;

            case R.id.remove:
                try {
                    int result = databaseHelper.deleteData(favouritesList.get(info.position));
                    if(result > 0) {
                        Toast.makeText(this, "Favourite removed!", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(this,"An error occurred: favourite not deleted",Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                updateFavouritesList();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }
}

